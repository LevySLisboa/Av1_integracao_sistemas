# CRUD - AV1 Integração de Sistemas

API REST desenvolvida em **Java 17** com **Spring Boot**, destinada ao gerenciamento de alunos, professores e provas.

Projeto desenvolvido como parte da avaliação da disciplina **Técnicas de Integração de Sistemas**.

---

## 🌐 API Online

A aplicação está hospedada no **Render** e pode ser acessada através do endereço:

**https://api-kjea.onrender.com**

---

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Hibernate**
- **PostgreSQL**
- **Maven**
- **Docker**
- **Render**

---

## 📋 Funcionalidades

A API possui operações de gerenciamento para:

### 👨‍🎓 Alunos

- Cadastro de alunos
- Listagem de alunos
- Busca de aluno por matrícula
- Atualização de alunos
- Exclusão de alunos

### 👨‍🏫 Professores

- Cadastro de professores
- Listagem de professores
- Busca de professor por matrícula
- Atualização de professores
- Exclusão de professores

### 📝 Provas

- Cadastro de provas
- Listagem de provas
- Busca de provas
- Atualização de provas
- Exclusão de provas
- Associação de provas a alunos e a professores

---

## 🔗 Rotas da API

### 👨‍🎓 Alunos

| Método | Path |
|---|---|
| `POST` | `/aluno` |
| `GET` | `/aluno` |
| `GET` | `/aluno/{matricula}` |
| `PUT` | `/aluno/{id}` |
| `DELETE` | `/aluno/{id}` |

### 👨‍🏫 Professores

| Método | Path |
|---|---|
| `POST` | `/professor` |
| `GET` | `/professor` |
| `GET` | `/professor/{matricula}` |
| `PUT` | `/professor/{id}` |
| `DELETE` | `/professor/{id}` |

### 📝 Provas

| Método | Path |
|---|---|
| `POST` | `/prova` |
| `GET` | `/prova` |
| `GET` | `/prova/{id}` |
| `PUT` | `/prova/{id}` |
| `DELETE` | `/prova/{id}` |
