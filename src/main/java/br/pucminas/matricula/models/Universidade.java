package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrega todos os dados mantidos pela secretaria. É o objeto persistido em arquivo.
 */
public class Universidade implements Serializable {
    private static final long serialVersionUID = 2L;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Curso> cursos = new ArrayList<>();
    private final List<Disciplina> disciplinas = new ArrayList<>();
    private Curriculo curriculoAtual;
    private long ultimoId;

    /** Gera identificadores sequenciais para alunos, disciplinas e matrículas. */
    public Long gerarId() {
        return ++ultimoId;
    }

    // ---------- Usuários ----------

    public void adicionarUsuario(Usuario usuario) {
        if (buscarUsuario(usuario.getLogin()) != null) {
            throw new RegraNegocioException("Já existe um usuário com o login '" + usuario.getLogin() + "'.");
        }
        if (usuario instanceof Aluno aluno && buscarAluno(aluno.getMatricula()) != null) {
            throw new RegraNegocioException("Já existe um aluno com a matrícula '" + aluno.getMatricula() + "'.");
        }
        if (usuario instanceof Aluno aluno && aluno.getId() == null) {
            aluno.setId(gerarId());
        }
        usuarios.add(usuario);
    }

    public void removerUsuario(Usuario usuario) {
        usuarios.remove(usuario);
    }

    public Usuario buscarUsuario(String login) {
        return usuarios.stream().filter(u -> u.getLogin().equals(login)).findFirst().orElse(null);
    }

    public Aluno buscarAluno(String matricula) {
        return getAlunos().stream().filter(a -> a.getMatricula().equalsIgnoreCase(matricula)).findFirst().orElse(null);
    }

    public Professor buscarProfessor(String login) {
        return buscarUsuario(login) instanceof Professor professor ? professor : null;
    }

    public List<Aluno> getAlunos() {
        return usuarios.stream().filter(Aluno.class::isInstance).map(Aluno.class::cast).toList();
    }

    public List<Professor> getProfessores() {
        return usuarios.stream().filter(Professor.class::isInstance).map(Professor.class::cast).toList();
    }

    // ---------- Cursos ----------

    public void adicionarCurso(Curso curso) {
        if (buscarCurso(curso.getNome()) != null) {
            throw new RegraNegocioException("Já existe um curso chamado '" + curso.getNome() + "'.");
        }
        cursos.add(curso);
    }

    public void removerCurso(Curso curso) {
        cursos.remove(curso);
    }

    public Curso buscarCurso(String nome) {
        return cursos.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }

    public List<Curso> getCursos() { return Collections.unmodifiableList(cursos); }

    // ---------- Disciplinas ----------

    public void adicionarDisciplina(Disciplina disciplina) {
        if (buscarDisciplina(disciplina.getCodigo()) != null) {
            throw new RegraNegocioException("Já existe uma disciplina com o código '" + disciplina.getCodigo() + "'.");
        }
        if (disciplina.getId() == null) {
            disciplina.setId(gerarId());
        }
        disciplinas.add(disciplina);
    }

    public void removerDisciplina(Disciplina disciplina) {
        disciplinas.remove(disciplina);
    }

    public Disciplina buscarDisciplina(String codigo) {
        return disciplinas.stream().filter(d -> d.getCodigo().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    }

    public List<Disciplina> getDisciplinas() { return Collections.unmodifiableList(disciplinas); }

    // ---------- Currículo ----------

    public Curriculo getCurriculoAtual() { return curriculoAtual; }
    public void setCurriculoAtual(Curriculo curriculoAtual) { this.curriculoAtual = curriculoAtual; }
}
