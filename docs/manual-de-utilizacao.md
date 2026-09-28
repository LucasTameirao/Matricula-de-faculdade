# Manual de Utilização — Sistema de Matrículas

Este manual explica como iniciar o sistema e como usá-lo com cada perfil de usuário: **aluno**, **professor** e **secretaria**. Todos os exemplos usam o banco de dados que vem junto com o projeto, na pasta `dados/`.

## Sumário

1. [Iniciando o sistema](#1-iniciando-o-sistema)
2. [Entrando no sistema](#2-entrando-no-sistema)
3. [Como ler as telas](#3-como-ler-as-telas)
4. [Perfil Aluno](#4-perfil-aluno)
5. [Perfil Professor](#5-perfil-professor)
6. [Perfil Secretaria](#6-perfil-secretaria)
7. [Ciclo de um semestre](#7-ciclo-de-um-semestre)
8. [Regras aplicadas pelo sistema](#8-regras-aplicadas-pelo-sistema)
9. [Banco de dados CSV](#9-banco-de-dados-csv)
10. [Solução de problemas](#10-solução-de-problemas)

---

## 1. Iniciando o sistema

**Pré-requisito:** Java (JDK) 17 ou superior. Para conferir a versão, rode `java -version` no Prompt de Comando (Windows) ou no terminal.

A classe principal é `br.pucminas.matricula.Main`. Escolha uma das formas:

| Onde | Como |
| ---- | ---- |
| **VSCode** (com o *Extension Pack for Java*) | Abra a pasta do projeto, abra `src/main/java/br/pucminas/matricula/Main.java` e clique em **Run** acima do `main`, ou pressione **F5**. |
| **IntelliJ IDEA** | *File → Open* → pasta do projeto. Abra `Main.java` e clique no ▶ verde ao lado do `main`. |
| **Eclipse** | *File → Import → Maven → Existing Maven Projects* → pasta do projeto. Botão direito em `Main.java` → *Run As → Java Application*. |
| **Windows, sem IDE** | Dois cliques em `executar.bat`. |
| **Linux/macOS, sem IDE** | `./executar.sh` |

O sistema funciona no terminal: você digita o número da opção e pressiona **Enter**. Ao iniciar, ele lê o banco de dados da pasta `dados/`. Se essa pasta não existir, ele a cria com os dados de exemplo.

---

## 2. Entrando no sistema

Na tela inicial, digite `1` (Entrar) e informe **login** e **senha**. O login é um nome de usuário, e não o número de matrícula. No banco de exemplo, **todos os usuários têm a senha `123`**.

| Perfil | Logins de exemplo | Observação |
| ------ | ----------------- | ---------- |
| Secretaria | `secretaria`, `coordenacao` | Acesso a todos os cadastros e ao período de matrículas |
| Professor | `ana`, `carlos`, `fernanda`, `ricardo`, `juliana`, `marcos`, `patricia`, `roberto` | Cada um ministra 2 ou 3 disciplinas |
| Aluno | `joao` | Ainda sem matrícula no semestre atual; bom para testar |
| Aluno | `maria` | Matrícula parcial (2 obrigatórias) |
| Aluno | `pedro` | Todas as vagas preenchidas (4 obrigatórias + 2 optativas) |
| Aluno | `lucas.costa`, `beatriz.pereira`, `rafael.rodrigues`... | 72 alunos ao todo; a lista completa está em `dados/alunos.csv` |

Se o login ou a senha estiverem errados, o sistema mostra `!! Login ou senha inválidos.` e volta à tela inicial.

Para sair de um perfil, digite `0` no menu. Para fechar o sistema, digite `0` na tela inicial.

---

## 3. Como ler as telas

```
============================================================
 ALUNO: João Silva (1001)
============================================================
[Semestre 2027/1 - Período de matrículas ABERTO]
1 - Ver disciplinas ofertadas
...
```

* **Título:** mostra o perfil e quem está logado. No caso do aluno, também o número de matrícula (`1001`).
* **Linha entre colchetes:** semestre atual e situação do período de matrículas: *ainda não aberto*, *ABERTO* ou *ENCERRADO*.
* **`>>`** indica que a operação deu certo; **`!!`** indica que foi recusada, com o motivo.
* Os códigos de disciplina podem ser digitados em maiúsculas ou minúsculas (`es102` = `ES102`).

**Situações de uma disciplina** (coluna *Situação*):

| Situação | Significado |
| -------- | ----------- |
| Não ofertada | Não faz parte do currículo do semestre atual |
| Aguardando período de matrículas | Está no currículo, mas o período ainda não foi aberto |
| Inscrições abertas | Aceita matrículas |
| Inscrições encerradas (lotada) | Atingiu 60 alunos e não aceita mais matrículas |
| Ativa | O período foi encerrado com 3 alunos ou mais: a disciplina vai ocorrer |
| Cancelada | O período foi encerrado com menos de 3 alunos: a disciplina não vai ocorrer |

---

## 4. Perfil Aluno

```
1 - Ver disciplinas ofertadas
2 - Matricular em disciplina obrigatória (1ª opção)
3 - Matricular em disciplina optativa (alternativa)
4 - Cancelar matrícula
5 - Minha matrícula do semestre
6 - Histórico de matrículas
0 - Sair
```

### 4.1 Ver disciplinas ofertadas (opção 1)
Lista as disciplinas do currículo do semestre, com o professor, os inscritos e a situação:

```
Código  Disciplina                         Professor        Inscritos  Situação
ES101   Algoritmos e Estruturas de Dados   Ana Souza        60/60      Inscrições encerradas (lotada)
ES102   Projeto de Software                Carlos Lima      23/60      Inscrições abertas
CC102   Teoria da Computação               Roberto Alves    2/60       Inscrições abertas
```

### 4.2 Matricular-se (opções 2 e 3)
1. Escolha `2` para uma disciplina **obrigatória** (1ª opção) ou `3` para uma **optativa** (alternativa).
2. O sistema mostra as disciplinas ofertadas. Digite o **código** da disciplina (ex.: `ES102`).
3. Se der certo, aparece `>> Matrícula realizada com sucesso! Sistema de cobranças notificado.`

Na primeira matrícula do semestre, o sistema cria a **matrícula do semestre**, identificada por semestre + número do aluno (ex.: `2027/1-1001`). As disciplinas seguintes entram nessa mesma matrícula.

Motivos de recusa mais comuns:

| Mensagem | Por quê |
| -------- | ------- |
| `!! Algoritmos e Estruturas de Dados atingiu o limite de 60 alunos. Inscrições encerradas.` | Disciplina lotada |
| `!! Limite de 4 disciplinas obrigatórias atingido.` | O aluno já tem 4 obrigatórias |
| `!! Limite de 2 disciplinas optativas atingido.` | O aluno já tem 2 optativas |
| `!! Você já está matriculado(a) em ...` | Matrícula repetida |
| `!! ... não é ofertada no semestre 2027/1.` | A disciplina não está no currículo |
| `!! Fora do período de matrículas.` | A secretaria ainda não abriu ou já encerrou o período |

### 4.3 Cancelar matrícula (opção 4)
O sistema mostra sua matrícula do semestre. Digite o código da disciplina a cancelar. O cancelamento só é aceito **durante o período de matrículas**. A disciplina cancelada continua no histórico, com a situação *Cancelado*.

### 4.4 Minha matrícula do semestre (opção 5)
```
Matrícula 2027/1-1001 (criada em 28/09/2026 09:18) - Em andamento
Obrigatórias: 1/4 | Optativas: 0/2
Código  Disciplina                         Tipo         Vinculado em      Situação
ES102   Projeto de Software                Obrigatória  28/09/2026 09:18  Inscrições abertas
```
A matrícula fica *Em andamento* enquanto o período está aberto e passa a *Concluída* quando a secretaria o encerra.

### 4.5 Histórico de matrículas (opção 6)
Mostra todas as matrículas do aluno, de todos os semestres, inclusive as disciplinas canceladas:
```
Matrícula 2026/2-1001 - Concluída
  ES101   Algoritmos e Estruturas de Dados   Obrigatória  Ativo
  ES102   Projeto de Software                Obrigatória  Cancelado
  ...
```

> Toda matrícula ou cancelamento **notifica o sistema de cobranças**, que recebe a lista atualizada das disciplinas a cobrar (veja `dados/cobrancas.csv`).

---

## 5. Perfil Professor

```
1 - Minhas disciplinas
2 - Alunos matriculados em uma disciplina
0 - Sair
```

* **Opção 1:** lista as disciplinas que o professor ministra, com o número de inscritos e a situação.
* **Opção 2:** mostra as disciplinas do professor. Digite o código de uma delas para ver os alunos matriculados:

```
Alunos matriculados em CC101: 26
Matrícula  Nome                           Curso
1006       Beatriz Pereira                Ciência da Computação
1009       Rafael Rodrigues               Engenharia de Software
...
```

O professor só pode consultar as próprias disciplinas. Qualquer outra recebe `!! Você não ministra a disciplina ...`.

---

## 6. Perfil Secretaria

```
1 - Cursos
2 - Disciplinas
3 - Professores
4 - Alunos
5 - Gerar currículo do semestre
6 - Abrir período de matrículas
7 - Encerrar período de matrículas
8 - Ver currículo atual
0 - Sair
```

### 6.1 Cadastros (opções 1 a 4)
Cada cadastro abre um submenu com **Listar**, **Cadastrar** e **Remover** (a opção `0` volta ao menu anterior).

| Cadastro | Dados pedidos | Observações |
| -------- | ------------- | ----------- |
| Cursos | nome, número de créditos | Um curso só pode ser removido se não tiver disciplinas nem alunos |
| Disciplinas | código (ex.: `ES108`), nome, curso, login do professor | Tem também a opção **Trocar professor**. Não é possível remover uma disciplina com alunos inscritos ou que apareça no histórico de matrículas |
| Professores | nome, login, senha | Um professor que ainda ministra disciplinas não pode ser removido |
| Alunos | nome, matrícula, curso, login, senha | Ao remover um aluno, as matrículas dele são canceladas |

Logins e números de matrícula não podem se repetir. Uma disciplina recém-cadastrada fica *Não ofertada* até entrar em um currículo.

### 6.2 Gerar currículo do semestre (opção 5)
1. Informe o semestre (ex.: `2027/2`).
2. Informe os códigos das disciplinas ofertadas, separados por vírgula (ex.: `ES101, ES102, CC101`), ou `TODAS`.
3. Confirme com `s`.

Regras: o período atual precisa estar encerrado, e não é possível repetir um semestre que já existe. As matrículas dos semestres anteriores continuam no histórico dos alunos.

### 6.3 Abrir período de matrículas (opção 6)
Libera matrículas e cancelamentos para os alunos, no currículo atual.

### 6.4 Encerrar período de matrículas (opção 7)
Fecha o período e mostra o resultado: disciplinas com **3 alunos ou mais** ficam **Ativas**, e as demais ficam **Canceladas**. As matrículas dos alunos passam a *Concluída*.

### 6.5 Ver currículo atual (opção 8)
Mostra o semestre, a situação do período e todas as disciplinas ofertadas, com os inscritos.

---

## 7. Ciclo de um semestre

```
Gerar currículo ──► Abrir período ──► Alunos se matriculam/cancelam ──► Encerrar período
   (opção 5)          (opção 6)         (cobranças notificadas)            (opção 7)
                                                                              │
                                  disciplinas com ≥ 3 alunos: ATIVAS ◄────────┤
                                  disciplinas com < 3 alunos: CANCELADAS ◄────┘
```

**Exemplo com o banco de exemplo** (semestre 2027/1, período aberto):
1. Entre como `joao` e tente se matricular em `ES101`: ela está lotada. Matricule-o em `ES102` (obrigatória) e `CC101` (optativa).
2. Entre como `pedro` e tente mais uma matrícula: ele já atingiu 4 obrigatórias e 2 optativas.
3. Entre como `ana` (professora) e consulte os alunos de `ES101`.
4. Entre como `secretaria` e encerre o período (opção 7): `CC102` (2 alunos), `EC102` (1) e `SI103` (0) são **canceladas**.
5. Ainda como secretaria, gere o currículo `2027/2` (opção 5) e abra o período (opção 6). O ciclo recomeça, e o semestre 2027/1 fica no histórico dos alunos.

---

## 8. Regras aplicadas pelo sistema

| Regra | Onde aparece |
| ----- | ------------ |
| Até **4 disciplinas obrigatórias** (1ª opção) por aluno no semestre | Matrícula do aluno |
| Até **2 disciplinas optativas** (alternativas) por aluno no semestre | Matrícula do aluno |
| Matrículas e cancelamentos só **durante o período de matrículas** | Aluno |
| Máximo de **60 alunos** por disciplina; ao atingir o limite, as inscrições são encerradas | Disciplina |
| Mínimo de **3 alunos** ao fim do período para a disciplina ficar ativa; caso contrário, é cancelada | Encerramento do período |
| O **sistema de cobranças** é notificado a cada matrícula ou cancelamento | `dados/cobrancas.csv` |
| Todo usuário acessa com **login e senha** | Tela inicial |

---

## 9. Banco de dados CSV

Todos os dados ficam na pasta `dados/`, com **um arquivo CSV por entidade**. Os arquivos usam separador `;` e codificação UTF-8, e abrem direto no Excel ou em qualquer editor de texto. O sistema **grava automaticamente** a cada alteração; não há um botão de salvar.

| Arquivo | Conteúdo |
| ------- | -------- |
| `secretarias.csv` | Usuários da secretaria (`login;senha;nome`) |
| `professores.csv` | Professores (`login;senha;nome`) |
| `cursos.csv` | Cursos (`nome;numeroCreditos`) |
| `alunos.csv` | Alunos (`id;matricula;nome;login;senha;curso`) |
| `disciplinas.csv` | Disciplinas (`id;codigo;nome;curso;loginProfessor;status`) |
| `curriculo.csv` | Semestre atual e situação do período (`semestre;periodoMatriculas`) |
| `curriculo_disciplinas.csv` | Disciplinas ofertadas no semestre atual (`semestre;codigoDisciplina`) |
| `matriculas.csv` | Matrícula de cada aluno por semestre (`id;codigoMatricula;matriculaAluno;semestre;dataCriacao;status`) |
| `itens_matricula.csv` | Disciplinas de cada matrícula (`idMatricula;matriculaAluno;codigoDisciplina;tipo;dataVinculo;status`) |
| `cobrancas.csv` | Notificações enviadas ao sistema de cobranças (criado na primeira matrícula) |

**Editar os arquivos à mão:** é possível, com o sistema **fechado**. Mantenha a linha de cabeçalho e o separador `;`. Se algo ficar inválido (por exemplo, um aluno com um curso que não existe), o sistema **não inicia e não altera nenhum arquivo**; ele informa o arquivo e a linha com o problema:

```
Não foi possível carregar os dados: dados/alunos.csv, linha 3: curso 'Curso Inexistente' não encontrado
```

**Voltar ao banco de exemplo:** rode `git checkout -- dados/` na pasta do projeto. Outra opção é apagar a pasta `dados/`: o sistema gera o banco de exemplo de novo na próxima execução.

---

## 10. Solução de problemas

| Problema | Solução |
| -------- | ------- |
| `'java' não é reconhecido como um comando` | Instale o JDK 17+ e reabra o terminal. |
| Os menus não aceitam o que eu digito na IDE | Rode pelo terminal integrado. No VSCode, use **F5** com a configuração *Sistema de Matrículas*, que já usa o terminal. |
| O VSCode mostra erros em vermelho, mas o programa roda | O índice da extensão Java está desatualizado. Use *Ctrl+Shift+P → Java: Clean Java Language Server Workspace* e reinicie. |
| Acentos estranhos no console do Windows | O sistema usa a codificação do console. Se ainda aparecerem, rode pelo terminal da IDE ou pelo `executar.bat`. |
| `!! Fora do período de matrículas.` | A secretaria precisa abrir o período (opção 6). |
| Não consigo gerar um novo currículo | Encerre antes o período atual (opção 7) e use um semestre novo. |
| O sistema não inicia e mostra um erro sobre um arquivo `.csv` | Corrija a linha indicada ou restaure o banco (seção 9). |
