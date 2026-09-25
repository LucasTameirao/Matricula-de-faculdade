package br.pucminas.matricula.models;

import java.util.List;

public class Secretaria extends Usuario {
    private static final long serialVersionUID = 1L;

    public Secretaria(String login, String senha, String nome) {
        super(login, senha, nome);
    }

    /** Gera o currículo do semestre com as disciplinas ofertadas, reiniciando suas inscrições. */
    public Curriculo gerarCurriculo(String semestre, List<Disciplina> disciplinas) {
        Curriculo curriculo = new Curriculo(semestre);
        for (Disciplina disciplina : disciplinas) {
            disciplina.reiniciar();
            curriculo.adicionarDisciplina(disciplina);
        }
        return curriculo;
    }

    public void abrirPeriodoMatriculas(Curriculo curriculo) {
        curriculo.abrirPeriodoMatriculas();
    }

    /** Encerra o período e define quais disciplinas ficam ativas ou são canceladas. */
    public void encerrarPeriodoMatriculas(Curriculo curriculo) {
        curriculo.encerrarPeriodoMatriculas();
    }

    @Override
    public String getTipo() { return "Secretaria"; }
}
