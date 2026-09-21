package br.pucminas.matricula.services;

import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Disciplina;

import java.util.List;

public class SistemaCobrancaService {

    public void notificarCobranca(Aluno aluno, List<Disciplina> disciplinasSemestre) {
        // Stub: Notifica o sistema de cobranças sobre as matrículas do aluno
        System.out.println("Notificando cobrança para o aluno: " + aluno.getNome());
    }
}

