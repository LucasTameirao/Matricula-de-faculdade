package br.pucminas.matricula;

import br.pucminas.matricula.exceptions.RegraNegocioException;
import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Curriculo;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.StatusDisciplina;
import br.pucminas.matricula.models.Universidade;
import br.pucminas.matricula.models.Usuario;
import br.pucminas.matricula.persistence.ArquivoPersistencia;
import br.pucminas.matricula.services.SistemaCobrancaService;
import br.pucminas.matricula.services.SistemaMatriculas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Bateria de testes automatizados para validar todas as regras de negócio
 * e os requisitos da Sprint 03 (Lab01S03).
 */
public class SistemaMatriculasTest {
    private static int testesExecutados = 0;
    private static int testesAprovados = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("============================================================");
        System.out.println(" INICIANDO TESTES DO SISTEMA DE MATRÍCULAS (Lab01S03)");
        System.out.println("============================================================\n");

        executar("Autenticação válida e inválida", SistemaMatriculasTest::testAutenticacao);
        executar("Limite de 4 disciplinas obrigatórias", SistemaMatriculasTest::testLimiteObrigatorias);
        executar("Limite de 2 disciplinas optativas", SistemaMatriculasTest::testLimiteOptativas);
        executar("Bloqueio de matrícula duplicada", SistemaMatriculasTest::testMatriculaDuplicada);
        executar("Limite máximo de 60 alunos por disciplina", SistemaMatriculasTest::testLimiteMaximo60Alunos);
        executar("Cancelamento de matrícula e liberação de vaga", SistemaMatriculasTest::testCancelamentoMatricula);
        executar("Bloqueio de matrícula fora do período", SistemaMatriculasTest::testPeriodoMatriculasFechado);
        executar("Encerramento do período: ativação com >= 3 alunos e cancelamento com < 3", SistemaMatriculasTest::testEncerramentoPeriodoRegraMinimo3Alunos);
        executar("Notificação ao sistema de cobranças em arquivo CSV", SistemaMatriculasTest::testNotificacaoCobranca);
        executar("Professor consulta suas disciplinas e alunos matriculados", SistemaMatriculasTest::testProfessorConsultaAlunos);
        executar("Secretaria: CRUD de curso, disciplina, professor e aluno", SistemaMatriculasTest::testSecretariaCRUD);
        executar("Persistência em arquivos CSV (salvar e recarregar)", SistemaMatriculasTest::testPersistenciaCsv);

        System.out.println("\n============================================================");
        System.out.printf(" RESULTADO: %d/%d testes aprovados.%n", testesAprovados, testesExecutados);
        System.out.println("============================================================");

        if (testesAprovados < testesExecutados) {
            System.exit(1);
        }
    }

    private static void executar(String nome, Teste runnable) {
        testesExecutados++;
        try {
            runnable.run();
            testesAprovados++;
            System.out.printf("  [OK] %s%n", nome);
        } catch (Throwable t) {
            System.out.printf("  [FALHA] %s: %s%n", nome, t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    interface Teste {
        void run() throws Exception;
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    // ---------- Cenário Base ----------

    private static ContextoTeste criarContexto() throws IOException {
        Path pastaTemp = Files.createTempDirectory("teste_matriculas_");
        Universidade universidade = new Universidade();
        ArquivoPersistencia persistencia = new ArquivoPersistencia(pastaTemp);
        SistemaCobrancaService cobranca = new SistemaCobrancaService(pastaTemp.resolve("cobrancas.csv"));

        Secretaria secretaria = new Secretaria("admin", "123", "Secretária Maria");
        universidade.adicionarUsuario(secretaria);

        Professor professor = new Professor("prof1", "123", "Prof. Carlos");
        universidade.adicionarUsuario(professor);

        Curso curso = new Curso("Engenharia de Software", 240);
        universidade.adicionarCurso(curso);

        Disciplina d1 = new Disciplina("ES101", "Programação Modular", curso, professor);
        Disciplina d2 = new Disciplina("ES102", "Projeto de Software", curso, professor);
        Disciplina d3 = new Disciplina("ES103", "Banco de Dados", curso, professor);
        Disciplina d4 = new Disciplina("ES104", "Engenharia de Requisitos", curso, professor);
        Disciplina d5 = new Disciplina("ES105", "Arquitetura de Software", curso, professor);
        Disciplina d6 = new Disciplina("ES106", "Inteligência Artificial", curso, professor);
        Disciplina d7 = new Disciplina("ES107", "Computação em Nuvem", curso, professor);

        for (Disciplina d : List.of(d1, d2, d3, d4, d5, d6, d7)) {
            universidade.adicionarDisciplina(d);
            curso.adicionarDisciplina(d);
            professor.adicionarDisciplina(d);
        }

        Aluno aluno = new Aluno("aluno1", "123", "João Silva", "1001", curso);
        universidade.adicionarUsuario(aluno);

        SistemaMatriculas sistema = new SistemaMatriculas(universidade, persistencia, cobranca);
        return new ContextoTeste(pastaTemp, universidade, sistema, secretaria, professor, aluno, curso,
                List.of(d1, d2, d3, d4, d5, d6, d7));
    }

    private static void limparContexto(ContextoTeste ctx) {
        try {
            Files.walk(ctx.pastaTemp)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }

    record ContextoTeste(Path pastaTemp, Universidade universidade, SistemaMatriculas sistema,
                         Secretaria secretaria, Professor professor, Aluno aluno, Curso curso,
                         List<Disciplina> disciplinas) {}

    // ---------- Testes ----------

    private static void testAutenticacao() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            Usuario u = ctx.sistema.login("aluno1", "123");
            afirmar(u != null && u.getNome().equals("João Silva"), "Deveria autenticar usuário com senha correta");

            boolean falhou = false;
            try {
                ctx.sistema.login("aluno1", "senha_errada");
            } catch (RegraNegocioException e) {
                falhou = true;
            }
            afirmar(falhou, "Deveria recusar login com senha errada");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testLimiteObrigatorias() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101", "ES102", "ES103", "ES104", "ES105"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", false);
            ctx.sistema.matricular(ctx.aluno, "ES102", false);
            ctx.sistema.matricular(ctx.aluno, "ES103", false);
            ctx.sistema.matricular(ctx.aluno, "ES104", false);

            Matricula m = ctx.aluno.getMatricula("2027/1");
            afirmar(m.contarObrigatorias() == 4, "Aluno deveria ter 4 obrigatórias");

            boolean lancouExcecao = false;
            try {
                ctx.sistema.matricular(ctx.aluno, "ES105", false);
            } catch (RegraNegocioException e) {
                lancouExcecao = true;
            }
            afirmar(lancouExcecao, "Não deve permitir a 5ª disciplina obrigatória (limite de 4)");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testLimiteOptativas() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101", "ES102", "ES103"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", true);
            ctx.sistema.matricular(ctx.aluno, "ES102", true);

            Matricula m = ctx.aluno.getMatricula("2027/1");
            afirmar(m.contarOptativas() == 2, "Aluno deveria ter 2 optativas");

            boolean lancouExcecao = false;
            try {
                ctx.sistema.matricular(ctx.aluno, "ES103", true);
            } catch (RegraNegocioException e) {
                lancouExcecao = true;
            }
            afirmar(lancouExcecao, "Não deve permitir a 3ª disciplina optativa (limite de 2)");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testMatriculaDuplicada() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", false);

            boolean lancouExcecao = false;
            try {
                ctx.sistema.matricular(ctx.aluno, "ES101", false);
            } catch (RegraNegocioException e) {
                lancouExcecao = true;
            }
            afirmar(lancouExcecao, "Não deve permitir matrícula duplicada na mesma disciplina");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testLimiteMaximo60Alunos() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            // Cadastra 60 alunos e matricula
            for (int i = 1; i <= 60; i++) {
                Aluno a = new Aluno("user" + i, "123", "Aluno " + i, "M" + i, ctx.curso);
                ctx.universidade.adicionarUsuario(a);
                ctx.sistema.matricular(a, "ES101", false);
            }

            Disciplina d = ctx.universidade.buscarDisciplina("ES101");
            afirmar(d.isLotada(), "Disciplina com 60 alunos deve estar lotada");

            // 61º aluno deve ser recusado
            Aluno aluno61 = new Aluno("user61", "123", "Aluno 61", "M61", ctx.curso);
            ctx.universidade.adicionarUsuario(aluno61);

            boolean lancouExcecao = false;
            try {
                ctx.sistema.matricular(aluno61, "ES101", false);
            } catch (RegraNegocioException e) {
                lancouExcecao = true;
            }
            afirmar(lancouExcecao, "Não deve permitir o 61º aluno em disciplina de 60 vagas");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testCancelamentoMatricula() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", false);
            afirmar(ctx.universidade.buscarDisciplina("ES101").getAlunosMatriculados().size() == 1, "Disciplina deve ter 1 aluno");

            ctx.sistema.cancelarMatricula(ctx.aluno, "ES101");
            afirmar(ctx.universidade.buscarDisciplina("ES101").getAlunosMatriculados().isEmpty(), "Disciplina deve ficar sem alunos após cancelamento");
            afirmar(ctx.aluno.consultarDisciplinas("2027/1").isEmpty(), "Aluno não deve ter disciplinas ativas");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testPeriodoMatriculasFechado() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101"));
            // Período ainda NÃO aberto

            boolean falhou = false;
            try {
                ctx.sistema.matricular(ctx.aluno, "ES101", false);
            } catch (RegraNegocioException e) {
                falhou = true;
            }
            afirmar(falhou, "Não deve permitir matrícula antes de abrir o período");

            // Abre e encerra o período
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);
            ctx.sistema.encerrarPeriodoMatriculas(ctx.secretaria);

            falhou = false;
            try {
                ctx.sistema.matricular(ctx.aluno, "ES101", false);
            } catch (RegraNegocioException e) {
                falhou = true;
            }
            afirmar(falhou, "Não deve permitir matrícula após encerrar o período");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testEncerramentoPeriodoRegraMinimo3Alunos() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101", "ES102"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            // ES101 terá 3 alunos
            Aluno a1 = new Aluno("u1", "123", "A1", "M1", ctx.curso);
            Aluno a2 = new Aluno("u2", "123", "A2", "M2", ctx.curso);
            Aluno a3 = new Aluno("u3", "123", "A3", "M3", ctx.curso);
            ctx.universidade.adicionarUsuario(a1);
            ctx.universidade.adicionarUsuario(a2);
            ctx.universidade.adicionarUsuario(a3);

            ctx.sistema.matricular(a1, "ES101", false);
            ctx.sistema.matricular(a2, "ES101", false);
            ctx.sistema.matricular(a3, "ES101", false);

            // ES102 terá apenas 2 alunos
            ctx.sistema.matricular(a1, "ES102", false);
            ctx.sistema.matricular(a2, "ES102", false);

            ctx.sistema.encerrarPeriodoMatriculas(ctx.secretaria);

            Disciplina d1 = ctx.universidade.buscarDisciplina("ES101");
            Disciplina d2 = ctx.universidade.buscarDisciplina("ES102");

            afirmar(d1.getStatus() == StatusDisciplina.ATIVA, "ES101 (3 alunos) deve ficar ATIVA");
            afirmar(d2.getStatus() == StatusDisciplina.CANCELADA, "ES102 (2 alunos) deve ser CANCELADA por ter menos de 3 alunos");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testNotificacaoCobranca() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101", "ES102"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", false);

            Path cobrancasPath = ctx.pastaTemp.resolve("cobrancas.csv");
            afirmar(Files.exists(cobrancasPath), "Arquivo cobrancas.csv deve ser criado");

            List<String> linhas = Files.readAllLines(cobrancasPath);
            afirmar(linhas.size() >= 2, "cobrancas.csv deve conter ao menos o cabeçalho e 1 registro");
            afirmar(linhas.get(1).contains("João Silva") && linhas.get(1).contains("ES101"),
                    "Registro de cobrança deve conter o nome do aluno e o código da disciplina");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testProfessorConsultaAlunos() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);

            ctx.sistema.matricular(ctx.aluno, "ES101", false);

            List<Aluno> matriculados = ctx.sistema.consultarAlunos(ctx.professor, "ES101");
            afirmar(matriculados.size() == 1, "Professor deve ver 1 aluno matriculado em ES101");
            afirmar(matriculados.get(0).getMatricula().equals("1001"), "Aluno retornado deve ser João Silva (1001)");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testSecretariaCRUD() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            // Cadastrar curso
            ctx.sistema.cadastrarCurso("Sistemas de Informação", 200);
            afirmar(ctx.universidade.buscarCurso("Sistemas de Informação") != null, "Curso deve ser cadastrado");

            // Cadastrar professor
            ctx.sistema.cadastrarProfessor("prof_novo", "123", "Prof. Novo");
            afirmar(ctx.universidade.buscarProfessor("prof_novo") != null, "Professor deve ser cadastrado");

            // Cadastrar disciplina
            ctx.sistema.cadastrarDisciplina("SI101", "Fundamentos de SI", "Sistemas de Informação", "prof_novo");
            afirmar(ctx.universidade.buscarDisciplina("SI101") != null, "Disciplina deve ser cadastrada");

            // Cadastrar aluno
            ctx.sistema.cadastrarAluno("aluno_novo", "123", "Aluno Novo", "2001", "Sistemas de Informação");
            afirmar(ctx.universidade.buscarAluno("2001") != null, "Aluno deve ser cadastrado");
        } finally {
            limparContexto(ctx);
        }
    }

    private static void testPersistenciaCsv() throws Exception {
        ContextoTeste ctx = criarContexto();
        try {
            ctx.sistema.gerarCurriculo(ctx.secretaria, "2027/1", List.of("ES101", "ES102"));
            ctx.sistema.abrirPeriodoMatriculas(ctx.secretaria);
            ctx.sistema.matricular(ctx.aluno, "ES101", false);
            ctx.sistema.matricular(ctx.aluno, "ES102", true);

            // Recarregar via persistência nova
            ArquivoPersistencia novaPersistencia = new ArquivoPersistencia(ctx.pastaTemp);
            Universidade recarregada = novaPersistencia.carregar();

            afirmar(recarregada != null, "Universidade recarregada não deve ser nula");
            afirmar(recarregada.buscarUsuario("aluno1") != null, "Aluno1 deve existir no banco recarregado");
            Aluno alunoRecarregado = recarregada.buscarAluno("1001");
            afirmar(alunoRecarregado != null, "Aluno 1001 deve ser encontrado");

            Matricula mat = alunoRecarregado.getMatricula("2027/1");
            afirmar(mat != null, "Matrícula 2027/1 deve existir");
            afirmar(mat.contarObrigatorias() == 1, "Deve ter 1 obrigatória persistida");
            afirmar(mat.contarOptativas() == 1, "Deve ter 1 optativa persistida");

            Curriculo curriculoRecarregado = recarregada.getCurriculoAtual();
            afirmar(curriculoRecarregado != null && curriculoRecarregado.getSemestre().equals("2027/1"), "Currículo deve ser 2027/1");
            afirmar(curriculoRecarregado.isPeriodoMatriculasAberto(), "Período deve continuar aberto após recarga");
        } finally {
            limparContexto(ctx);
        }
    }
}
