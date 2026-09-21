package br.pucminas.matricula.models;

import java.util.ArrayList;
import java.util.List;

public class Professor extends Usuario {
    private String nome;
    private List<Disciplina> disciplinasMinistradas;

    public Professor(String login, String senha, String nome) {
        super(login, senha);
        this.nome = nome;
        this.disciplinasMinistradas = new ArrayList<>();
    }

    public List<Aluno> consultarAlunos(Disciplina disciplina) {
        // Stub: Retorna a lista de alunos matriculados na disciplina informada
        return new ArrayList<>();
    }
    
    public List<Disciplina> consultarDisciplinas() {
        // Stub: Retorna as disciplinas ministradas por este professor
        return this.disciplinasMinistradas;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}

