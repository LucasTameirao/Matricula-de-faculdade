package br.pucminas.matricula.services;

import br.pucminas.matricula.models.Aluno;
import br.pucminas.matricula.models.Disciplina;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public void notificarCobranca(Aluno aluno, List<Disciplina> disciplinasSemestre, String semestre) {
        String disciplinas = disciplinasSemestre.isEmpty()
                ? "(nenhuma)"
                : disciplinasSemestre.stream().map(Disciplina::getNome).collect(Collectors.joining(", "));
        String registro = String.format("[%s] Semestre %s | Aluno: %s (matrícula %s) | %d disciplina(s) a cobrar: %s%n",
                LocalDateTime.now().format(FORMATO_DATA), semestre, aluno.getNome(), aluno.getMatricula(),
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
