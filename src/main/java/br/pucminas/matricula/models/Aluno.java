package br.pucminas.matricula.models;

import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {
    private String nome;
    private String matricula;
    private List<Disciplina> disciplinasMatriculadas;
    private List<Disciplina> disciplinasOptativas;

    public Aluno(String login, String senha, String nome, String matricula) {
        super(login, senha);
        this.nome = nome;
        this.matricula = matricula;
        this.disciplinasMatriculadas = new ArrayList<>();
        this.disciplinasOptativas = new ArrayList<>();
    }

    public boolean matricular(Disciplina disciplina, boolean isOptativa) {
        // Stub: Realiza matrícula do aluno em uma disciplina
        return true;
    }

    public boolean cancelarMatricula(Disciplina disciplina) {
        // Stub: Cancela a matrícula do aluno em uma disciplina
        return true;
    }

    public List<Disciplina> consultarDisciplinas() {
        // Stub: Retorna as disciplinas atuais do aluno
        return this.disciplinasMatriculadas;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}

