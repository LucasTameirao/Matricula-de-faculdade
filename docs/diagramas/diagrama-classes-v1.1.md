# Diagrama de Classes — v1.1 (correção do Lab01S02)

Correção do diagrama v1 feita pela equipe na Sprint 02: inclusão de identificadores (`id`) e das classes `Matricula` e `AlunoMatricula`, que substituem a associação direta entre `Aluno` e `Disciplina`. A versão atual está no [README](../../README.md).

```mermaid
classDiagram
    class Usuario {
        <<abstract>>
        #login: String
        #senha: String
        +autenticar(login, senha): boolean
    }
    
    class Aluno {
        -id: Long
        -nome: String
        -matricula: String
        +matricular(Disciplina, isOptativa): boolean
        +cancelarMatricula(Disciplina): boolean
        +consultarDisciplinas(): List~Disciplina~
    }
    
    class Professor {
        -nome: String
        +consultarAlunos(Disciplina): List~Aluno~
        +consultarDisciplinas(): List~Disciplina~
    }
    
    class Secretaria {
        +gerarCurriculo(semestre: String): Curriculo
        +manterDisciplina(Disciplina): boolean
        +manterProfessor(Professor): boolean
        +manterAluno(Aluno): boolean
        +manterCurso(Curso): boolean
    }
    
    Usuario <|-- Aluno
    Usuario <|-- Professor
    Usuario <|-- Secretaria
    
    class Curso {
        -nome: String
        -numeroCreditos: int
        +adicionarDisciplina(Disciplina)
    }
    
    class Disciplina {
        -id: Long
        -nome: String
        -limiteAlunos: int = 60
        -minAlunos: int = 3
        -ativa: boolean
        +adicionarAluno(Aluno): boolean
        +removerAluno(Aluno): boolean
        +verificarStatus()
    }
    
    class Curriculo {
        -semestre: String
        +adicionarDisciplina(Disciplina)
    }
    
    class Matricula {
        -id: Long
        -codigoMatricula: String
        -dataCriacao: Date
        -status: String
    }

    class AlunoMatricula {
        -alunoId: Long
        -matriculaId: Long
        -disciplinaId: Long
        -dataVinculo: Date
        -isOptativa: boolean
        -status: String
    }
    
    class SistemaCobrancaService {
        +notificarCobranca(Aluno, List~Disciplina~)
    }
    
    Curso "1" *-- "0..*" Disciplina : contem
    Curriculo "1" o-- "0..*" Disciplina : oferece
    Disciplina "0..*" -- "1" Professor : ministrada por
    
    Aluno "1" <-- "0..*" AlunoMatricula : alunoId
    Matricula "1" <-- "0..*" AlunoMatricula : matriculaId
    Disciplina "1" <-- "0..*" AlunoMatricula : disciplinaId
    
    SistemaCobrancaService ..> Aluno : notifica
```
