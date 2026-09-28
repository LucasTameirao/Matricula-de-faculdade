package br.pucminas.matricula.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Vínculo entre o aluno, a sua matrícula do semestre e uma disciplina.
 * Cancelar a disciplina não apaga o vínculo: ele fica com status CANCELADO no histórico.
 */
public class AlunoMatricula implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Aluno aluno;
    private final Matricula matricula;
    private final Disciplina disciplina;
    private final LocalDateTime dataVinculo;
    private final boolean optativa;
    private StatusVinculo status;

    public AlunoMatricula(Matricula matricula, Disciplina disciplina, boolean optativa) {
        this.aluno = matricula.getAluno();
        this.matricula = matricula;
        this.disciplina = disciplina;
        this.optativa = optativa;
        this.dataVinculo = LocalDateTime.now();
        this.status = StatusVinculo.ATIVO;
    }

    public void cancelar() {
        status = StatusVinculo.CANCELADO;
    }

    public boolean isAtivo() {
        return status == StatusVinculo.ATIVO;
    }

    // Getters
    public Aluno getAluno() { return aluno; }
    public Matricula getMatricula() { return matricula; }
    public Disciplina getDisciplina() { return disciplina; }
    public LocalDateTime getDataVinculo() { return dataVinculo; }
    public boolean isOptativa() { return optativa; }
    public StatusVinculo getStatus() { return status; }
}
