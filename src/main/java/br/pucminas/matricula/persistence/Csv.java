package br.pucminas.matricula.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Leitura e escrita de arquivos CSV no formato aceito pelo Excel em português:
 * separador ';', campos com caracteres especiais entre aspas e codificação UTF-8 com BOM.
 */
public final class Csv {
    public static final char SEPARADOR = ';';
    /** Mesma quebra de linha em qualquer sistema, para o Git não acusar o arquivo inteiro como alterado. */
    private static final String QUEBRA_LINHA = "\n";
    private static final String BOM = "﻿";

    private Csv() {
    }

    /** Monta uma linha CSV a partir dos campos. */
    public static String linha(Object... campos) {
        StringBuilder linha = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                linha.append(SEPARADOR);
            }
            linha.append(formatarCampo(String.valueOf(campos[i])));
        }
        return linha.toString();
    }

    private static String formatarCampo(String valor) {
        String semQuebras = valor.replace("\r", "").replace('\n', ' ');
        boolean precisaAspas = semQuebras.indexOf(SEPARADOR) >= 0 || semQuebras.indexOf('"') >= 0
                || !semQuebras.equals(semQuebras.strip());
        return precisaAspas ? '"' + semQuebras.replace("\"", "\"\"") + '"' : semQuebras;
    }

    /** Divide uma linha CSV em campos, respeitando campos entre aspas. */
    public static List<String> dividir(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (entreAspas) {
                if (c == '"' && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else if (c == '"') {
                    entreAspas = false;
                } else {
                    atual.append(c);
                }
            } else if (c == '"') {
                entreAspas = true;
            } else if (c == SEPARADOR) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }

    /** Lê todas as linhas do arquivo (incluindo o cabeçalho), sem o BOM. */
    public static List<String> lerLinhas(Path arquivo) {
        try {
            List<String> linhas = new ArrayList<>(Files.readAllLines(arquivo, StandardCharsets.UTF_8));
            if (!linhas.isEmpty() && linhas.get(0).startsWith(BOM)) {
                linhas.set(0, linhas.get(0).substring(1));
            }
            return linhas;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
    }

    /** Substitui o conteúdo do arquivo pelo cabeçalho e as linhas informadas. */
    public static void gravar(Path arquivo, String cabecalho, List<String> linhas) {
        StringBuilder conteudo = new StringBuilder(BOM).append(cabecalho).append(QUEBRA_LINHA);
        for (String linha : linhas) {
            conteudo.append(linha).append(QUEBRA_LINHA);
        }
        try {
            criarPasta(arquivo);
            // Grava em arquivo temporário e substitui, para não corromper os dados em caso de falha.
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            Files.writeString(temporario, conteudo, StandardCharsets.UTF_8);
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar " + arquivo, e);
        }
    }

    /** Acrescenta uma linha ao fim do arquivo, criando-o com o cabeçalho se ainda não existir. */
    public static void acrescentar(Path arquivo, String cabecalho, String linha) {
        try {
            criarPasta(arquivo);
            if (!Files.exists(arquivo)) {
                Files.writeString(arquivo, BOM + cabecalho + QUEBRA_LINHA, StandardCharsets.UTF_8);
            }
            Files.writeString(arquivo, linha + QUEBRA_LINHA, StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar em " + arquivo, e);
        }
    }

    private static void criarPasta(Path arquivo) throws IOException {
        Path pasta = arquivo.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
    }
}
