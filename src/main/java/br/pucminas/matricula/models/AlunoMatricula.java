package br.pucminas.matricula.models;

import java.time.LocalDateTime;

/**
 * Vínculo entre o aluno, a sua matrícula do semestre e uma disciplina.
 * Cancelar a disciplina não apaga o vínculo: ele fica com status CANCELADO no histórico.
 */
public class AlunoMatricula {
    private final Aluno aluno;
    private final Matricula matricula;
    private final Disciplina disciplina;
    private final LocalDateTime dataVinculo;
    private final boolean optativa;
    private StatusVinculo status;

    public AlunoMatricula(Matricula matricula, Disciplina disciplina, boolean optativa) {
        this(matricula, disciplina, optativa, LocalDateTime.now(), StatusVinculo.ATIVO);
    }

    /** Usado pela persistência para recriar um vínculo salvo. */
    public AlunoMatricula(Matricula matricula, Disciplina disciplina, boolean optativa,
                          LocalDateTime dataVinculo, StatusVinculo status) {
        this.aluno = matricula.getAluno();
        this.matricula = matricula;
        this.disciplina = disciplina;
        this.optativa = optativa;
        this.dataVinculo = dataVinculo;
        this.status = status;
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
