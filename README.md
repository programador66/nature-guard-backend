# 🌿 NatureGuard Backend

API REST para a plataforma **NatureGuard** — um sistema de denúncias ambientais que permite que cidadãos registrem e acompanhem ocorrências como queimadas, desmatamentos, poluição e outros crimes ambientais.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Tecnologias](#-tecnologias)
- [Arquitetura do Projeto](#-arquitetura-do-projeto)
- [Modelo de Dados](#-modelo-de-dados)
- [Endpoints da API](#-endpoints-da-api)
  - [Autenticação](#autenticação)
  - [Reports (Denúncias)](#reports-denúncias)
  - [Uploads](#uploads)
- [Segurança](#-segurança)
- [Configuração e Execução](#-configuração-e-execução)
  - [Pré-requisitos](#pré-requisitos)
  - [Subir o Banco de Dados](#1-subir-o-banco-de-dados)
  - [Executar a Aplicação](#2-executar-a-aplicação)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Exemplos de Uso (cURL)](#-exemplos-de-uso-curl)
- [Testes](#-testes)
- [Estrutura de Pastas](#-estrutura-de-pastas)

---

## 🎯 Visão Geral

O NatureGuard Backend fornece uma API completa para:

- **Cadastro e autenticação** de usuários com JWT
- **Criação, edição e listagem** de denúncias ambientais (reports)
- **Upload de imagens** associadas a cada denúncia
- **Filtros avançados** por tags, data, texto e nome do usuário
- **Paginação** na listagem de denúncias
- **Modo anônimo** — usuários que optam por esse modo têm seus nomes preservados nas listagens públicas

---

## 🛠 Tecnologias

| Tecnologia              | Versão   | Finalidade                          |
|--------------------------|----------|--------------------------------------|
| Java                     | 17       | Linguagem principal                  |
| Spring Boot              | 4.0.5    | Framework web                        |
| Spring Security          | —        | Autenticação e autorização           |
| Spring Data JPA          | —        | Acesso a dados / ORM                 |
| PostgreSQL               | 16       | Banco de dados relacional            |
| JJWT (jsonwebtoken)      | 0.12.6   | Geração e validação de tokens JWT    |
| Lombok                   | —        | Redução de boilerplate               |
| Bean Validation          | —        | Validação de DTOs                    |
| Docker / Docker Compose  | —        | Infraestrutura (PostgreSQL + pgAdmin)|
| Maven                    | —        | Gerenciamento de dependências        |

---

## 🏗 Arquitetura do Projeto

O projeto segue uma arquitetura em camadas (Layered Architecture):

```
Controller → Service (interface) → ServiceImpl → Repository → Database
                                        ↕
                                   Mapper / DTO
```

- **Controller** — Recebe requisições HTTP, valida entrada e delega ao Service.
- **Service** — Interface que define contratos de negócio.
- **ServiceImpl** — Implementação da lógica de negócio.
- **Repository** — Interfaces JPA para acesso ao banco.
- **Specification** — Filtros dinâmicos com JPA Criteria API.
- **Mapper** — Conversão entre entidades e DTOs.
- **JWT** — Filtro de autenticação e geração/validação de tokens.
- **Config** — Configurações de segurança, CORS e recursos estáticos.
- **Exceptions** — Tratamento global de exceções.

---

## 📊 Modelo de Dados

### User (`users`)

| Campo            | Tipo          | Descrição                                |
|------------------|---------------|------------------------------------------|
| `id`             | `BIGINT` (PK) | Identificador auto-incremento           |
| `name`           | `VARCHAR`      | Nome do usuário                         |
| `email`          | `VARCHAR` (UK) | Email único (usado como login)          |
| `password`       | `VARCHAR`      | Senha criptografada (BCrypt)            |
| `is_autonomous_mode` | `BOOLEAN`  | Modo anônimo ativado                    |
| `created_at`     | `TIMESTAMP`    | Data de criação                         |

### Report (`reports`)

| Campo        | Tipo          | Descrição                              |
|--------------|---------------|----------------------------------------|
| `id`         | `BIGINT` (PK) | Identificador auto-incremento         |
| `title`      | `VARCHAR`      | Título da denúncia                    |
| `description`| `VARCHAR`      | Descrição detalhada                   |
| `lat`        | `DOUBLE`       | Latitude (localização embarcada)      |
| `lng`        | `DOUBLE`       | Longitude (localização embarcada)     |
| `address`    | `VARCHAR`      | Endereço textual                      |
| `user_id`    | `VARCHAR`      | Email do usuário que criou            |
| `created_at` | `TIMESTAMP`    | Data de criação                       |

### Report Tags (`report_tags`)

| Campo       | Tipo          | Descrição                               |
|-------------|---------------|-----------------------------------------|
| `report_id` | `BIGINT` (FK) | Referência ao report                   |
| `tag`       | `VARCHAR`      | Tag da denúncia (ex: `QUEIMADA`)       |

### Report Images (`report_images`)

| Campo       | Tipo          | Descrição                               |
|-------------|---------------|-----------------------------------------|
| `report_id` | `BIGINT` (FK) | Referência ao report                   |
| `image_url` | `VARCHAR`      | Caminho da imagem no servidor          |

---

## 🔌 Endpoints da API

### Autenticação

| Método | Rota             | Autenticado | Descrição                           |
|--------|------------------|-------------|--------------------------------------|
| POST   | `/auth/register` | ❌          | Cadastro de novo usuário             |
| POST   | `/auth/login`    | ❌          | Login e obtenção de token JWT        |

#### `POST /auth/register`

**Request Body:**
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "123456",
  "confirmationPassword": "123456",
  "isAutonomousMode": false
}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "isAutonomousMode": false,
  "createdAt": "2026-04-01T16:00:00",
  "token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

#### `POST /auth/login`

**Request Body:**
```json
{
  "email": "joao@email.com",
  "password": "123456"
}
```

**Response (200):** Mesmo formato do register.

---

### Reports (Denúncias)

| Método | Rota                 | Autenticado | Descrição                                          |
|--------|----------------------|-------------|------------------------------------------------------|
| POST   | `/reports`           | ✅          | Criar nova denúncia (multipart/form-data)            |
| GET    | `/reports`           | ❌          | Listagem paginada com filtros                        |
| GET    | `/reports/my-reports`| ✅          | Listar denúncias do usuário autenticado              |
| PUT    | `/reports/{id}`      | ✅          | Editar denúncia existente (somente o autor)          |

#### `POST /reports`

Envio via `multipart/form-data` com duas partes:

| Part     | Tipo                  | Obrigatório | Descrição                    |
|----------|-----------------------|-------------|-------------------------------|
| `data`   | `application/json`    | ✅          | JSON com dados do report      |
| `images` | arquivos (`file[]`)   | ❌          | Imagens da denúncia           |

**Parte `data` (JSON):**
```json
{
  "title": "Queimada forte",
  "description": "Fogo grande na mata",
  "tags": ["QUEIMADA"],
  "lat": -26.3,
  "lng": -48.8,
  "address": "Joinville"
}
```

**Response (200):**
```json
{
  "id": 1,
  "title": "Queimada forte",
  "description": "Fogo grande na mata",
  "tags": ["QUEIMADA"],
  "lat": -26.3,
  "lng": -48.8,
  "address": "Joinville",
  "createdAt": "2026-04-01T17:00:00",
  "userName": "João Silva",
  "images": ["/uploads/uuid-file.jpg"]
}
```

#### `GET /reports` — Listagem Paginada com Filtros

| Parâmetro   | Tipo       | Obrigatório | Descrição                                          |
|-------------|------------|-------------|------------------------------------------------------|
| `page`      | `int`      | ❌          | Número da página (padrão: 0)                         |
| `size`      | `int`      | ❌          | Itens por página (padrão: 10)                        |
| `sort`      | `string`   | ❌          | Campo de ordenação (padrão: `createdAt`)             |
| `tags`      | `string[]` | ❌          | Filtro por tags (ex: `tags=QUEIMADA&tags=POLUIÇÃO`)  |
| `startDate` | `datetime` | ❌          | Data início (ISO 8601)                               |
| `endDate`   | `datetime` | ❌          | Data fim (ISO 8601)                                  |
| `search`    | `string`   | ❌          | Busca por título, descrição ou nome do usuário       |

**Response (200):** Resposta paginada Spring (`Page<ReportResponseDTO>`).

> **Nota:** Quando o usuário está com `isAutonomousMode = true`, o campo `userName` retorna `"Usuário anônimo"`.

#### `PUT /reports/{id}`

Mesmo formato do `POST /reports`. Apenas o autor da denúncia pode editá-la.

---

### Uploads

| Método | Rota               | Autenticado | Descrição                        |
|--------|---------------------|-------------|-----------------------------------|
| GET    | `/uploads/{file}`   | ❌          | Acesso público às imagens         |

As imagens são servidas como recursos estáticos a partir do diretório configurado em `file.upload-dir`.

---

## 🔒 Segurança

- **Autenticação:** JWT (JSON Web Token) com HMAC SHA, expiração de 1 hora.
- **Criptografia de senhas:** BCrypt.
- **Sessões:** Stateless (sem estado no servidor).
- **CORS:** Configurável via variável de ambiente `CORS_ALLOWED_ORIGINS`, utilizando `allowedOriginPatterns` para flexibilidade em produção.
- **Rotas públicas:**
  - `POST /auth/register`
  - `POST /auth/login`
  - `GET /reports` (listagem pública)
  - `GET /uploads/**` (imagens)
- **Rotas protegidas:** Todas as demais requerem header `Authorization: Bearer <token>`.

---

## ⚙ Configuração e Execução

### Pré-requisitos

- **Java 17+**
- **Maven 3.8+** (ou use o wrapper `./mvnw`)
- **Docker e Docker Compose**

### 1. Subir o Banco de Dados

```bash
cd backend
docker-compose up -d
```

Isso inicia:
- **PostgreSQL 16** na porta `5432` (user: `admin`, password: `admin123`, database: `natureguard`)
- **pgAdmin 4** na porta `8081` (email: `admin@natureguard.com`, password: `admin123`)

### 2. Executar a Aplicação

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada na porta **3333** (perfil `dev`).

> O Hibernate está configurado com `ddl-auto=update`, portanto as tabelas são criadas/atualizadas automaticamente.

---

## 🔧 Variáveis de Ambiente

| Variável                | Padrão                                                        | Descrição                          |
|-------------------------|---------------------------------------------------------------|------------------------------------|
| `CORS_ALLOWED_ORIGINS`  | `http://localhost:5173`                                       | Origens permitidas (separadas por vírgula) |
| `server.port`           | `3333` (dev)                                                  | Porta do servidor                  |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/natureguard`                | URL de conexão com o banco         |
| `spring.datasource.username` | `admin`                                                  | Usuário do banco                   |
| `spring.datasource.password` | `admin123`                                               | Senha do banco                     |
| `file.upload-dir`       | `./uploads`                                                   | Diretório de armazenamento de imagens |

---

## 📝 Exemplos de Uso (cURL)

### Registro

```bash
curl -X POST http://localhost:3333/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria",
    "email": "maria@email.com",
    "password": "123456",
    "confirmationPassword": "123456",
    "isAutonomousMode": false
  }'
```

### Login

```bash
curl -X POST http://localhost:3333/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@email.com",
    "password": "123456"
  }'
```

### Criar Report (com imagem)

```bash
curl -X POST http://localhost:3333/reports \
  -H "Authorization: Bearer <SEU_TOKEN>" \
  -F 'data={"title":"Queimada forte","description":"Fogo grande na mata","tags":["QUEIMADA"],"lat":-26.3,"lng":-48.8,"address":"Joinville"};type=application/json' \
  -F "images=@/caminho/para/foto.jpg"
```

### Criar Report (sem imagem)

```bash
curl -X POST http://localhost:3333/reports \
  -H "Authorization: Bearer <SEU_TOKEN>" \
  -F 'data={"title":"Desmatamento","description":"Área desmatada","tags":["DESMATAMENTO"],"lat":-26.3,"lng":-48.8,"address":"Joinville"};type=application/json'
```

### Listagem Paginada com Filtros

```bash
# Página 0, 10 itens, filtro por tag
curl "http://localhost:3333/reports?page=0&size=10&tags=QUEIMADA"

# Busca por texto (título, descrição ou nome do usuário)
curl "http://localhost:3333/reports?search=fogo"

# Filtro por período
curl "http://localhost:3333/reports?startDate=2026-04-01T00:00:00&endDate=2026-04-30T23:59:59"
```

### Meus Reports

```bash
curl http://localhost:3333/reports/my-reports \
  -H "Authorization: Bearer <SEU_TOKEN>"
```

### Editar Report

```bash
curl -X PUT http://localhost:3333/reports/1 \
  -H "Authorization: Bearer <SEU_TOKEN>" \
  -F 'data={"title":"Queimada atualizada","description":"Descrição atualizada","tags":["QUEIMADA","INCÊNDIO"],"lat":-26.3,"lng":-48.8,"address":"Joinville"};type=application/json' \
  -F "images=@/caminho/para/nova-foto.jpg"
```

---

## 🧪 Testes

O projeto possui testes unitários para as camadas de Controller e Service:

```
src/test/java/com/natureguard/backend/
├── controller/
│   ├── AuthControllerTest.java
│   └── ReportControllerTest.java
└── service/
    └── impl/
        ├── AuthServiceImplTest.java
        ├── FileStorageServiceImplTest.java
        └── ReportServiceImplTest.java
```

Para executar todos os testes:

```bash
./mvnw test
```

---

## 📂 Estrutura de Pastas

```
backend/
├── docker-compose.yml                  # PostgreSQL + pgAdmin
├── pom.xml                             # Dependências Maven
├── mvnw / mvnw.cmd                     # Maven Wrapper
├── uploads/                            # Diretório de imagens (runtime)
└── src/
    ├── main/
    │   ├── java/com/natureguard/backend/
    │   │   ├── BackendApplication.java           # Classe principal
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java           # Regras de segurança HTTP
    │   │   │   ├── SecurityBeansConfig.java      # Beans (PasswordEncoder, CORS)
    │   │   │   └── WebConfig.java                # Servir uploads como estáticos
    │   │   ├── controller/
    │   │   │   ├── AuthController.java           # /auth/register, /auth/login
    │   │   │   └── ReportController.java         # /reports (CRUD)
    │   │   ├── domain/
    │   │   │   ├── dto/
    │   │   │   │   ├── AuthResponseDTO.java
    │   │   │   │   ├── LoginRequestDTO.java
    │   │   │   │   ├── RegisterRequestDTO.java
    │   │   │   │   ├── ReportRequestDTO.java
    │   │   │   │   └── ReportResponseDTO.java
    │   │   │   └── model/
    │   │   │       ├── User.java                 # Entidade JPA
    │   │   │       ├── Report.java               # Entidade JPA
    │   │   │       └── Location.java             # @Embeddable (lat, lng, address)
    │   │   ├── exceptions/
    │   │   │   └── GlobalExceptionHandler.java   # Tratamento global de erros
    │   │   ├── jwt/
    │   │   │   ├── JwtService.java               # Gerar/validar tokens
    │   │   │   └── JwtAuthenticationFilter.java  # Filtro de autenticação
    │   │   ├── mapper/
    │   │   │   └── ReportMapper.java             # Entity ↔ DTO
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ReportRepository.java
    │   │   │   └── specification/
    │   │   │       └── ReportSpecification.java  # Filtros dinâmicos (Criteria API)
    │   │   └── service/
    │   │       ├── AuthService.java              # Interface
    │   │       ├── ReportService.java            # Interface
    │   │       ├── FileStorageService.java       # Interface
    │   │       └── impl/
    │   │           ├── AuthServiceImpl.java
    │   │           ├── ReportServiceImpl.java
    │   │           └── FileStorageServiceImpl.java
    │   └── resources/
    │       ├── application.properties            # Configuração geral
    │       ├── application-dev.properties        # Configuração de desenvolvimento
    │       └── logback-spring.xml                # Configuração de logs
    └── test/                                     # Testes unitários
```

---

## 📜 Licença

Projeto acadêmico (TCC).

