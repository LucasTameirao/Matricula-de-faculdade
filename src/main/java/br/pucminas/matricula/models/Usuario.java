package br.pucminas.matricula.models;

public abstract class Usuario {
    protected String login;
    protected String senha;

    public Usuario(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }

    public boolean autenticar(String login, String senha) {
        // Stub: Valida login e senha
        return this.login.equals(login) && this.senha.equals(senha);
    }
    
    // Getters e Setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}

