package br.pucminas.matricula.models;

import br.pucminas.matricula.exceptions.RegraNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Professor extends Usuario {
    private static final long serialVersionUID = 1L;

    private final List<Disciplina> disciplinasMinistradas;

    public Professor(String login, String senha, String nome) {
        super(login, senha, nome);
        this.disciplinasMinistradas = new ArrayList<>();
    }

    /** Retorna os alunos matriculados em uma disciplina ministrada por este professor. */
    public List<Aluno> consultarAlunos(Disciplina disciplina) {
        if (!disciplinasMinistradas.contains(disciplina)) {
            throw new RegraNegocioException("Você não ministra a disciplina " + disciplina.getNome() + ".");
        }
        return new ArrayList<>(disciplina.getAlunosMatriculados());
    }

    public List<Disciplina> consultarDisciplinas() {
        return Collections.unmodifiableList(disciplinasMinistradas);
    }

    public void adicionarDisciplina(Disciplina disciplina) {
        if (!disciplinasMinistradas.contains(disciplina)) {
            disciplinasMinistradas.add(disciplina);
        }
    }

    public void removerDisciplina(Disciplina disciplina) {
        disciplinasMinistradas.remove(disciplina);
    }

    @Override
    public String getTipo() { return "Professor"; }
}
