package br.pucminas.matricula.models;

import java.util.ArrayList;
import java.util.List;

public class Curso {
    private String nome;
    private int numeroCreditos;
    private List<Disciplina> disciplinas;

    public Curso(String nome, int numeroCreditos) {
        this.nome = nome;
        this.numeroCreditos = numeroCreditos;
        this.disciplinas = new ArrayList<>();
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
    }
    
    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public int getNumeroCreditos() { return numeroCreditos; }
    public void setNumeroCreditos(int numeroCreditos) { this.numeroCreditos = numeroCreditos; }
    
    public List<Disciplina> getDisciplinas() { return disciplinas; }
}

