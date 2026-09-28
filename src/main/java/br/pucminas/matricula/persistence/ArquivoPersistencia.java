package br.pucminas.matricula.persistence;

import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.AlunoMatricula;
import br.pucminas.matricula.models.Curriculo;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.StatusDisciplina;
import br.pucminas.matricula.models.StatusMatricula;
import br.pucminas.matricula.models.StatusVinculo;
import br.pucminas.matricula.models.Universidade;
import br.pucminas.matricula.models.Usuario;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * "Banco de dados" em CSV: cada tabela é um arquivo dentro da pasta de dados,
 * com uma linha de cabeçalho e um registro por linha.
 */
public class ArquivoPersistencia {
    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    /** Formato gravado pelo Excel ao salvar a planilha (sem segundos). */
    private static final DateTimeFormatter FORMATO_DATA_EXCEL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String SECRETARIAS = "secretarias.csv";
    private static final String PROFESSORES = "professores.csv";
    private static final String CURSOS = "cursos.csv";
    private static final String ALUNOS = "alunos.csv";
    private static final String DISCIPLINAS = "disciplinas.csv";
    private static final String CURRICULO = "curriculo.csv";
    private static final String CURRICULO_DISCIPLINAS = "curriculo_disciplinas.csv";
    private static final String MATRICULAS = "matriculas.csv";
    private static final String ITENS_MATRICULA = "itens_matricula.csv";

    private static final String[] TABELAS = {SECRETARIAS, PROFESSORES, CURSOS, ALUNOS, DISCIPLINAS,
            CURRICULO, CURRICULO_DISCIPLINAS, MATRICULAS, ITENS_MATRICULA};

    private static final String PERIODO_NAO_ABERTO = "NAO_ABERTO";
    private static final String PERIODO_ABERTO = "ABERTO";
    private static final String PERIODO_ENCERRADO = "ENCERRADO";
    private static final String OBRIGATORIA = "OBRIGATORIA";
    private static final String OPTATIVA = "OPTATIVA";

    private final Path pasta;

    public ArquivoPersistencia(Path pasta) {
        this.pasta = pasta;
    }

    // =====================================================================
    // Gravação
    // =====================================================================

    public void salvar(Universidade universidade) {
        List<String> linhas = new ArrayList<>();
        for (Usuario u : universidade.getUsuarios()) {
            if (u instanceof Secretaria s) {
                linhas.add(Csv.linha(s.getLogin(), s.getSenha(), s.getNome()));
            }
        }
        gravar(SECRETARIAS, "login;senha;nome", linhas);

        linhas = new ArrayList<>();
        for (Professor p : universidade.getProfessores()) {
            linhas.add(Csv.linha(p.getLogin(), p.getSenha(), p.getNome()));
        }
        gravar(PROFESSORES, "login;senha;nome", linhas);

        linhas = new ArrayList<>();
        for (Curso c : universidade.getCursos()) {
            linhas.add(Csv.linha(c.getNome(), c.getNumeroCreditos()));
        }
        gravar(CURSOS, "nome;numeroCreditos", linhas);

        linhas = new ArrayList<>();
        for (Aluno a : universidade.getAlunos()) {
            linhas.add(Csv.linha(a.getId(), a.getMatricula(), a.getNome(), a.getLogin(), a.getSenha(),
                    a.getCurso().getNome()));
        }
        gravar(ALUNOS, "id;matricula;nome;login;senha;curso", linhas);

        linhas = new ArrayList<>();
        for (Disciplina d : universidade.getDisciplinas()) {
            linhas.add(Csv.linha(d.getId(), d.getCodigo(), d.getNome(), d.getCurso().getNome(),
                    d.getProfessor().getLogin(), d.getStatus().name()));
        }
        gravar(DISCIPLINAS, "id;codigo;nome;curso;loginProfessor;status", linhas);

        Curriculo curriculo = universidade.getCurriculoAtual();
        linhas = new ArrayList<>();
        List<String> ofertadas = new ArrayList<>();
        if (curriculo != null) {
            String periodo = curriculo.isPeriodoMatriculasAberto() ? PERIODO_ABERTO
                    : curriculo.isPeriodoMatriculasEncerrado() ? PERIODO_ENCERRADO : PERIODO_NAO_ABERTO;
            linhas.add(Csv.linha(curriculo.getSemestre(), periodo));
            for (Disciplina d : curriculo.getDisciplinas()) {
                ofertadas.add(Csv.linha(curriculo.getSemestre(), d.getCodigo()));
            }
        }
        gravar(CURRICULO, "semestre;periodoMatriculas", linhas);
        gravar(CURRICULO_DISCIPLINAS, "semestre;codigoDisciplina", ofertadas);

        linhas = new ArrayList<>();
        List<String> itens = new ArrayList<>();
        for (Aluno a : universidade.getAlunos()) {
            for (Matricula m : a.getMatriculas()) {
                linhas.add(Csv.linha(m.getId(), m.getCodigoMatricula(), a.getMatricula(), m.getSemestre(),
                        m.getDataCriacao().format(FORMATO_DATA), m.getStatus().name()));
                for (AlunoMatricula item : m.getItens()) {
                    itens.add(Csv.linha(m.getId(), a.getMatricula(), item.getDisciplina().getCodigo(),
                            item.isOptativa() ? OPTATIVA : OBRIGATORIA,
                            item.getDataVinculo().format(FORMATO_DATA), item.getStatus().name()));
                }
            }
        }
        gravar(MATRICULAS, "id;codigoMatricula;matriculaAluno;semestre;dataCriacao;status", linhas);
        gravar(ITENS_MATRICULA, "idMatricula;matriculaAluno;codigoDisciplina;tipo;dataVinculo;status", itens);
    }

    private void gravar(String tabela, String cabecalho, List<String> linhas) {
        Csv.gravar(pasta.resolve(tabela), cabecalho, linhas);
    }

    // =====================================================================
    // Leitura
    // =====================================================================

    /**
     * Retorna os dados salvos, ou {@code null} se nenhuma tabela existir ainda.
     * Lança {@link IllegalStateException} se algum arquivo estiver com formato inválido.
     */
    public Universidade carregar() {
        boolean existeAlgumaTabela = false;
        for (String tabela : TABELAS) {
            existeAlgumaTabela |= Files.exists(pasta.resolve(tabela));
        }
        if (!existeAlgumaTabela) {
            return null;
        }

        Universidade universidade = new Universidade();
        long[] maiorId = {0};

        processar(SECRETARIAS, 3, r ->
                universidade.adicionarUsuario(new Secretaria(r.campo(0), r.campo(1), r.campo(2))));

        processar(PROFESSORES, 3, r ->
                universidade.adicionarUsuario(new Professor(r.campo(0), r.campo(1), r.campo(2))));

        processar(CURSOS, 2, r ->
                universidade.adicionarCurso(new Curso(r.campo(0), Integer.parseInt(r.campo(1)))));

        processar(ALUNOS, 6, r -> {
            Aluno aluno = new Aluno(r.campo(3), r.campo(4), r.campo(2), r.campo(1), curso(universidade, r.campo(5)));
            aluno.setId(id(r.campo(0), maiorId));
            universidade.adicionarUsuario(aluno);
        });

        processar(DISCIPLINAS, 6, r -> {
            Curso curso = curso(universidade, r.campo(3));
            Professor professor = universidade.buscarProfessor(r.campo(4));
            if (professor == null) {
                throw new IllegalArgumentException("professor '" + r.campo(4) + "' não encontrado");
            }
            Disciplina disciplina = new Disciplina(r.campo(1), r.campo(2), curso, professor);
            disciplina.setId(id(r.campo(0), maiorId));
            disciplina.restaurarStatus(StatusDisciplina.valueOf(r.campo(5)));
            universidade.adicionarDisciplina(disciplina);
            curso.adicionarDisciplina(disciplina);
            professor.adicionarDisciplina(disciplina);
        });

        processar(CURRICULO, 2, r -> {
            if (universidade.getCurriculoAtual() != null) {
                throw new IllegalArgumentException("só pode existir um currículo atual");
            }
            Curriculo curriculo = new Curriculo(r.campo(0));
            switch (r.campo(1)) {
                case PERIODO_NAO_ABERTO -> curriculo.restaurarPeriodo(false, false);
                case PERIODO_ABERTO -> curriculo.restaurarPeriodo(true, false);
                case PERIODO_ENCERRADO -> curriculo.restaurarPeriodo(false, true);
                default -> throw new IllegalArgumentException("período inválido: " + r.campo(1));
            }
            universidade.setCurriculoAtual(curriculo);
        });

        processar(CURRICULO_DISCIPLINAS, 2, r -> {
            Curriculo curriculo = universidade.getCurriculoAtual();
            if (curriculo == null || !curriculo.getSemestre().equals(r.campo(0))) {
                throw new IllegalArgumentException("semestre '" + r.campo(0) + "' não é o do currículo atual");
            }
            curriculo.adicionarDisciplina(disciplina(universidade, r.campo(1)));
        });

        Map<Long, Matricula> matriculasPorId = new HashMap<>();
        processar(MATRICULAS, 6, r -> {
            Aluno aluno = aluno(universidade, r.campo(2));
            Matricula matricula = new Matricula(id(r.campo(0), maiorId), aluno, r.campo(3),
                    data(r.campo(4)), StatusMatricula.valueOf(r.campo(5)));
            if (aluno.getMatricula(matricula.getSemestre()) != null) {
                throw new IllegalArgumentException("o aluno já tem matrícula no semestre " + matricula.getSemestre());
            }
            aluno.restaurarMatricula(matricula);
            matriculasPorId.put(matricula.getId(), matricula);
        });

        Curriculo curriculo = universidade.getCurriculoAtual();
        processar(ITENS_MATRICULA, 6, r -> {
            Matricula matricula = matriculasPorId.get(Long.parseLong(r.campo(0)));
            if (matricula == null) {
                throw new IllegalArgumentException("matrícula de id " + r.campo(0) + " não encontrada");
            }
            Disciplina disciplina = disciplina(universidade, r.campo(2));
            boolean optativa = switch (r.campo(3)) {
                case OPTATIVA -> true;
                case OBRIGATORIA -> false;
                default -> throw new IllegalArgumentException("tipo inválido: " + r.campo(3));
            };
            AlunoMatricula item = matricula.restaurarItem(disciplina, optativa, data(r.campo(4)),
                    StatusVinculo.valueOf(r.campo(5)));
            // Os inscritos de uma disciplina são os vínculos ativos do semestre do currículo atual.
            boolean ofertaAtual = curriculo != null && curriculo.getSemestre().equals(matricula.getSemestre())
                    && curriculo.contemDisciplina(disciplina);
            if (item.isAtivo() && ofertaAtual) {
                disciplina.restaurarInscricao(item);
            }
        });

        universidade.restaurarUltimoId(maiorId[0]);
        return universidade;
    }

    private record Registro(int linha, List<String> campos) {
        String campo(int i) { return campos.get(i).strip(); }
    }

    /** Lê a tabela (ignorando o cabeçalho e linhas vazias) e aplica a ação a cada registro. */
    private void processar(String tabela, int quantidadeCampos, Consumer<Registro> acao) {
        Path arquivo = pasta.resolve(tabela);
        if (!Files.exists(arquivo)) {
            return;
        }
        List<String> linhas = Csv.lerLinhas(arquivo);
        for (int i = 1; i < linhas.size(); i++) {
            if (linhas.get(i).isBlank()) {
                continue;
            }
            Registro registro = new Registro(i + 1, Csv.dividir(linhas.get(i)));
            if (registro.campos().size() != quantidadeCampos) {
                throw erro(arquivo, registro.linha(), "esperados " + quantidadeCampos
                        + " campos, mas a linha tem " + registro.campos().size());
            }
            try {
                acao.accept(registro);
            } catch (RuntimeException e) {
                throw erro(arquivo, registro.linha(), e.getMessage());
            }
        }
    }

    private static long id(String valor, long[] maiorId) {
        long id = Long.parseLong(valor);
        maiorId[0] = Math.max(maiorId[0], id);
        return id;
    }

    private static LocalDateTime data(String valor) {
        try {
            return LocalDateTime.parse(valor, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(valor, FORMATO_DATA_EXCEL);
        }
    }

    private static Curso curso(Universidade universidade, String nome) {
        Curso curso = universidade.buscarCurso(nome);
        if (curso == null) {
            throw new IllegalArgumentException("curso '" + nome + "' não encontrado");
        }
        return curso;
    }

    private static Aluno aluno(Universidade universidade, String matricula) {
        Aluno aluno = universidade.buscarAluno(matricula);
        if (aluno == null) {
            throw new IllegalArgumentException("aluno com matrícula '" + matricula + "' não encontrado");
        }
        return aluno;
    }

    private static Disciplina disciplina(Universidade universidade, String codigo) {
        Disciplina disciplina = universidade.buscarDisciplina(codigo);
        if (disciplina == null) {
            throw new IllegalArgumentException("disciplina '" + codigo + "' não encontrada");
        }
        return disciplina;
    }

    private static IllegalStateException erro(Path arquivo, int linha, String mensagem) {
        return new IllegalStateException(arquivo + ", linha " + linha + ": " + mensagem);
    }
}
