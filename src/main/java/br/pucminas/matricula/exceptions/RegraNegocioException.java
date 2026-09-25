package br.pucminas.matricula.exceptions;

/**
 * Lançada quando uma operação viola uma regra de negócio do sistema de matrículas.
 * A mensagem é exibida diretamente ao usuário.
 */
public class RegraNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
