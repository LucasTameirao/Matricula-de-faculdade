package br.pucminas.matricula.models;

public enum StatusVinculo {
    ATIVO("Ativo"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusVinculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
