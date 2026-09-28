package br.pucminas.matricula.persistence;

import br.pucminas.matricula.exceptions.RegraNegocioException;
import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Curriculo;
import br.pucminas.matricula.models.Curso;
import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;
import br.pucminas.matricula.models.Professor;
import br.pucminas.matricula.models.Secretaria;
import br.pucminas.matricula.models.StatusMatricula;
import br.pucminas.matricula.models.StatusVinculo;
import br.pucminas.matricula.models.Universidade;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gera o banco de dados de exemplo quando a pasta de dados não existe.
 * <ul>
 *   <li>Semestre 2026/2: já concluído, disponível no histórico dos alunos;</li>
 *   <li>Semestre 2027/1: currículo atual, com o período de matrículas aberto e alunos já matriculados.
 *       ES101 está lotada (60 alunos), CC102 e EC102 têm menos de 3 alunos e SI103 não tem nenhum.</li>
 * </ul>
 */
public final class DadosIniciais {

    private static final String SENHA_PADRAO = "123";
    private static final String SEMESTRE_ANTERIOR = "2026/2";
    private static final String SEMESTRE_ATUAL = "2027/1";

    private static final String[] PRIMEIROS_NOMES = {
            "João", "Maria", "Pedro", "Ana Clara", "Lucas", "Beatriz", "Gabriel", "Larissa", "Rafael", "Camila",
            "Mateus", "Juliana", "Felipe", "Mariana", "Gustavo", "Letícia", "Bruno", "Isabela", "Thiago", "Carolina",
            "Vinícius", "Amanda", "Leonardo", "Fernanda", "Diego", "Natália", "Rodrigo", "Bianca", "Eduardo",
            "Vitória", "André", "Luana", "Henrique", "Débora", "Caio", "Sofia"};
    private static final String[] SOBRENOMES = {
            "Silva", "Oliveira", "Santos", "Souza", "Costa", "Pereira", "Almeida", "Ferreira", "Rodrigues", "Gomes",
            "Martins", "Araújo", "Ribeiro", "Carvalho", "Lopes", "Barbosa", "Rocha", "Dias", "Moreira", "Nunes",
            "Cardoso", "Teixeira", "Correia", "Mendes"};
    private static final int TOTAL_ALUNOS = 72;
    /** Os primeiros alunos cursaram o semestre anterior e têm histórico. */
    private static final int ALUNOS_COM_HISTORICO = 48;

    /** Disciplinas com poucos inscritos (ficam de fora da distribuição automática). */
    private static final List<String> POUCOS_INSCRITOS = List.of("CC102", "EC102", "SI103");
    private static final List<String> NAO_OFERTADAS = List.of("ES106", "CC104", "EC103");

    private DadosIniciais() {
    }

    public static Universidade criar() {
        Universidade universidade = new Universidade();

        Secretaria secretaria = new Secretaria("secretaria", SENHA_PADRAO, "Secretaria Acadêmica");
        universidade.adicionarUsuario(secretaria);
        universidade.adicionarUsuario(new Secretaria("coordenacao", SENHA_PADRAO, "Coordenação de Graduação"));

        Curso es = curso(universidade, "Engenharia de Software", 240);
        Curso cc = curso(universidade, "Ciência da Computação", 240);
        Curso si = curso(universidade, "Sistemas de Informação", 200);
        Curso ec = curso(universidade, "Engenharia de Computação", 260);

        Map<String, Professor> professores = Map.of(
                "ana", professor(universidade, "ana", "Ana Souza"),
                "carlos", professor(universidade, "carlos", "Carlos Lima"),
                "fernanda", professor(universidade, "fernanda", "Fernanda Rocha"),
                "ricardo", professor(universidade, "ricardo", "Ricardo Mendes"),
                "juliana", professor(universidade, "juliana", "Juliana Castro"),
                "marcos", professor(universidade, "marcos", "Marcos Pereira"),
                "patricia", professor(universidade, "patricia", "Patrícia Gomes"),
                "roberto", professor(universidade, "roberto", "Roberto Alves"));

        Object[][] disciplinas = {
                {"ES101", "Algoritmos e Estruturas de Dados", es, "ana"},
                {"ES102", "Projeto de Software", es, "carlos"},
                {"ES103", "Banco de Dados", es, "fernanda"},
                {"ES104", "Cálculo I", es, "ricardo"},
                {"ES105", "Arquitetura de Computadores", es, "marcos"},
                {"ES106", "Empreendedorismo", es, "patricia"},
                {"ES107", "Inglês Instrumental", es, "juliana"},
                {"CC101", "Programação Orientada a Objetos", cc, "ana"},
                {"CC102", "Teoria da Computação", cc, "roberto"},
                {"CC103", "Inteligência Artificial", cc, "fernanda"},
                {"CC104", "Computação Gráfica", cc, "marcos"},
                {"SI101", "Lógica de Programação", si, "juliana"},
                {"SI102", "Gestão de Projetos", si, "patricia"},
                {"SI103", "Sistemas de Apoio à Decisão", si, "carlos"},
                {"EC101", "Circuitos Digitais", ec, "roberto"},
                {"EC102", "Sistemas Embarcados", ec, "marcos"},
                {"EC103", "Redes de Computadores", ec, "ricardo"},
        };
        for (Object[] d : disciplinas) {
            Professor professor = professores.get((String) d[3]);
            Curso curso = (Curso) d[2];
            Disciplina disciplina = new Disciplina((String) d[0], (String) d[1], curso, professor);
            universidade.adicionarDisciplina(disciplina);
            curso.adicionarDisciplina(disciplina);
            professor.adicionarDisciplina(disciplina);
        }

        Curso[] cursos = {es, cc, si, ec};
        for (int i = 0; i < TOTAL_ALUNOS; i++) {
            String primeiroNome = PRIMEIROS_NOMES[i % PRIMEIROS_NOMES.length];
            String sobrenome = SOBRENOMES[i % SOBRENOMES.length];
            String login = i < 3 ? normalizar(primeiroNome) : normalizar(primeiroNome.split(" ")[0]) + "." + normalizar(sobrenome);
            Curso curso = i < 3 ? es : cursos[i % cursos.length];
            universidade.adicionarUsuario(new Aluno(login, SENHA_PADRAO, primeiroNome + " " + sobrenome,
                    String.valueOf(1001 + i), curso));
        }

        criarHistorico(universidade);
        criarSemestreAtual(universidade, secretaria);
        return universidade;
    }

    /** Semestre anterior já concluído (o registro fica apenas no histórico de matrículas). */
    private static void criarHistorico(Universidade universidade) {
        List<Aluno> alunos = universidade.getAlunos();
        List<Disciplina> todas = universidade.getDisciplinas();
        for (int i = 0; i < ALUNOS_COM_HISTORICO; i++) {
            Aluno aluno = alunos.get(i);
            LocalDateTime data = LocalDateTime.of(2026, 7, 1 + i % 20, 9 + i % 8, (i * 7) % 60, 0);
            Matricula matricula = new Matricula(universidade.gerarId(), aluno, SEMESTRE_ANTERIOR, data,
                    StatusMatricula.CONCLUIDA);
            aluno.restaurarMatricula(matricula);

            List<Disciplina> doCurso = new ArrayList<>(aluno.getCurso().getDisciplinas());
            for (Disciplina d : todas) {
                if (!doCurso.contains(d)) {
                    doCurso.add(d); // completa com disciplinas de outros cursos quando o curso tem poucas
                }
            }
            for (int j = 0; j < 4; j++) {
                Disciplina d = doCurso.get((i + j) % doCurso.size());
                // Alguns alunos cancelaram uma disciplina durante aquele período.
                StatusVinculo status = (i % 5 == 0 && j == 1) ? StatusVinculo.CANCELADO : StatusVinculo.ATIVO;
                matricula.restaurarItem(d, false, data.plusMinutes(j), status);
            }
            if (i % 2 == 0) {
                Disciplina optativa = todas.get((i * 3 + 5) % todas.size());
                if (matricula.buscarItemAtivo(optativa) == null) {
                    matricula.restaurarItem(optativa, true, data.plusMinutes(5), StatusVinculo.ATIVO);
                }
            }
        }
    }

    /** Currículo atual com o período aberto; as matrículas passam pelas regras do domínio. */
    private static void criarSemestreAtual(Universidade universidade, Secretaria secretaria) {
        List<Disciplina> ofertadas = universidade.getDisciplinas().stream()
                .filter(d -> !NAO_OFERTADAS.contains(d.getCodigo()))
                .toList();
        universidade.getDisciplinas().forEach(Disciplina::retirarDeOferta);
        Curriculo curriculo = secretaria.gerarCurriculo(SEMESTRE_ATUAL, ofertadas);
        secretaria.abrirPeriodoMatriculas(curriculo);
        universidade.setCurriculoAtual(curriculo);

        List<Disciplina> distribuiveis = ofertadas.stream()
                .filter(d -> !POUCOS_INSCRITOS.contains(d.getCodigo()) && !d.getCodigo().equals("ES101"))
                .toList();
        List<Aluno> alunos = universidade.getAlunos();

        // João (1001) não tem matrícula no semestre atual, para ser usado na demonstração.
        // Maria (1002) tem matrícula parcial; Pedro (1003) já preencheu todas as vagas.
        matricular(universidade, alunos.get(1), false, "ES102", "ES103");
        matricular(universidade, alunos.get(2), false, "ES101", "ES102", "ES103", "ES104");
        matricular(universidade, alunos.get(2), true, "CC103", "SI102");

        for (int i = 3; i < alunos.size(); i++) {
            Aluno aluno = alunos.get(i);
            int obrigatorias = 3 + i % 2;
            List<String> escolhidas = new ArrayList<>();
            if (i <= 61) {
                escolhidas.add("ES101"); // ES101 chega a 60 alunos e fica lotada
            }
            List<Disciplina> doCurso = distribuiveis.stream().filter(d -> d.getCurso() == aluno.getCurso()).toList();
            for (int j = 0; escolhidas.size() < obrigatorias && j < doCurso.size(); j++) {
                escolhidas.add(doCurso.get((i + j) % doCurso.size()).getCodigo());
            }
            for (int j = 0; escolhidas.size() < obrigatorias && j < distribuiveis.size(); j++) {
                String codigo = distribuiveis.get((i + j) % distribuiveis.size()).getCodigo();
                if (!escolhidas.contains(codigo)) {
                    escolhidas.add(codigo);
                }
            }
            matricular(universidade, aluno, false, escolhidas.toArray(String[]::new));

            List<String> optativas = new ArrayList<>();
            for (int j = 0; optativas.size() < i % 3 && j < distribuiveis.size(); j++) {
                String codigo = distribuiveis.get((i * 3 + j) % distribuiveis.size()).getCodigo();
                if (!escolhidas.contains(codigo)) {
                    optativas.add(codigo);
                }
            }
            matricular(universidade, aluno, true, optativas.toArray(String[]::new));
        }

        // Disciplinas com menos de 3 alunos: serão canceladas se o período for encerrado assim.
        matricular(universidade, alunos.get(10), true, "CC102");
        matricular(universidade, alunos.get(25), true, "CC102");
        matricular(universidade, alunos.get(40), true, "EC102");
    }

    private static void matricular(Universidade universidade, Aluno aluno, boolean optativa, String... codigos) {
        if (codigos.length == 0) {
            return;
        }
        if (aluno.getMatricula(SEMESTRE_ATUAL) == null) {
            aluno.iniciarMatricula(universidade.gerarId(), SEMESTRE_ATUAL);
        }
        for (String codigo : codigos) {
            try {
                aluno.matricular(SEMESTRE_ATUAL, universidade.buscarDisciplina(codigo), optativa);
            } catch (RegraNegocioException e) {
                // Vaga esgotada ou limite do aluno atingido: segue com as próximas disciplinas.
            }
        }
    }

    private static Curso curso(Universidade universidade, String nome, int creditos) {
        Curso curso = new Curso(nome, creditos);
        universidade.adicionarCurso(curso);
        return curso;
    }

    private static Professor professor(Universidade universidade, String login, String nome) {
        Professor professor = new Professor(login, SENHA_PADRAO, nome);
        universidade.adicionarUsuario(professor);
        return professor;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
