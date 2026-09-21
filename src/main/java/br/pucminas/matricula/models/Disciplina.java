package br.pucminas.matricula.models;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {
    private String nome;
    private Professor professor;
    private Curso curso;
    private List<Aluno> alunosMatriculados;
    private static final int LIMITE_ALUNOS = 60;
    private static final int MIN_ALUNOS = 3;
    private boolean ativa;

    public Disciplina(String nome, Curso curso) {
        this.nome = nome;
        this.curso = curso;
        this.alunosMatriculados = new ArrayList<>();
        this.ativa = false;
    }

    public boolean adicionarAluno(Aluno aluno) {
        // Stub: Adiciona aluno se não atingiu limite
        if (alunosMatriculados.size() < LIMITE_ALUNOS) {
            alunosMatriculados.add(aluno);
            return true;
        }
        return false;
    }

    public boolean removerAluno(Aluno aluno) {
        // Stub: Remove aluno da disciplina
        return alunosMatriculados.remove(aluno);
    }

    public void verificarStatus() {
        // Stub: Verifica se atingiu mínimo de alunos
        if (alunosMatriculados.size() >= MIN_ALUNOS) {
            this.ativa = true;
        } else {
            this.ativa = false;
        }
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    
    public boolean isAtiva() { return ativa; }
    public List<Aluno> getAlunosMatriculados() { return alunosMatriculados; }
}

