package br.pucminas.matricula.models;

public enum StatusDisciplina {
    NAO_OFERTADA("Não ofertada"),
    AGUARDANDO_PERIODO("Aguardando período de matrículas"),
    EM_MATRICULA("Em matrícula"),
    ATIVA("Ativa"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDisciplina(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
