package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Curriculo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String semestre;
    private final List<Disciplina> disciplinas;
    private boolean periodoMatriculasAberto;
    private boolean periodoMatriculasEncerrado;

    public Curriculo(String semestre) {
        this.semestre = semestre;
        this.disciplinas = new ArrayList<>();
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        if (!disciplinas.contains(disciplina)) {
            disciplinas.add(disciplina);
        }
    }

    public void removerDisciplina(Disciplina disciplina) {
        disciplinas.remove(disciplina);
    }

    public void abrirPeriodoMatriculas() {
        if (periodoMatriculasEncerrado) {
            throw new RegraNegocioException("O período de matrículas de " + semestre
                    + " já foi encerrado. Gere um novo currículo.");
        }
        if (periodoMatriculasAberto) {
            throw new RegraNegocioException("O período de matrículas já está aberto.");
        }
        periodoMatriculasAberto = true;
    }

    public void encerrarPeriodoMatriculas() {
        if (!periodoMatriculasAberto) {
            throw new RegraNegocioException("O período de matrículas não está aberto.");
        }
        periodoMatriculasAberto = false;
        periodoMatriculasEncerrado = true;
        for (Disciplina disciplina : disciplinas) {
            disciplina.verificarStatus();
        }
    }

    public boolean contemDisciplina(Disciplina disciplina) {
        return disciplinas.contains(disciplina);
    }

    public String getSituacaoPeriodo() {
        if (periodoMatriculasAberto) return "Período de matrículas ABERTO";
        if (periodoMatriculasEncerrado) return "Período de matrículas ENCERRADO";
        return "Período de matrículas ainda não aberto";
    }

    // Getters e Setters
    public String getSemestre() { return semestre; }

    public List<Disciplina> getDisciplinas() { return Collections.unmodifiableList(disciplinas); }

    public boolean isPeriodoMatriculasAberto() { return periodoMatriculasAberto; }
    public boolean isPeriodoMatriculasEncerrado() { return periodoMatriculasEncerrado; }
}
