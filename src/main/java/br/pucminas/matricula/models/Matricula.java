package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * Matrícula de um aluno em um semestre. Reúne os vínculos com as disciplinas
 * e aplica o limite de 4 obrigatórias (1ª opção) e 2 optativas (alternativas).
 */
public class Matricula implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_OBRIGATORIAS = 4;
    public static final int MAX_OPTATIVAS = 2;

    private final Long id;
    private final String codigoMatricula;
    private final LocalDateTime dataCriacao;
    private StatusMatricula status;
    private final Aluno aluno;
    private final String semestre;
    private final List<AlunoMatricula> itens;

    public Matricula(Long id, Aluno aluno, String semestre) {
        this.id = id;
        this.aluno = aluno;
        this.semestre = semestre;
        this.codigoMatricula = semestre + "-" + aluno.getMatricula();
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusMatricula.EM_ANDAMENTO;
        this.itens = new ArrayList<>();
    }

    public AlunoMatricula adicionarDisciplina(Disciplina disciplina, boolean isOptativa) {
        exigirEmAndamento();
        if (buscarItemAtivo(disciplina) != null) {
            throw new RegraNegocioException("Você já está matriculado(a) em " + disciplina.getNome() + ".");
        }
        int quantidade = isOptativa ? contarOptativas() : contarObrigatorias();
        int limite = isOptativa ? MAX_OPTATIVAS : MAX_OBRIGATORIAS;
        if (quantidade >= limite) {
            throw new RegraNegocioException("Limite de " + limite + " disciplinas "
                    + (isOptativa ? "optativas" : "obrigatórias") + " atingido.");
        }
        AlunoMatricula item = new AlunoMatricula(this, disciplina, isOptativa);
        disciplina.adicionarInscricao(item);
        itens.add(item);
        return item;
    }

    public void cancelarDisciplina(Disciplina disciplina) {
        exigirEmAndamento();
        AlunoMatricula item = buscarItemAtivo(disciplina);
        if (item == null) {
            throw new RegraNegocioException("Você não está matriculado(a) em " + disciplina.getNome() + ".");
        }
        item.cancelar();
        disciplina.removerInscricao(item);
    }

    /** Cancela todos os vínculos ativos (usado quando o aluno é removido do sistema). */
    public void cancelarTodas() {
        for (AlunoMatricula item : getItensAtivos()) {
            item.cancelar();
            item.getDisciplina().removerInscricao(item);
        }
    }

    /** Fecha a matrícula ao fim do período de matrículas. */
    public void concluir() {
        status = StatusMatricula.CONCLUIDA;
    }

    public AlunoMatricula buscarItemAtivo(Disciplina disciplina) {
        return itens.stream()
                .filter(i -> i.isAtivo() && i.getDisciplina() == disciplina)
                .findFirst().orElse(null);
    }

    public List<AlunoMatricula> getItensAtivos() {
        return itens.stream().filter(AlunoMatricula::isAtivo).toList();
    }

    public List<Disciplina> getDisciplinas() {
        return getItensAtivos().stream().map(AlunoMatricula::getDisciplina).toList();
    }

    public int contarObrigatorias() {
        return (int) getItensAtivos().stream().filter(i -> !i.isOptativa()).count();
    }

    public int contarOptativas() {
        return (int) getItensAtivos().stream().filter(AlunoMatricula::isOptativa).count();
    }

    private void exigirEmAndamento() {
        if (status != StatusMatricula.EM_ANDAMENTO) {
            throw new RegraNegocioException("A matrícula " + codigoMatricula + " já foi concluída.");
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getCodigoMatricula() { return codigoMatricula; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public StatusMatricula getStatus() { return status; }
    public Aluno getAluno() { return aluno; }
    public String getSemestre() { return semestre; }
    public List<AlunoMatricula> getItens() { return Collections.unmodifiableList(itens); }
}
