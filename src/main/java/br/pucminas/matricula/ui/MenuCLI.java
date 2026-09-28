package br.pucminas.matricula.ui;

import br.pucminas.matricula.exceptions.RegraNegocioException;
import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Curriculo;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.AlunoMatricula;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.Usuario;
import br.pucminas.matricula.services.SistemaMatriculas;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Interface de linha de comando do sistema de matrículas.
 */
public class MenuCLI {
    private static final String FORMATO_DISCIPLINA = "%-7s %-34s %-16s %-10s %s%n";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SistemaMatriculas sistema;
    private final Scanner scanner = new Scanner(System.in, charsetDoConsole());

    public MenuCLI(SistemaMatriculas sistema) {
        this.sistema = sistema;
    }

    public void iniciar() {
        titulo("SISTEMA DE MATRÍCULAS - PUC MINAS");
        while (true) {
            System.out.println();
            System.out.println("1 - Entrar");
            System.out.println("0 - Sair");
            switch (lerTexto("Opção: ")) {
                case "1" -> entrar();
                case "0" -> {
                    System.out.println("Até logo!");
                    return;
                }
                default -> opcaoInvalida();
            }
        }
    }

    private void entrar() {
        String login = lerTexto("Login: ");
        String senha = lerTexto("Senha: ");
        Usuario usuario;
        try {
            usuario = sistema.login(login, senha);
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
            return;
        }
        sucesso("Bem-vindo(a), " + usuario.getNome() + " (" + usuario.getTipo() + ")!");
        if (usuario instanceof Aluno aluno) {
            menuAluno(aluno);
        } else if (usuario instanceof Professor professor) {
            menuProfessor(professor);
        } else if (usuario instanceof Secretaria secretaria) {
            menuSecretaria(secretaria);
        }
    }

    // =====================================================================
    // Aluno
    // =====================================================================

    private void menuAluno(Aluno aluno) {
        while (true) {
            titulo("ALUNO: " + aluno.getNome() + " (" + aluno.getMatricula() + ")");
            mostrarSituacaoCurriculo();
            System.out.println("1 - Ver disciplinas ofertadas");
            System.out.println("2 - Matricular em disciplina obrigatória (1ª opção)");
            System.out.println("3 - Matricular em disciplina optativa (alternativa)");
            System.out.println("4 - Cancelar matrícula");
            System.out.println("5 - Minha matrícula do semestre");
            System.out.println("6 - Histórico de matrículas");
            System.out.println("0 - Sair");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarDisciplinas(sistema.getDisciplinasOfertadas());
                case "2" -> matricular(aluno, false);
                case "3" -> matricular(aluno, true);
                case "4" -> executar(() -> {
                    listarMatriculas(aluno);
                    if (aluno.consultarDisciplinas(sistema.getSemestreAtual()).isEmpty()) return;
                    sistema.cancelarMatricula(aluno, lerTexto("Código da disciplina a cancelar: "));
                    sucesso("Matrícula cancelada. Sistema de cobranças notificado.");
                });
                case "5" -> listarMatriculas(aluno);
                case "6" -> listarHistorico(aluno);
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void matricular(Aluno aluno, boolean optativa) {
        executar(() -> {
            listarDisciplinas(sistema.getDisciplinasOfertadas());
            sistema.matricular(aluno, lerTexto("Código da disciplina: "), optativa);
            sucesso("Matrícula realizada com sucesso! Sistema de cobranças notificado.");
        });
    }

    private void listarMatriculas(Aluno aluno) {
        String semestre = sistema.getSemestreAtual();
        Matricula matricula = semestre == null ? null : aluno.getMatricula(semestre);
        if (matricula == null) {
            System.out.println("\nVocê ainda não possui matrícula no semestre atual.");
            return;
        }
        System.out.printf("%nMatrícula %s (criada em %s) - %s%n", matricula.getCodigoMatricula(),
                matricula.getDataCriacao().format(FORMATO_DATA), matricula.getStatus().getDescricao());
        System.out.printf("Obrigatórias: %d/%d | Optativas: %d/%d%n",
                matricula.contarObrigatorias(), Matricula.MAX_OBRIGATORIAS,
                matricula.contarOptativas(), Matricula.MAX_OPTATIVAS);
        List<AlunoMatricula> itens = matricula.getItensAtivos();
        if (itens.isEmpty()) {
            System.out.println("Você não está matriculado(a) em nenhuma disciplina.");
            return;
        }
        System.out.printf("%-7s %-34s %-12s %-17s %s%n", "Código", "Disciplina", "Tipo", "Vinculado em", "Situação");
        for (AlunoMatricula item : itens) {
            Disciplina d = item.getDisciplina();
            System.out.printf("%-7s %-34s %-12s %-17s %s%n", d.getCodigo(), d.getNome(),
                    item.isOptativa() ? "Optativa" : "Obrigatória",
                    item.getDataVinculo().format(FORMATO_DATA), d.getSituacao());
        }
    }

    private void listarHistorico(Aluno aluno) {
        if (aluno.getMatriculas().isEmpty()) {
            System.out.println("\nNenhuma matrícula registrada.");
            return;
        }
        for (Matricula m : aluno.getMatriculas()) {
            System.out.printf("%nMatrícula %s - %s%n", m.getCodigoMatricula(), m.getStatus().getDescricao());
            for (AlunoMatricula item : m.getItens()) {
                System.out.printf("  %-7s %-34s %-12s %s%n", item.getDisciplina().getCodigo(),
                        item.getDisciplina().getNome(), item.isOptativa() ? "Optativa" : "Obrigatória",
                        item.getStatus().getDescricao());
            }
        }
    }

    // =====================================================================
    // Professor
    // =====================================================================

    private void menuProfessor(Professor professor) {
        while (true) {
            titulo("PROFESSOR: " + professor.getNome());
            System.out.println("1 - Minhas disciplinas");
            System.out.println("2 - Alunos matriculados em uma disciplina");
            System.out.println("0 - Sair");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarDisciplinas(professor.consultarDisciplinas());
                case "2" -> executar(() -> {
                    listarDisciplinas(professor.consultarDisciplinas());
                    if (professor.consultarDisciplinas().isEmpty()) return;
                    String codigo = lerTexto("Código da disciplina: ");
                    listarAlunosDaDisciplina(sistema.consultarAlunos(professor, codigo), codigo);
                });
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void listarAlunosDaDisciplina(List<Aluno> alunos, String codigo) {
        System.out.printf("%nAlunos matriculados em %s: %d%n", codigo.toUpperCase(), alunos.size());
        if (alunos.isEmpty()) return;
        System.out.printf("%-10s %-30s %s%n", "Matrícula", "Nome", "Curso");
        for (Aluno a : alunos) {
            System.out.printf("%-10s %-30s %s%n", a.getMatricula(), a.getNome(), a.getCurso().getNome());
        }
    }

    // =====================================================================
    // Secretaria
    // =====================================================================

    private void menuSecretaria(Secretaria secretaria) {
        while (true) {
            titulo("SECRETARIA: " + secretaria.getNome());
            mostrarSituacaoCurriculo();
            System.out.println("1 - Cursos");
            System.out.println("2 - Disciplinas");
            System.out.println("3 - Professores");
            System.out.println("4 - Alunos");
            System.out.println("5 - Gerar currículo do semestre");
            System.out.println("6 - Abrir período de matrículas");
            System.out.println("7 - Encerrar período de matrículas");
            System.out.println("8 - Ver currículo atual");
            System.out.println("0 - Sair");
            switch (lerTexto("Opção: ")) {
                case "1" -> menuCursos();
                case "2" -> menuDisciplinas();
                case "3" -> menuProfessores();
                case "4" -> menuAlunos();
                case "5" -> executar(() -> gerarCurriculo(secretaria));
                case "6" -> executar(() -> {
                    sistema.abrirPeriodoMatriculas(secretaria);
                    sucesso("Período de matrículas aberto.");
                });
                case "7" -> executar(() -> {
                    Curriculo curriculo = sistema.encerrarPeriodoMatriculas(secretaria);
                    sucesso("Período de matrículas encerrado. Resultado (mínimo de "
                            + Disciplina.MIN_ALUNOS + " alunos para a disciplina ocorrer):");
                    listarDisciplinas(curriculo.getDisciplinas());
                });
                case "8" -> verCurriculo();
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void gerarCurriculo(Secretaria secretaria) {
        listarDisciplinas(sistema.getDisciplinas());
        String semestre = lerTexto("Semestre (ex.: 2027/2): ");
        String codigos = lerTexto("Códigos das disciplinas separados por vírgula (ou TODAS): ");
        List<String> lista = codigos.equalsIgnoreCase("todas")
                ? sistema.getDisciplinas().stream().map(Disciplina::getCodigo).toList()
                : Arrays.stream(codigos.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        System.out.println("Atenção: as matrículas do semestre anterior serão descartadas.");
        if (!lerTexto("Confirmar? (s/n): ").equalsIgnoreCase("s")) {
            System.out.println("Operação cancelada.");
            return;
        }
        Curriculo curriculo = sistema.gerarCurriculo(secretaria, semestre, lista);
        sucesso("Currículo " + curriculo.getSemestre() + " gerado com " + curriculo.getDisciplinas().size()
                + " disciplina(s). Abra o período de matrículas para os alunos se inscreverem.");
    }

    private void verCurriculo() {
        Curriculo curriculo = sistema.getCurriculoAtual();
        if (curriculo == null) {
            System.out.println("Nenhum currículo gerado.");
            return;
        }
        System.out.println("\nCurrículo " + curriculo.getSemestre() + " - " + curriculo.getSituacaoPeriodo());
        listarDisciplinas(curriculo.getDisciplinas());
    }

    private void menuCursos() {
        while (true) {
            titulo("CURSOS");
            System.out.println("1 - Listar  2 - Cadastrar  3 - Remover  0 - Voltar");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarCursos();
                case "2" -> executar(() -> {
                    String nome = lerTexto("Nome: ");
                    int creditos = lerInteiro("Número de créditos: ");
                    sistema.cadastrarCurso(nome, creditos);
                    sucesso("Curso cadastrado.");
                });
                case "3" -> executar(() -> {
                    listarCursos();
                    sistema.removerCurso(lerTexto("Nome do curso a remover: "));
                    sucesso("Curso removido.");
                });
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void menuDisciplinas() {
        while (true) {
            titulo("DISCIPLINAS");
            System.out.println("1 - Listar  2 - Cadastrar  3 - Trocar professor  4 - Remover  0 - Voltar");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarDisciplinas(sistema.getDisciplinas());
                case "2" -> executar(() -> {
                    String codigo = lerTexto("Código (ex.: ES108): ");
                    String nome = lerTexto("Nome: ");
                    listarCursos();
                    String curso = lerTexto("Nome do curso: ");
                    listarProfessores();
                    String professor = lerTexto("Login do professor: ");
                    sistema.cadastrarDisciplina(codigo, nome, curso, professor);
                    sucesso("Disciplina cadastrada. Inclua-a em um currículo para ofertá-la.");
                });
                case "3" -> executar(() -> {
                    listarDisciplinas(sistema.getDisciplinas());
                    String codigo = lerTexto("Código da disciplina: ");
                    listarProfessores();
                    sistema.alterarProfessorDisciplina(codigo, lerTexto("Login do novo professor: "));
                    sucesso("Professor alterado.");
                });
                case "4" -> executar(() -> {
                    listarDisciplinas(sistema.getDisciplinas());
                    sistema.removerDisciplina(lerTexto("Código da disciplina a remover: "));
                    sucesso("Disciplina removida.");
                });
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void menuProfessores() {
        while (true) {
            titulo("PROFESSORES");
            System.out.println("1 - Listar  2 - Cadastrar  3 - Remover  0 - Voltar");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarProfessores();
                case "2" -> executar(() -> {
                    String nome = lerTexto("Nome: ");
                    String login = lerTexto("Login: ");
                    String senha = lerTexto("Senha: ");
                    sistema.cadastrarProfessor(login, senha, nome);
                    sucesso("Professor cadastrado.");
                });
                case "3" -> executar(() -> {
                    listarProfessores();
                    sistema.removerProfessor(lerTexto("Login do professor a remover: "));
                    sucesso("Professor removido.");
                });
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    private void menuAlunos() {
        while (true) {
            titulo("ALUNOS");
            System.out.println("1 - Listar  2 - Cadastrar  3 - Remover  0 - Voltar");
            switch (lerTexto("Opção: ")) {
                case "1" -> listarAlunos();
                case "2" -> executar(() -> {
                    String nome = lerTexto("Nome: ");
                    String matricula = lerTexto("Matrícula: ");
                    listarCursos();
                    String curso = lerTexto("Nome do curso: ");
                    String login = lerTexto("Login: ");
                    String senha = lerTexto("Senha: ");
                    sistema.cadastrarAluno(login, senha, nome, matricula, curso);
                    sucesso("Aluno cadastrado.");
                });
                case "3" -> executar(() -> {
                    listarAlunos();
                    sistema.removerAluno(lerTexto("Matrícula do aluno a remover: "));
                    sucesso("Aluno removido.");
                });
                case "0" -> { return; }
                default -> opcaoInvalida();
            }
        }
    }

    // =====================================================================
    // Listagens
    // =====================================================================

    private void listarDisciplinas(List<Disciplina> disciplinas) {
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina.");
            return;
        }
        System.out.println();
        System.out.printf(FORMATO_DISCIPLINA, "Código", "Disciplina", "Professor", "Inscritos", "Situação");
        for (Disciplina d : disciplinas) {
            System.out.printf(FORMATO_DISCIPLINA, d.getCodigo(), d.getNome(), d.getProfessor().getNome(),
                    d.getAlunosMatriculados().size() + "/" + Disciplina.LIMITE_ALUNOS, d.getSituacao());
        }
    }

    private void listarCursos() {
        if (sistema.getCursos().isEmpty()) {
            System.out.println("Nenhum curso cadastrado.");
            return;
        }
        System.out.printf("%n%-34s %-9s %s%n", "Curso", "Créditos", "Disciplinas");
        for (Curso c : sistema.getCursos()) {
            System.out.printf("%-34s %-9d %d%n", c.getNome(), c.getNumeroCreditos(), c.getDisciplinas().size());
        }
    }

    private void listarProfessores() {
        if (sistema.getProfessores().isEmpty()) {
            System.out.println("Nenhum professor cadastrado.");
            return;
        }
        System.out.printf("%n%-12s %-30s %s%n", "Login", "Nome", "Disciplinas");
        for (Professor p : sistema.getProfessores()) {
            System.out.printf("%-12s %-30s %d%n", p.getLogin(), p.getNome(), p.consultarDisciplinas().size());
        }
    }

    private void listarAlunos() {
        if (sistema.getAlunos().isEmpty()) {
            System.out.println("Nenhum aluno cadastrado.");
            return;
        }
        System.out.printf("%n%-10s %-25s %-12s %-26s %s%n", "Matrícula", "Nome", "Login", "Curso", "Disciplinas no semestre");
        for (Aluno a : sistema.getAlunos()) {
            System.out.printf("%-10s %-25s %-12s %-26s %d%n", a.getMatricula(), a.getNome(), a.getLogin(),
                    a.getCurso().getNome(), a.consultarDisciplinas(sistema.getSemestreAtual()).size());
        }
    }

    private void mostrarSituacaoCurriculo() {
        Curriculo curriculo = sistema.getCurriculoAtual();
        if (curriculo == null) {
            System.out.println("[Nenhum currículo gerado]");
        } else {
            System.out.println("[Semestre " + curriculo.getSemestre() + " - " + curriculo.getSituacaoPeriodo() + "]");
        }
    }

    // =====================================================================
    // Entrada e saída
    // =====================================================================

    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    /**
     * Codificação usada pelo terminal. No Windows o console não usa UTF-8 (ex.: CP850),
     * e ler a entrada com a codificação errada corrompe acentos como em "Informação".
     */
    private static Charset charsetDoConsole() {
        for (String propriedade : new String[]{"stdin.encoding", "stdout.encoding", "sun.stdout.encoding"}) {
            String nome = System.getProperty(propriedade);
            if (nome != null) {
                try {
                    return Charset.forName(nome);
                } catch (RuntimeException e) {
                    // Codificação desconhecida: tenta a próxima.
                }
            }
        }
        return Charset.defaultCharset();
    }

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        if (!scanner.hasNextLine()) {
            System.out.println("\nEntrada encerrada. Até logo!");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private int lerInteiro(String rotulo) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(rotulo));
            } catch (NumberFormatException e) {
                erro("Digite um número inteiro.");
            }
        }
    }

    private void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println(" " + texto);
        System.out.println("=".repeat(60));
    }

    private void sucesso(String mensagem) {
        System.out.println(">> " + mensagem);
    }

    private void erro(String mensagem) {
        System.out.println("!! " + mensagem);
    }

    private void opcaoInvalida() {
        erro("Opção inválida.");
    }
}
