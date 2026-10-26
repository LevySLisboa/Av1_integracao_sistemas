# Sistema de Provas Acadêmicas — API com Sessão + 2FA por Email

API REST em **Java 17 / Spring Boot** para gerenciar **alunos, professores e provas**, protegida por
**autenticação baseada em sessão (cookie HttpOnly) com segundo fator por email (OTP)**.
Projeto da **AV2 — Técnicas de Integração de Sistemas** (evolução da AV1).

🌐 **Versão deployada:** https://api-kjea.onrender.com
> No plano gratuito do Render a primeira requisição pode demorar ~50 s (instância "acordando").

---

## Sumário
1. [Apresentação do sistema](#1-apresentação-do-sistema)
2. [Autenticação](#2-autenticação)
3. [Documentação de uso](#3-documentação-de-uso)
4. [Integração com API externa](#4-integração-com-api-externa)
5. [Deploy](#5-deploy)
6. [Testes](#6-testes)
7. [Estrutura do projeto](#7-estrutura-do-projeto)
8. [Limitações conhecidas](#8-limitações-conhecidas)

---

## 1. Apresentação do sistema

**Nome:** Sistema de Provas Acadêmicas.
**Descrição:** API para que a coordenação cadastre alunos e professores, que professores lancem provas/notas
e que alunos consultem apenas as próprias provas, com dados pessoais sensíveis (CPF, salário, telefone,
notas) protegidos por perfil.

### Casos de uso principais
- Administrador cadastra alunos, professores e contas de acesso.
- Professor cria/edita as provas das suas turmas e lança notas.
- Aluno consulta suas provas e seu cadastro.
- Professor/Admin consulta se a data de uma prova cai em **feriado nacional** e a **localização (CEP)** de um aluno (integração externa).

### Perfis e permissões

| Recurso / ação | ADMIN | PROFESSOR | ALUNO |
|---|:---:|:---:|:---:|
| `GET /alunos` (lista) | ✅ completo | ✅ resumo (sem CPF/telefone/nascimento) | ❌ 403 |
| `GET /alunos/{matrícula}` | ✅ completo | ✅ resumo | ✅ **só o próprio** (403 nos demais) |
| `POST/PUT/DELETE /alunos` | ✅ | ❌ 403 | ❌ 403 |
| `GET /professor` e `/professor/{matrícula}` | ✅ completo | ✅ próprio completo, outros resumo | ✅ resumo (sem salário/CPF) |
| `POST/PUT/DELETE /professor` | ✅ | ❌ 403 | ❌ 403 |
| `GET /provas` | ✅ todas | ✅ só as dele | ✅ só as dele |
| `GET /provas/{id}` | ✅ | ✅ só se for dele | ✅ só se for dele |
| `POST/PUT/DELETE /provas` | ✅ | ✅ só provas dele | ❌ 403 |
| `/usuarios` (gestão de contas) | ✅ | ❌ 403 | ❌ 403 |
| `/integracao/**` | ✅ | ✅ (prova: só dele) | ❌ 403 |

### Dados gerenciados

| Recurso | Campos |
|---|---|
| **Aluno** | nome, email, matrícula (gerada), dataNascimento, curso, cpfCnpj 🔒, semestreDeReferencia, telefone 🔒, pontuacaoMediaGeral |
| **Professor** | nome, email, matrícula (gerada), dataNascimento 🔒, cpfCnpj 🔒, telefone 🔒, departamento, especializacao, titulacao, salario 🔒, cargaHoraria, dataAdmissao |
| **Prova** | disciplina, nota, av, assunto, professor, turma, aluno, dataAplicacao |
| **Usuario** (login) | nome, email, senhaHash 🔒 (BCrypt, nunca exposta), papel, matricula vinculada, tentativas de falha, bloqueio, último login |

🔒 = dado protegido (não aparece para perfis sem permissão). Exclusões são **lógicas** (`ativo=false`).
O banco é populado automaticamente com **3 professores, 3 alunos e 3 provas** (`DataSeeder`).

---

## 2. Autenticação

### Tipo escolhido
**Autenticação por sessão (cookie de sessão HttpOnly, *stateful*) + senha (BCrypt) + 2º fator por email (OTP de 6 dígitos).**
Não usamos Basic Auth, Bearer Token (nem JWT no header `Authorization`), OAuth 2.0 ou API Key.

### Como funciona (passo a passo)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente (Postman/curl/navegador)
    participant A as API (Spring Security)
    participant DB as PostgreSQL
    participant E as Email (Resend)

    C->>A: GET /auth/csrf
    A-->>C: token CSRF (cookie XSRF-TOKEN + JSON)

    C->>A: POST /auth/login {email, senha} + X-XSRF-TOKEN
    A->>DB: busca usuário, confere hash BCrypt
    alt senha incorreta
        A-->>C: 401 (e conta falhas; 5 falhas = bloqueio de 15 min)
    else senha correta
        A->>DB: grava HASH do OTP (expira em 5 min)
        A->>E: envia código de 6 dígitos
        E-->>C: 📧 email com o código
        A-->>C: 200 "código enviado" + cookie SESSION (PRÉ-autenticada, sem acesso)
    end

    C->>A: POST /auth/verificar-2fa {codigo} + cookie SESSION
    A->>DB: valida hash, expiração e tentativas (máx. 5); apaga o código (uso único)
    alt código inválido / expirado
        A-->>C: 401
    else código correto
        A->>A: troca o ID da sessão (anti session-fixation) e a promove a AUTENTICADA
        A-->>C: 200 + usuário (novo cookie SESSION)
    end

    C->>A: GET /provas (cookie SESSION)
    A->>A: sessão → perfil → regra de URL → regra de posse
    A-->>C: 200 / 401 / 403

    C->>A: POST /auth/logout + X-XSRF-TOKEN
    A->>DB: destrói a sessão
    A-->>C: 200 (cookie removido)
```

Resumo do fluxo:
1. `GET /auth/csrf` entrega o token CSRF.
2. `POST /auth/login` valida email+senha. Se corretos, **ainda não dá acesso**: cria uma sessão *pré-autenticada* e envia o código por email.
3. `POST /auth/verificar-2fa` valida o código. Só então a sessão vira *autenticada* (com o perfil do usuário).
4. Cada requisição seguinte envia o cookie `SESSION` (automático em navegador/Postman; `-b` no curl). O servidor busca a sessão no banco e aplica as regras de perfil.
5. `POST /auth/logout` destrói a sessão no servidor — o cookie roubado deixa de valer **imediatamente**.

### Por que escolhemos essa abordagem
- **Atende ao enunciado:** é diferente de Basic, Bearer, OAuth 2.0 e API Key.
- **Logout e revogação reais:** a sessão vive no servidor; podemos encerrá-la na hora (com JWT isso exige listas de bloqueio).
- **Segurança em camadas:** quem descobre a senha ainda precisa do email do usuário.
- **Dados sensíveis:** o sistema guarda CPF, salário e notas — um segundo fator é proporcional ao risco.
- **Encaixa no Spring:** Spring Security + Spring Session JDBC dão suporte maduro; as sessões ficam no PostgreSQL e **sobrevivem a reinícios/deploys** do Render.

### Pontos fortes
- Revogação imediata (logout, bloqueio de usuário, troca de senha).
- Cookie `HttpOnly` + `Secure` + `SameSite=Strict`: JavaScript não lê a sessão (mitiga roubo por XSS).
- Segundo fator reduz o impacto de senha vazada/fraca.
- O código OTP é de uso único, expira rápido e só seu hash é armazenado.
- Nenhum segredo trafega em header a cada requisição.

### Pontos fracos / quando **não** usar
- **Stateful:** exige armazenamento de sessão e complica escalar horizontalmente (mitigado com Spring Session JDBC, mas há custo de consulta ao banco por requisição).
- **CSRF:** cookies exigem proteção anti-CSRF e cuidado com CORS (implementamos token CSRF, o que dá mais trabalho ao cliente).
- **Não é ideal** para apps mobile nativos, integração máquina-a-máquina (serviço→serviço) ou APIs públicas de terceiros — nesses casos token/OAuth 2.0 é mais adequado.
- **Email como 2º fator é o elo mais fraco:** se a caixa de email for comprometida, o fator cai. TOTP (app autenticador) ou WebAuthn são mais robustos. Também há dependência de um provedor de email (se estiver fora, ninguém loga).
- Atrito de UX: o usuário precisa abrir o email a cada login.

### Segurança implementada

| Tema | Implementação |
|---|---|
| **Senhas** | Hash **BCrypt (custo 12)**; nunca em texto puro; mínimo de 10 caracteres ao criar usuário; `senhaHash` marcado `@JsonIgnore` |
| **OTP** | 6 dígitos via `SecureRandom`; só o **hash BCrypt** é salvo; **expira em 5 min**; **máx. 5 tentativas**; **uso único**; reenvio com intervalo mínimo de 60 s |
| **Tokens/sessão expiram?** | Sim: sessão expira após **30 min** de inatividade; janela do 2FA de 5 min; ID de sessão trocado ao autenticar (anti *session fixation*) |
| **Rate limiting** | Por IP: **10 req/min** em `/auth/login`, `/auth/verificar-2fa`, `/auth/reenviar-codigo` e **120 req/min** no total → `429` + `Retry-After` |
| **Força bruta** | **5 senhas erradas = conta bloqueada por 15 min** |
| **Enumeração de usuários** | Mesma mensagem (`Credenciais inválidas.`) para email inexistente e senha errada; BCrypt "fantasma" equaliza o tempo de resposta |
| **Cookie de sessão** | `HttpOnly`, `Secure`, `SameSite=Strict` |
| **CSRF** | Token duplo (cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN`) em todo método que altera estado |
| **Validação de entrada** | Bean Validation (`@Valid`) em todos os corpos; erros `400` com os campos inválidos |
| **Erros** | `401`, `403`, `404`, `409`, `429`, `502`, `503` padronizados; erros internos **não vazam** stack trace |
| **Dados sensíveis** | DTOs de resumo e `@JsonIgnoreProperties` escondem CPF, salário, telefone etc. de quem não tem permissão |
| **Mass assignment** | `id`, `ativo` e `matricula` do corpo são ignorados na criação |

### Implementação (código)

**Tecnologias:** Spring Boot 4, Spring Security, Spring Session JDBC, Spring Data JPA, PostgreSQL, Bean Validation, RestClient, Resend (API de email), Docker.

**Cadeia de segurança** (`config/SecurityConfig.java`, resumo):
```java
http
  .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
  .httpBasic(AbstractHttpConfigurer::disable)       // sem Basic
  .formLogin(AbstractHttpConfigurer::disable)
  .addFilterBefore(rateLimit, SecurityContextHolderFilter.class)
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/auth/csrf", "/auth/login", "/auth/verificar-2fa", "/auth/reenviar-codigo").permitAll()
      .requestMatchers("/usuarios/**").hasRole("ADMIN")
      .requestMatchers(HttpMethod.GET, "/provas/**").hasAnyRole("ADMIN", "PROFESSOR", "ALUNO")
      .requestMatchers("/provas/**").hasAnyRole("ADMIN", "PROFESSOR")
      /* ... */
      .anyRequest().authenticated());
```

**Promoção da sessão após o 2FA** (`services/AutenticacaoService.java`, resumo):
```java
Resultado r = codigos.validar(u.getId(), req.codigo());   // hash, expiração, tentativas
...
request.changeSessionId();                                // anti session fixation
var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null,
        List.of(new SimpleGrantedAuthority("ROLE_" + u.getPapel())));
contexto.setAuthentication(auth);
contextRepository.saveContext(contexto, request, response); // grava na sessão (PostgreSQL)
```

**Geração do OTP** (`services/CodigoVerificacaoService.java`, resumo):
```java
String codigo = String.format("%06d", random.nextInt(1_000_000));   // SecureRandom
repo.save(new CodigoVerificacao(usuarioId, encoder.encode(codigo),  // só o hash
          Instant.now().plusSeconds(ttlSegundos)));
```

---

## 3. Documentação de uso

### 3.1 Rodar localmente

**Pré-requisitos:** Java 17+, Maven (ou `./mvnw`), PostgreSQL.

```bash
git clone <URL-DO-REPOSITORIO>
cd Av1_integracao_sistemas-main
cp .env.example .env        # edite os valores
createdb crud_av2           # ou crie o banco pelo seu cliente
./mvnw spring-boot:run
```
A API sobe em `http://localhost:8080`. Com `MAIL_PROVIDER=console` (padrão), **o código 2FA aparece no log** do servidor (somente para desenvolvimento).

### 3.2 Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|:---:|---|
| `DATABASE_URL` | ✅ | URL JDBC, ex.: `jdbc:postgresql://localhost:5432/crud_av2` |
| `DATABASE_USER` / `DATABASE_PASS` | ✅ | Credenciais do banco |
| `COOKIE_SECURE` | — | `true` (padrão). Use `false` **só** em HTTP local |
| `MAIL_PROVIDER` | — | `console` (padrão, dev) \| `resend` (produção) \| `smtp` |
| `MAIL_FROM` | — | Remetente. Padrão `onboarding@resend.dev` |
| `RESEND_API_KEY` | se `resend` | Chave da API do Resend |
| `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` | se `smtp` | Config SMTP padrão do Spring |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD` | recomendado | Cria o admin inicial (senha ≥ 10 caracteres) |
| `SEED_PROFESSOR_EMAIL`, `SEED_ALUNO_EMAIL`, `SEED_USER_PASSWORD` | opcional | Contas de demonstração vinculadas ao 1º professor/aluno |
| `RATE_LIMIT_AUTH` / `RATE_LIMIT_GERAL` | — | Req/min por IP (padrão 10 / 120) |
| `SHOW_SQL` | — | `true` para logar SQL (padrão `false`) |

### 3.3 Exemplos de requisição (curl)

> Como a autenticação é por **cookie de sessão** + **CSRF**, use um *cookie jar* (`-c`/`-b`) e envie o token no header `X-XSRF-TOKEN` nos `POST/PUT/DELETE`.
> Script pronto: [`docs/exemplos-curl.sh`](docs/exemplos-curl.sh).

```bash
BASE=https://api-kjea.onrender.com      # ou http://localhost:8080
JAR=jar.txt

# 1) token CSRF
curl -s -c $JAR -b $JAR $BASE/auth/csrf
XSRF=$(awk '$6=="XSRF-TOKEN"{print $7}' $JAR | tail -n1)

# 2) login — etapa 1 (senha). O código chega por email
curl -s -c $JAR -b $JAR -X POST $BASE/auth/login \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF" \
  -d '{"email":"admin@exemplo.com","senha":"SuaSenha#123"}'
# {"mensagem":"Senha correta. Enviamos um código de 6 dígitos para a***@exemplo.com.",
#  "doisFatoresNecessario":true,"codigoExpiraEmSegundos":300,...}

# 3) login — etapa 2 (código do email)
XSRF=$(awk '$6=="XSRF-TOKEN"{print $7}' $JAR | tail -n1)
curl -s -c $JAR -b $JAR -X POST $BASE/auth/verificar-2fa \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF" \
  -d '{"codigo":"123456"}'
# {"mensagem":"Autenticado com sucesso.","usuario":{"id":1,"email":"admin@exemplo.com","papel":"ADMIN",...}}

# 4) endpoints protegidos (só o cookie; GET não precisa de CSRF)
curl -s -b $JAR $BASE/auth/me
curl -s -b $JAR $BASE/alunos
curl -s -b $JAR $BASE/provas

# 5) criar prova (POST precisa de CSRF) — ADMIN ou PROFESSOR dono
curl -s -b $JAR -c $JAR -X POST $BASE/provas \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF" \
  -d '{"disciplina":"Redes","nota":8.0,"av":2,"assunto":"TCP/IP","turma":"N1",
       "professor":{"id":1},"aluno":{"id":1},"dataAplicacao":"15/11/2026"}'

# 6) logout
curl -s -b $JAR -c $JAR -X POST $BASE/auth/logout -H "X-XSRF-TOKEN: $XSRF"
```

**Postman:** habilite o cookie jar (padrão), faça `GET /auth/csrf` primeiro e, no *Pre-request Script* da coleção, adicione:
```js
pm.request.headers.upsert({ key: 'X-XSRF-TOKEN', value: pm.cookies.get('XSRF-TOKEN') });
```

### 3.4 Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| GET | `/auth/csrf` | pública | Token CSRF |
| POST | `/auth/login` | pública | Etapa 1: email + senha → envia código |
| POST | `/auth/verificar-2fa` | sessão pré-autenticada | Etapa 2: valida o código |
| POST | `/auth/reenviar-codigo` | sessão pré-autenticada | Novo código (intervalo mín. 60 s) |
| GET | `/auth/me` | autenticado | Dados do usuário logado |
| POST | `/auth/logout` | autenticado | Encerra a sessão |
| GET/POST/DELETE | `/usuarios`, `/usuarios/{id}` | ADMIN | Gestão de contas |
| GET/POST/PUT/DELETE | `/alunos`, `/alunos/{matricula}` | ver tabela de permissões | CRUD de alunos |
| GET/POST/PUT/DELETE | `/professor`, `/professor/{matricula}` | ver tabela de permissões | CRUD de professores |
| GET/POST/PUT/DELETE | `/provas`, `/provas/{id}` | ver tabela de permissões | CRUD de provas |
| GET | `/integracao/provas/{id}/calendario` | ADMIN, PROFESSOR | Prova + feriados (externo) |
| GET | `/integracao/alunos/{matricula}/localizacao?cep=` | ADMIN, PROFESSOR | Aluno + endereço do CEP (externo) |

### 3.5 Exemplos de erro

Formato padrão: `{"timestamp","status","erro","mensagem"[,"campos"]}`.

**401 Unauthorized — sem sessão autenticada**
```bash
curl -i $BASE/provas
```
```json
{"status":401,"erro":"Unauthorized","mensagem":"Autenticação necessária. Faça login em POST /auth/login e confirme o código enviado por email."}
```

**401 — senha errada** (mesma mensagem para email inexistente) → `{"status":401,...,"mensagem":"Credenciais inválidas."}`

**401 — código errado/expirado** → `"Código inválido."` / `"Código expirado. Faça login novamente."`

**403 Forbidden — perfil sem permissão** (ex.: ALUNO tentando listar alunos, ou ver prova de outro aluno)
```json
{"status":403,"erro":"Forbidden","mensagem":"Acesso negado: seu perfil não tem permissão para este recurso."}
```

**403 — CSRF ausente** (POST sem `X-XSRF-TOKEN`)
```json
{"status":403,"erro":"Forbidden","mensagem":"Token CSRF ausente ou inválido. Obtenha em GET /auth/csrf e envie no header X-XSRF-TOKEN."}
```

**400 Bad Request — validação**
```json
{"status":400,"erro":"Bad Request","mensagem":"Dados inválidos.","campos":{"email":"deve ser um endereço de e-mail bem formado"}}
```

**429 Too Many Requests — rate limit** (header `Retry-After`) ou conta bloqueada por 5 senhas erradas.

**404 / 409 / 502 / 503** — recurso inexistente / duplicado / falha na API externa / falha no envio do email.

### 3.6 Como testar online
1. Peça ao grupo as credenciais de demonstração (ou crie um admin via `SEED_*` no deploy).
2. Rode `BASE=https://api-kjea.onrender.com ./docs/exemplos-curl.sh email senha` e digite o código recebido por email.
3. Teste também o 403 logando como **ALUNO** e tentando `GET /alunos`.

---

## 4. Integração com API externa

Usamos a **[BrasilAPI](https://brasilapi.com.br)** (pública, sem chave) via `RestClient` com timeouts (5 s conexão / 10 s leitura). Falhas viram `502 Bad Gateway`; CEP inexistente vira `404`.

| Endpoint local | Requisição externa | Dados locais | Combinação |
|---|---|---|---|
| `GET /integracao/provas/{id}/calendario` | `GET https://brasilapi.com.br/api/feriados/v1/{ano}` | Prova (disciplina, turma, professor, aluno, `dataAplicacao`) | Verifica se `dataAplicacao` coincide com feriado nacional ou fim de semana e lista os próximos feriados |
| `GET /integracao/alunos/{matricula}/localizacao?cep=60811905` | `GET https://brasilapi.com.br/api/cep/v2/{cep}` | Aluno (nome, curso, semestre) | Junta o cadastro do aluno com rua/bairro/cidade/UF do CEP |

Exemplo (a prova seed nº 1 está marcada para 02/11/2026, feriado de Finados):
```bash
curl -s -b $JAR $BASE/integracao/provas/1/calendario
```
```json
{
  "dadosLocais":  {"id":1,"disciplina":"Técnicas de Integração de Sistemas","dataAplicacao":"2026-11-02","diaDaSemana":"MONDAY", "...":"..."},
  "dadosExternos":{"fonte":"BrasilAPI /feriados/v1/2026","coincideComFeriado":true,"feriado":{"date":"2026-11-02","name":"Finados","type":"national"},"proximosFeriados":[...]},
  "alerta":"A prova está marcada em um feriado nacional: Finados"
}
```

---

## 5. Deploy

Deploy no **Render** (Web Service com Docker + PostgreSQL do Render).

1. Suba o repositório no GitHub.
2. Render → **New → PostgreSQL** (copie a *Internal Database URL*, convertendo para `jdbc:postgresql://host:5432/db`).
3. Render → **New → Web Service** → conecte o repositório → *Runtime: Docker* (informe o caminho do `DockerFile`).
4. Configure as variáveis de ambiente da seção 3.2. Em produção: `COOKIE_SECURE=true`, `MAIL_PROVIDER=resend`, `RESEND_API_KEY`, `MAIL_FROM`, `SEED_*`.
5. A cada `git push` na branch principal o Render refaz o deploy. As tabelas (inclusive `SPRING_SESSION`) são criadas automaticamente.

### ⚠️ Email no Render (plano gratuito)
Desde 26/09/2025, **serviços gratuitos do Render bloqueiam saída SMTP** (portas 25, 465 e 587). Por isso o envio em produção usa a **API HTTP do Resend** (HTTPS/443). Passos:
1. Crie uma conta em https://resend.com e gere uma **API Key**.
2. Sem domínio próprio verificado, use `MAIL_FROM=onboarding@resend.dev` — nesse modo o Resend só entrega para o **email dono da conta**. Para enviar a qualquer destinatário, verifique um domínio no Resend.
3. Defina `MAIL_PROVIDER=resend` e `RESEND_API_KEY`.

> `MAIL_PROVIDER=console` **nunca** deve ser usado em produção (o código vai para o log).

---

## 6. Testes
```bash
./mvnw test
```
Há testes unitários do limitador de requisições (rate limiting) e da máscara de email. O teste `contextLoads` herdado da AV1 exige banco de dados configurado.

Roteiro manual sugerido: login sem 2FA (deve falhar), código errado 5×, senha errada 5×, ALUNO acessando `/alunos` (403), ALUNO acessando prova de outro (403), POST sem CSRF (403), logout e reuso do cookie (401).

---

## 7. Estrutura do projeto
```
src/main/java/com/example/demo/
├── config/        SecurityConfig, HttpClientConfig, DataSeeder
├── controllers/   AuthController, UsuarioController, Aluno/Professor/ProvaController, IntegracaoController
├── dto/           LoginRequest, CodigoRequest, UsuarioRequest/Response, AlunoResumoDTO, ProfessorResumoDTO
├── exception/     ApiException + exceções por status HTTP
├── handler/       ErroHandler (respostas de erro padronizadas)
├── models/        entity (Aluno, Professor, Prova, Usuario, CodigoVerificacao) e repository
├── security/      RateLimitFilter, LimitadorDeRequisicoes, UsuarioPrincipal, JsonErro, Mascara
└── services/      AutenticacaoService, CodigoVerificacaoService, PermissaoService, IntegracaoService, email/*
docs/exemplos-curl.sh
frontend/       (React + Mantine: tela de login com 2FA)
```

---

## 8. Limitações conhecidas
- **Rate limiter em memória:** o contador é por instância e zera ao reiniciar; com várias instâncias seria necessário Redis ou similar. O IP vem de `X-Forwarded-For` (confiável atrás do proxy do Render).
- **Email é um 2º fator fraco** comparado a TOTP/WebAuthn; depende de provedor externo e da segurança da caixa de entrada.
- **Sem recuperação de senha** nem troca de senha pelo usuário (apenas o ADMIN cria contas).
- **Sem CORS configurado:** pensado para clientes não-navegador (Postman/curl) ou front-end no mesmo domínio.
- **Plano gratuito do Render** dorme após inatividade (primeira chamada lenta).
- **Listagens sem paginação.**

---

## 9. Front-end de demonstração (login com 2FA)

Pasta [`frontend/`](frontend): **React + Vite + Mantine** com a tela de login em 3 etapas (senha → código de 6 dígitos → acesso liberado) e um painel que dispara GETs nos endpoints protegidos, mostrando na tela os `200`, `401` e `403`. Serve para demonstrar o fluxo na apresentação.

```bash
cd frontend
npm install
cp .env.example .env     # VITE_API_TARGET = endereço da API
npm run dev              # http://localhost:5173
```

- **Sem CORS:** o navegador chama `/api/...` no próprio endereço do front e o Vite repassa para a API (`vite.config.js`). Assim os cookies `SESSION` e `XSRF-TOKEN` funcionam como em mesma origem. Para usar a API do Render, troque `VITE_API_TARGET=https://api-kjea.onrender.com`.
- **API local:** suba com `COOKIE_SECURE=false`.
- **Fluxo no código:** `src/api.js` (token CSRF + cookie), `EtapaSenha.jsx`, `EtapaCodigo.jsx` (entrada do código, contagem de expiração e reenvio) e `Painel.jsx` (teste dos endpoints).
- **Publicar o front em outro domínio** exigiria configurar CORS com credenciais no Spring e rever o `SameSite=Strict` do cookie; para a demonstração, o proxy do Vite basta.
