package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Disciplina {
    public static final int LIMITE_ALUNOS = 60;
    public static final int MIN_ALUNOS = 3;

    private Long id;
    private String codigo;
    private String nome;
    private Curso curso;
    private Professor professor;
    private final List<AlunoMatricula> inscricoes;
    private StatusDisciplina status;

    public Disciplina(String codigo, String nome, Curso curso, Professor professor) {
        this.codigo = codigo;
        this.nome = nome;
        this.curso = curso;
        this.professor = professor;
        this.inscricoes = new ArrayList<>();
        this.status = StatusDisciplina.NAO_OFERTADA;
    }

    /** Inscreve o aluno; ao atingir 60 alunos as inscrições da disciplina ficam encerradas. */
    public void adicionarInscricao(AlunoMatricula inscricao) {
        if (status != StatusDisciplina.EM_MATRICULA) {
            throw new RegraNegocioException("As inscrições para " + nome + " não estão abertas.");
        }
        if (isLotada()) {
            throw new RegraNegocioException(nome + " atingiu o limite de " + LIMITE_ALUNOS
                    + " alunos. Inscrições encerradas.");
        }
        inscricoes.add(inscricao);
    }

    public boolean removerInscricao(AlunoMatricula inscricao) {
        return inscricoes.remove(inscricao);
    }

    /** Chamado ao fim do período: a disciplina só ocorre com pelo menos 3 alunos. */
    public void verificarStatus() {
        status = inscricoes.size() >= MIN_ALUNOS ? StatusDisciplina.ATIVA : StatusDisciplina.CANCELADA;
    }

    /** Inclui a disciplina no currículo; as inscrições abrem junto com o período de matrículas. */
    public void ofertar() {
        inscricoes.clear();
        status = StatusDisciplina.AGUARDANDO_PERIODO;
    }

    /** Prepara a disciplina para um novo período de matrículas. */
    public void reiniciar() {
        inscricoes.clear();
        status = StatusDisciplina.EM_MATRICULA;
    }

    /** Retira a disciplina da oferta (ela não faz parte do currículo atual). */
    public void retirarDeOferta() {
        inscricoes.clear();
        status = StatusDisciplina.NAO_OFERTADA;
    }

    /** Usado pela persistência para recriar o estado salvo, sem reaplicar as regras. */
    public void restaurarStatus(StatusDisciplina status) {
        this.status = status;
    }

    /** Usado pela persistência para recriar os inscritos da oferta atual. */
    public void restaurarInscricao(AlunoMatricula inscricao) {
        inscricoes.add(inscricao);
    }

    public boolean isLotada() {
        return inscricoes.size() >= LIMITE_ALUNOS;
    }

    public boolean isInscricoesAbertas() {
        return status == StatusDisciplina.EM_MATRICULA && !isLotada();
    }

    public boolean isAtiva() {
        return status == StatusDisciplina.ATIVA;
    }

    public String getSituacao() {
        if (status != StatusDisciplina.EM_MATRICULA) {
            return status.getDescricao();
        }
        return isLotada() ? "Inscrições encerradas (lotada)" : "Inscrições abertas";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Curso getCurso() { return curso; }

    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    public StatusDisciplina getStatus() { return status; }
    public List<AlunoMatricula> getInscricoes() { return Collections.unmodifiableList(inscricoes); }

    public List<Aluno> getAlunosMatriculados() {
        return inscricoes.stream().map(AlunoMatricula::getAluno).toList();
    }
}
