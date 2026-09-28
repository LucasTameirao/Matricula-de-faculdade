package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aluno extends Usuario {
    private Long id;
    private String matricula;
    private Curso curso;
    private final List<Matricula> matriculas;

    public Aluno(String login, String senha, String nome, String matricula, Curso curso) {
        super(login, senha, nome);
        this.matricula = matricula;
        this.curso = curso;
        this.matriculas = new ArrayList<>();
    }

    /** Retorna a matrícula do semestre, criando-a se o aluno ainda não tiver uma. */
    public Matricula iniciarMatricula(Long idMatricula, String semestre) {
        Matricula existente = getMatricula(semestre);
        if (existente != null) {
            return existente;
        }
        Matricula nova = new Matricula(idMatricula, this, semestre);
        matriculas.add(nova);
        return nova;
    }

    public void matricular(String semestre, Disciplina disciplina, boolean isOptativa) {
        exigirMatricula(semestre).adicionarDisciplina(disciplina, isOptativa);
    }

    public void cancelarMatricula(String semestre, Disciplina disciplina) {
        exigirMatricula(semestre).cancelarDisciplina(disciplina);
    }

    public List<Disciplina> consultarDisciplinas(String semestre) {
        Matricula m = getMatricula(semestre);
        return m == null ? List.of() : m.getDisciplinas();
    }

    public Matricula getMatricula(String semestre) {
        return matriculas.stream().filter(m -> m.getSemestre().equals(semestre)).findFirst().orElse(null);
    }

    /** Usado pela persistência para recriar as matrículas salvas. */
    public void restaurarMatricula(Matricula matricula) {
        matriculas.add(matricula);
    }

    public void cancelarTodasMatriculas() {
        matriculas.forEach(Matricula::cancelarTodas);
    }

    private Matricula exigirMatricula(String semestre) {
        Matricula m = getMatricula(semestre);
        if (m == null) {
            throw new RegraNegocioException("Você não possui matrícula no semestre " + semestre + ".");
        }
        return m;
    }

    @Override
    public String getTipo() { return "Aluno"; }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public List<Matricula> getMatriculas() { return Collections.unmodifiableList(matriculas); }
}
