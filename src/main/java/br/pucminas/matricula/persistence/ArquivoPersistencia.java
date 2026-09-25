package br.pucminas.matricula.persistence;

import br.pucminas.matricula.models.Universidade;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Persiste todos os dados do sistema em um arquivo binário via serialização Java.
 */
public class ArquivoPersistencia {
    private final Path arquivo;

    public ArquivoPersistencia(Path arquivo) {
        this.arquivo = arquivo;
    }

    /** Retorna os dados salvos, ou {@code null} se ainda não existir um arquivo válido. */
    public Universidade carregar() {
        if (!Files.exists(arquivo)) {
            return null;
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(arquivo))) {
            return (Universidade) entrada.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Não foi possível ler " + arquivo + " (" + e.getMessage() + "). Iniciando com dados padrão.");
            return null;
        }
    }

    public void salvar(Universidade universidade) {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            // Grava em arquivo temporário e substitui, para não corromper os dados em caso de falha.
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(temporario))) {
                saida.writeObject(universidade);
            }
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar os dados em " + arquivo, e);
        }
    }
}
