package br.pucminas.matricula.models;

import java.util.ArrayList;
import java.util.List;

public class Curriculo {
    private String semestre;
    private List<Disciplina> disciplinas;

    public Curriculo(String semestre) {
        this.semestre = semestre;
        this.disciplinas = new ArrayList<>();
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
    }
    
    // Getters e Setters
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    
    public List<Disciplina> getDisciplinas() { return disciplinas; }
}

