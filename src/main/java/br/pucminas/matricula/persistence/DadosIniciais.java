package br.pucminas.matricula.persistence;

import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.Universidade;

/**
 * Dados de exemplo usados na primeira execução, quando ainda não há arquivo salvo.
 */
public final class DadosIniciais {

    private DadosIniciais() {
    }

    public static Universidade criar() {
        Universidade universidade = new Universidade();

        Secretaria secretaria = new Secretaria("secretaria", "123", "Secretaria Acadêmica");
        universidade.adicionarUsuario(secretaria);

        Curso engSoftware = new Curso("Engenharia de Software", 240);
        universidade.adicionarCurso(engSoftware);

        Professor ana = new Professor("ana", "123", "Ana Souza");
        Professor carlos = new Professor("carlos", "123", "Carlos Lima");
        universidade.adicionarUsuario(ana);
        universidade.adicionarUsuario(carlos);

        String[][] disciplinas = {
                {"ES101", "Algoritmos e Estruturas de Dados"},
                {"ES102", "Projeto de Software"},
                {"ES103", "Banco de Dados"},
                {"ES104", "Cálculo I"},
                {"ES105", "Arquitetura de Computadores"},
                {"ES106", "Empreendedorismo"},
                {"ES107", "Inglês Instrumental"},
        };
        for (int i = 0; i < disciplinas.length; i++) {
            Professor professor = i % 2 == 0 ? ana : carlos;
            Disciplina disciplina = new Disciplina(disciplinas[i][0], disciplinas[i][1], engSoftware, professor);
            universidade.adicionarDisciplina(disciplina);
            engSoftware.adicionarDisciplina(disciplina);
            professor.adicionarDisciplina(disciplina);
        }

        universidade.adicionarUsuario(new Aluno("joao", "123", "João Silva", "1001", engSoftware));
        universidade.adicionarUsuario(new Aluno("maria", "123", "Maria Oliveira", "1002", engSoftware));
        universidade.adicionarUsuario(new Aluno("pedro", "123", "Pedro Santos", "1003", engSoftware));

        // Currículo do próximo semestre já gerado e com o período de matrículas aberto.
        var curriculo = secretaria.gerarCurriculo("2027/1", universidade.getDisciplinas());
        secretaria.abrirPeriodoMatriculas(curriculo);
        universidade.setCurriculoAtual(curriculo);

        return universidade;
    }
}
