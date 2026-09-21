package br.pucminas.matricula.models;

public class Secretaria extends Usuario {

    public Secretaria(String login, String senha) {
        super(login, senha);
    }

    public Curriculo gerarCurriculo(String semestre) {
        // Stub: Gera currículo para o semestre
        return new Curriculo(semestre);
    }

    public boolean manterDisciplina(Disciplina disciplina) {
        // Stub: Cadastra ou atualiza disciplina
        return true;
    }

    public boolean manterProfessor(Professor professor) {
        // Stub: Cadastra ou atualiza professor
        return true;
    }

    public boolean manterAluno(Aluno aluno) {
        // Stub: Cadastra ou atualiza aluno
        return true;
    }
    
    public boolean manterCurso(Curso curso) {
        // Stub: Cadastra ou atualiza curso
        return true;
    }
}

