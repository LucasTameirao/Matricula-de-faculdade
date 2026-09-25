package br.pucminas.matricula.services;

import br.pucminas.matricula.exceptions.RegraNegocioException;
import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Curriculo;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.Universidade;
import br.pucminas.matricula.models.Usuario;
import br.pucminas.matricula.persistence.ArquivoPersistencia;

import java.util.ArrayList;
import java.util.List;

/**
 * Fachada que coordena os casos de uso do sistema: aplica as regras que dependem
 * do contexto (período de matrículas, currículo atual), notifica o sistema de
 * cobranças e persiste os dados após cada alteração.
 */
public class SistemaMatriculas {
    private final Universidade universidade;
    private final ArquivoPersistencia persistencia;
    private final SistemaCobrancaService cobranca;

    public SistemaMatriculas(Universidade universidade, ArquivoPersistencia persistencia,
                             SistemaCobrancaService cobranca) {
        this.universidade = universidade;
        this.persistencia = persistencia;
        this.cobranca = cobranca;
    }

    // ---------- Autenticação ----------

    public Usuario login(String login, String senha) {
        Usuario usuario = universidade.buscarUsuario(login);
        if (usuario == null || !usuario.autenticar(login, senha)) {
            throw new RegraNegocioException("Login ou senha inválidos.");
        }
        return usuario;
    }

    // ---------- Aluno ----------

    public Curriculo getCurriculoAtual() {
        return universidade.getCurriculoAtual();
    }

    public List<Disciplina> getDisciplinasOfertadas() {
        Curriculo curriculo = universidade.getCurriculoAtual();
        return curriculo == null ? List.of() : curriculo.getDisciplinas();
    }

    public void matricular(Aluno aluno, String codigoDisciplina, boolean isOptativa) {
        Curriculo curriculo = exigirPeriodoAberto();
        Disciplina disciplina = exigirDisciplina(codigoDisciplina);
        if (!curriculo.contemDisciplina(disciplina)) {
            throw new RegraNegocioException(disciplina.getNome() + " não é ofertada no semestre " + curriculo.getSemestre() + ".");
        }
        aluno.matricular(disciplina, isOptativa);
        cobranca.notificarCobranca(aluno, aluno.consultarDisciplinas(), curriculo.getSemestre());
        salvar();
    }

    public void cancelarMatricula(Aluno aluno, String codigoDisciplina) {
        Curriculo curriculo = exigirPeriodoAberto();
        aluno.cancelarMatricula(exigirDisciplina(codigoDisciplina));
        cobranca.notificarCobranca(aluno, aluno.consultarDisciplinas(), curriculo.getSemestre());
        salvar();
    }

    // ---------- Professor ----------

    public List<Aluno> consultarAlunos(Professor professor, String codigoDisciplina) {
        return professor.consultarAlunos(exigirDisciplina(codigoDisciplina));
    }

    // ---------- Secretaria: cursos ----------

    public List<Curso> getCursos() { return universidade.getCursos(); }

    public void cadastrarCurso(String nome, int numeroCreditos) {
        exigirTexto(nome, "Nome do curso");
        if (numeroCreditos <= 0) {
            throw new RegraNegocioException("O número de créditos deve ser positivo.");
        }
        universidade.adicionarCurso(new Curso(nome.trim(), numeroCreditos));
        salvar();
    }

    public void removerCurso(String nome) {
        Curso curso = universidade.buscarCurso(nome);
        if (curso == null) {
            throw new RegraNegocioException("Curso '" + nome + "' não encontrado.");
        }
        if (!curso.getDisciplinas().isEmpty()) {
            throw new RegraNegocioException("Remova antes as disciplinas do curso " + curso.getNome() + ".");
        }
        if (universidade.getAlunos().stream().anyMatch(a -> a.getCurso() == curso)) {
            throw new RegraNegocioException("Existem alunos vinculados ao curso " + curso.getNome() + ".");
        }
        universidade.removerCurso(curso);
        salvar();
    }

    // ---------- Secretaria: professores ----------

    public List<Professor> getProfessores() { return universidade.getProfessores(); }

    public void cadastrarProfessor(String login, String senha, String nome) {
        exigirTexto(login, "Login");
        exigirTexto(senha, "Senha");
        exigirTexto(nome, "Nome");
        universidade.adicionarUsuario(new Professor(login.trim(), senha, nome.trim()));
        salvar();
    }

    public void removerProfessor(String login) {
        Professor professor = universidade.buscarProfessor(login);
        if (professor == null) {
            throw new RegraNegocioException("Professor com login '" + login + "' não encontrado.");
        }
        if (!professor.consultarDisciplinas().isEmpty()) {
            throw new RegraNegocioException("O professor ainda ministra disciplinas. Remova-as ou troque o professor antes.");
        }
        universidade.removerUsuario(professor);
        salvar();
    }

    // ---------- Secretaria: alunos ----------

    public List<Aluno> getAlunos() { return universidade.getAlunos(); }

    public void cadastrarAluno(String login, String senha, String nome, String matricula, String nomeCurso) {
        exigirTexto(login, "Login");
        exigirTexto(senha, "Senha");
        exigirTexto(nome, "Nome");
        exigirTexto(matricula, "Matrícula");
        Curso curso = exigirCurso(nomeCurso);
        universidade.adicionarUsuario(new Aluno(login.trim(), senha, nome.trim(), matricula.trim(), curso));
        salvar();
    }

    public void removerAluno(String matricula) {
        Aluno aluno = universidade.buscarAluno(matricula);
        if (aluno == null) {
            throw new RegraNegocioException("Aluno com matrícula '" + matricula + "' não encontrado.");
        }
        for (Disciplina disciplina : aluno.consultarDisciplinas()) {
            aluno.cancelarMatricula(disciplina);
        }
        universidade.removerUsuario(aluno);
        salvar();
    }

    // ---------- Secretaria: disciplinas ----------

    public List<Disciplina> getDisciplinas() { return universidade.getDisciplinas(); }

    public void cadastrarDisciplina(String codigo, String nome, String nomeCurso, String loginProfessor) {
        exigirTexto(codigo, "Código");
        exigirTexto(nome, "Nome");
        Curso curso = exigirCurso(nomeCurso);
        Professor professor = exigirProfessor(loginProfessor);
        Disciplina disciplina = new Disciplina(codigo.trim().toUpperCase(), nome.trim(), curso, professor);
        universidade.adicionarDisciplina(disciplina);
        curso.adicionarDisciplina(disciplina);
        professor.adicionarDisciplina(disciplina);
        salvar();
    }

    public void alterarProfessorDisciplina(String codigo, String loginProfessor) {
        Disciplina disciplina = exigirDisciplina(codigo);
        Professor novo = exigirProfessor(loginProfessor);
        disciplina.getProfessor().removerDisciplina(disciplina);
        disciplina.setProfessor(novo);
        novo.adicionarDisciplina(disciplina);
        salvar();
    }

    public void removerDisciplina(String codigo) {
        Disciplina disciplina = exigirDisciplina(codigo);
        if (!disciplina.getAlunosMatriculados().isEmpty()) {
            throw new RegraNegocioException("A disciplina possui alunos matriculados e não pode ser removida.");
        }
        Curriculo curriculo = universidade.getCurriculoAtual();
        if (curriculo != null) {
            curriculo.removerDisciplina(disciplina);
        }
        disciplina.getCurso().removerDisciplina(disciplina);
        disciplina.getProfessor().removerDisciplina(disciplina);
        universidade.removerDisciplina(disciplina);
        salvar();
    }

    // ---------- Secretaria: currículo e período de matrículas ----------

    public Curriculo gerarCurriculo(Secretaria secretaria, String semestre, List<String> codigosDisciplinas) {
        exigirTexto(semestre, "Semestre");
        Curriculo atual = universidade.getCurriculoAtual();
        if (atual != null && atual.isPeriodoMatriculasAberto()) {
            throw new RegraNegocioException("Encerre o período de matrículas atual antes de gerar um novo currículo.");
        }
        List<Disciplina> ofertadas = new ArrayList<>();
        for (String codigo : codigosDisciplinas) {
            Disciplina disciplina = exigirDisciplina(codigo);
            if (!ofertadas.contains(disciplina)) {
                ofertadas.add(disciplina);
            }
        }
        if (ofertadas.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos uma disciplina para o currículo.");
        }
        // Novo semestre: as matrículas do semestre anterior deixam de valer.
        universidade.getAlunos().forEach(Aluno::limparMatriculas);
        universidade.getDisciplinas().forEach(Disciplina::retirarDeOferta);

        Curriculo curriculo = secretaria.gerarCurriculo(semestre.trim(), ofertadas);
        universidade.setCurriculoAtual(curriculo);
        salvar();
        return curriculo;
    }

    public void abrirPeriodoMatriculas(Secretaria secretaria) {
        secretaria.abrirPeriodoMatriculas(exigirCurriculo());
        salvar();
    }

    public Curriculo encerrarPeriodoMatriculas(Secretaria secretaria) {
        Curriculo curriculo = exigirCurriculo();
        secretaria.encerrarPeriodoMatriculas(curriculo);
        salvar();
        return curriculo;
    }

    // ---------- Auxiliares ----------

    private void salvar() {
        persistencia.salvar(universidade);
    }

    private Curriculo exigirCurriculo() {
        Curriculo curriculo = universidade.getCurriculoAtual();
        if (curriculo == null) {
            throw new RegraNegocioException("Nenhum currículo foi gerado ainda.");
        }
        return curriculo;
    }

    private Curriculo exigirPeriodoAberto() {
        Curriculo curriculo = universidade.getCurriculoAtual();
        if (curriculo == null || !curriculo.isPeriodoMatriculasAberto()) {
            throw new RegraNegocioException("Fora do período de matrículas.");
        }
        return curriculo;
    }

    private Disciplina exigirDisciplina(String codigo) {
        Disciplina disciplina = universidade.buscarDisciplina(codigo == null ? "" : codigo.trim());
        if (disciplina == null) {
            throw new RegraNegocioException("Disciplina '" + codigo + "' não encontrada.");
        }
        return disciplina;
    }

    private Curso exigirCurso(String nome) {
        Curso curso = universidade.buscarCurso(nome == null ? "" : nome.trim());
        if (curso == null) {
            throw new RegraNegocioException("Curso '" + nome + "' não encontrado.");
        }
        return curso;
    }

    private Professor exigirProfessor(String login) {
        Professor professor = universidade.buscarProfessor(login == null ? "" : login.trim());
        if (professor == null) {
            throw new RegraNegocioException("Professor com login '" + login + "' não encontrado.");
        }
        return professor;
    }

    private void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " não pode ser vazio.");
        }
    }
}
