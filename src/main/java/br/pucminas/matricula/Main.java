package br.pucminas.matricula;

import br.pucminas.matricula.models.Universidade;
import br.pucminas.matricula.persistence.ArquivoPersistencia;
import br.pucminas.matricula.persistence.DadosIniciais;
import br.pucminas.matricula.services.SistemaCobrancaService;
import br.pucminas.matricula.services.SistemaMatriculas;
import br.pucminas.matricula.ui.MenuCLI;

import java.nio.file.Path;

public class Main {
    private static final Path PASTA_DADOS = Path.of("dados");

    public static void main(String[] args) {
        ArquivoPersistencia persistencia = new ArquivoPersistencia(PASTA_DADOS);
        Universidade universidade;
        try {
            universidade = persistencia.carregar();
        } catch (RuntimeException e) {
            // Não sobrescreve os arquivos: o usuário corrige o CSV ou apaga a pasta de dados.
            System.err.println("Não foi possível carregar os dados: " + e.getMessage());
            System.err.println("Corrija o arquivo indicado ou apague a pasta '" + PASTA_DADOS
                    + "' para recriar os dados de exemplo.");
            System.exit(1);
            return;
        }
        if (universidade == null) {
            universidade = DadosIniciais.criar();
            persistencia.salvar(universidade);
            System.out.println("Banco de dados CSV criado em '" + PASTA_DADOS.toAbsolutePath() + "'.");
        }

        SistemaCobrancaService cobranca = new SistemaCobrancaService(PASTA_DADOS.resolve("cobrancas.csv"));
        SistemaMatriculas sistema = new SistemaMatriculas(universidade, persistencia, cobranca);
        new MenuCLI(sistema).iniciar();
    }
}
