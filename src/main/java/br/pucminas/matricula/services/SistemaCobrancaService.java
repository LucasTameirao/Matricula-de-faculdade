package br.pucminas.matricula.services;

import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Simula o sistema de cobranças externo: cada notificação é registrada em um arquivo texto.
 */
public class SistemaCobrancaService {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Path arquivoNotificacoes;

    public SistemaCobrancaService(Path arquivoNotificacoes) {
        this.arquivoNotificacoes = arquivoNotificacoes;
    }

    /** Informa as disciplinas que devem ser cobradas do aluno na matrícula do semestre. */
    public void notificarCobranca(Matricula matricula) {
        var disciplinasSemestre = matricula.getDisciplinas();
        String disciplinas = disciplinasSemestre.isEmpty()
                ? "(nenhuma)"
                : disciplinasSemestre.stream().map(Disciplina::getNome).collect(Collectors.joining(", "));
        String registro = String.format("[%s] Matrícula %s | Aluno: %s (%s) | %d disciplina(s) a cobrar: %s%n",
                LocalDateTime.now().format(FORMATO_DATA), matricula.getCodigoMatricula(),
                matricula.getAluno().getNome(), matricula.getAluno().getMatricula(),
                disciplinasSemestre.size(), disciplinas);
        try {
            Path pasta = arquivoNotificacoes.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            Files.writeString(arquivoNotificacoes, registro, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Falha ao notificar o sistema de cobranças: " + e.getMessage());
        }
    }
}
