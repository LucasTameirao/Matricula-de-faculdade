package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aluno extends Usuario {
    private static final long serialVersionUID = 1L;

    public static final int MAX_OBRIGATORIAS = 4;
    public static final int MAX_OPTATIVAS = 2;

    private String matricula;
    private Curso curso;
    private final List<Disciplina> disciplinasObrigatorias;
    private final List<Disciplina> disciplinasOptativas;

    public Aluno(String login, String senha, String nome, String matricula, Curso curso) {
        super(login, senha, nome);
        this.matricula = matricula;
        this.curso = curso;
        this.disciplinasObrigatorias = new ArrayList<>();
        this.disciplinasOptativas = new ArrayList<>();
    }

    /**
     * Matricula o aluno na disciplina, respeitando o limite de 4 obrigatórias
     * (1ª opção) e 2 optativas (alternativas).
     */
    public void matricular(Disciplina disciplina, boolean isOptativa) {
        if (estaMatriculado(disciplina)) {
            throw new RegraNegocioException("Você já está matriculado(a) em " + disciplina.getNome() + ".");
        }
        List<Disciplina> lista = isOptativa ? disciplinasOptativas : disciplinasObrigatorias;
        int limite = isOptativa ? MAX_OPTATIVAS : MAX_OBRIGATORIAS;
        if (lista.size() >= limite) {
            throw new RegraNegocioException("Limite de " + limite + " disciplinas "
                    + (isOptativa ? "optativas" : "obrigatórias") + " atingido.");
        }
        disciplina.adicionarAluno(this);
        lista.add(disciplina);
    }

    public void cancelarMatricula(Disciplina disciplina) {
        boolean removida = disciplinasObrigatorias.remove(disciplina) || disciplinasOptativas.remove(disciplina);
        if (!removida) {
            throw new RegraNegocioException("Você não está matriculado(a) em " + disciplina.getNome() + ".");
        }
        disciplina.removerAluno(this);
    }

    public List<Disciplina> consultarDisciplinas() {
        List<Disciplina> todas = new ArrayList<>(disciplinasObrigatorias);
        todas.addAll(disciplinasOptativas);
        return todas;
    }

    public boolean estaMatriculado(Disciplina disciplina) {
        return disciplinasObrigatorias.contains(disciplina) || disciplinasOptativas.contains(disciplina);
    }

    public boolean isOptativa(Disciplina disciplina) {
        return disciplinasOptativas.contains(disciplina);
    }

    /** Remove as matrículas do semestre anterior (usado ao gerar um novo currículo). */
    public void limparMatriculas() {
        disciplinasObrigatorias.clear();
        disciplinasOptativas.clear();
    }

    @Override
    public String getTipo() { return "Aluno"; }

    // Getters e Setters
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public List<Disciplina> getDisciplinasObrigatorias() { return Collections.unmodifiableList(disciplinasObrigatorias); }
    public List<Disciplina> getDisciplinasOptativas() { return Collections.unmodifiableList(disciplinasOptativas); }
}
