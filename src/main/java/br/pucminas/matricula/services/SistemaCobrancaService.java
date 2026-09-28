package br.pucminas.matricula.services;

import br.pucminas.matricula.models.Disciplina;
import br.pucminas.matricula.models.Matricula;
import br.pucminas.matricula.persistence.ArquivoPersistencia;
import br.pucminas.matricula.persistence.Csv;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simula o sistema de cobranças externo: cada notificação é registrada em um arquivo CSV.
 */
public class SistemaCobrancaService {
    private static final String CABECALHO = "dataHora;codigoMatricula;matriculaAluno;aluno;quantidadeDisciplinas;disciplinas";

    private final Path arquivoNotificacoes;

    public SistemaCobrancaService(Path arquivoNotificacoes) {
        this.arquivoNotificacoes = arquivoNotificacoes;
    }

    /** Informa as disciplinas que devem ser cobradas do aluno na matrícula do semestre. */
    public void notificarCobranca(Matricula matricula) {
        List<Disciplina> disciplinasSemestre = matricula.getDisciplinas();
        String disciplinas = disciplinasSemestre.stream().map(Disciplina::getCodigo).collect(Collectors.joining(", "));
        String registro = Csv.linha(LocalDateTime.now().format(ArquivoPersistencia.FORMATO_DATA),
                matricula.getCodigoMatricula(), matricula.getAluno().getMatricula(), matricula.getAluno().getNome(),
                disciplinasSemestre.size(), disciplinas);
        try {
            Csv.acrescentar(arquivoNotificacoes, CABECALHO, registro);
        } catch (UncheckedIOException e) {
            System.err.println("Falha ao notificar o sistema de cobranças: " + e.getMessage());
        }
    }
}
