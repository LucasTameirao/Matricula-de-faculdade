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
        ArquivoPersistencia persistencia = new ArquivoPersistencia(PASTA_DADOS.resolve("universidade.dat"));
        Universidade universidade = persistencia.carregar();
        if (universidade == null) {
            universidade = DadosIniciais.criar();
            persistencia.salvar(universidade);
        }

        SistemaCobrancaService cobranca = new SistemaCobrancaService(PASTA_DADOS.resolve("cobrancas.txt"));
        SistemaMatriculas sistema = new SistemaMatriculas(universidade, persistencia, cobranca);
        new MenuCLI(sistema).iniciar();
    }
}
