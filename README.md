# Card API v1.0.0

API RESTful desenvolvida em Java 17 com Spring Boot para cadastro e consulta de números de cartão de forma segura.

## 🚀 Tecnologias e Bibliotecas

### Core
- **Java 17** - Linguagem de programação
- **Spring Boot 3.0.7** - Framework principal
- **Maven** - Gerenciador de dependências

### Spring Framework
- **Spring Boot Starter Web** - Aplicação web RESTful
- **Spring Boot Starter Data JPA** - Persistência de dados
- **Spring Boot Starter Security** - Segurança e autenticação
- **Spring Boot Starter Validation** - Validação de dados
- **Spring Security Test** - Testes de segurança

### Banco de Dados
- **MySQL 8.0** - Banco de dados principal (produção)
- **H2 Database** - Banco de dados em memória (testes)
- **MySQL Connector/J** - Driver JDBC para MySQL

### Segurança e Autenticação
- **JWT (JSON Web Token)** - Autenticação baseada em tokens
  - `jjwt-api` (0.11.5)
  - `jjwt-impl` (0.11.5)
  - `jjwt-jackson` (0.11.5)
- **Jasypt Spring Boot Starter** (3.0.3) - Criptografia de dados sensíveis
- **BCrypt** - Hash de senhas (incluído no Spring Security)

### Utilitários
- **Lombok** - Redução de boilerplate code
- **Logstash Logback Encoder** (7.4) - Formatação de logs estruturados

### Testes
- **JUnit 5 (Jupiter)** - Framework de testes unitários
  - `junit-jupiter-api` - API do JUnit 5
  - `junit-jupiter-engine` - Engine de execução
- **Mockito** - Framework de mocking
  - `mockito-core` - Core do Mockito
  - `mockito-junit-jupiter` - Extensão Mockito para JUnit 5
- **Spring Boot Starter Test** - Utilitários de teste do Spring Boot
- **H2 Database** - Banco de dados em memória para testes

### Containerização
- **Docker** - Containerização da aplicação
- **Docker Compose** - Orquestração de containers

### Versões Principais

| Biblioteca | Versão |
|------------|--------|
| **Card API** | **1.0.0** |
| Spring Boot | 3.0.7 |
| Java | 17 |
| JWT (jjwt) | 0.11.5 |
| Jasypt | 3.0.3 |
| Logstash Logback Encoder | 7.4 |
| MySQL Connector | 8.0+ |
| JUnit 5 | (via Spring Boot Starter Test) |
| Mockito | (via Spring Boot Starter Test) |

## 📋 Funcionalidades

- ✅ Autenticação JWT com roles de permissão
- ✅ Cadastro de cartão único via API
- ✅ Cadastro em lote via arquivo TXT
- ✅ Consulta de cartão por número completo
- ✅ Criptografia end-to-end dos dados sensíveis
- ✅ Logging completo de requisições e respostas
- ✅ Testes unitários com cobertura completa

## 🔐 Segurança

- **JWT Authentication**: Autenticação baseada em tokens JWT
- **Role-Based Access Control**: Controle de acesso baseado em roles
- **End-to-End Encryption**: Criptografia dos números de cartão usando Jasypt
- **Hash SHA-256**: Hash dos números de cartão para busca eficiente

## 📦 Estrutura do Projeto

```
card-api/
├── src/
│   ├── main/
│   │   ├── java/com/hyperativa/cardapi/
│   │   │   ├── config/          # Configurações (Security, Async, Web)
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── exception/       # Tratamento de exceções
│   │   │   ├── repository/      # Repositórios JPA
│   │   │   ├── security/        # Configuração de segurança e JWT
│   │   │   └── service/         # Lógica de negócio
│   │   └── resources/
│   │       └── application.yml
│   └── test/                    # Testes unitários
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## 🛠️ Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Docker e Docker Compose (para execução via containers)
- MySQL 8.0 (se executar localmente sem Docker)

## 🚀 Como Executar

### Opção 1: Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <repository-url>
cd card-api
```

2. Execute o docker-compose:
```bash
docker-compose up --build
```

A aplicação estará disponível em `http://localhost:8080`

### Opção 2: Execução Local

1. Inicie o MySQL (ou use Docker apenas para o banco):
```bash
docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=carddb -p 3306:3306 mysql:8.0
```

2. Configure as variáveis de ambiente ou edite `application.yml`

3. Execute a aplicação:
```bash
mvn clean install
mvn spring-boot:run
```

## 📝 Usuários Padrão

A aplicação vem com 3 usuários pré-configurados:

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | CARD_REGISTER, CARD_QUERY |
| register | register123 | CARD_REGISTER |
| query | query123 | CARD_QUERY |

## 📡 Endpoints da API

### 1. Autenticação

#### POST `/api/auth/login`
Autentica o usuário e retorna um token JWT.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "roles": ["CARD_REGISTER", "CARD_QUERY"]
}
```

### 2. Cadastro de Cartão

#### POST `/api/cards/register`
Cadastra um único cartão. Requer role `CARD_REGISTER`.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request:**
```json
{
  "cardNumber": "4456897999999999",
  "batchNumber": "BATCH001",
  "sequenceNumber": 1
}
```

**Response:**
```json
{
  "id": 1,
  "message": "Card registered successfully"
}
```

#### POST `/api/cards/register/file`
Cadastra múltiplos cartões a partir de arquivo TXT. Requer role `CARD_REGISTER`.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Request:**
- Form data com campo `file` contendo arquivo `.txt`

**Response:**
```json
[
  {
    "id": 1,
    "message": "Card registered successfully"
  },
  {
    "id": 2,
    "message": "Card registered successfully"
  }
]
```

### 3. Consulta de Cartão

#### POST `/api/cards/check`
Verifica se um cartão existe na base de dados. Requer role `CARD_QUERY`.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request:**
```json
{
  "cardNumber": "4456897999999999"
}
```

**Response (Cartão encontrado):**
```json
{
  "exists": true,
  "cardId": 1,
  "message": "Card found"
}
```

**Response (Cartão não encontrado):**
```json
{
  "exists": false,
  "cardId": null,
  "message": "Card not found"
}
```

## 📄 Formato do Arquivo TXT

O arquivo TXT deve seguir o formato especificado:

```
DESAFIO-HYPERATIVA           20180524LOTE0001000010
C2     4456897999999999
C1     4456897922969999
C3     4456897999999999
C4     4456897998199999
C5     4456897999999999124
C6     4456897912999999
C7     445689799999998
C8     4456897919999999
C9     4456897999099999
C10    4456897919999999
LOTE0001000010
```

**Estrutura:**
- **Linha 1 (Header)**: 
  - Posições 1-29: Nome (DESAFIO-HYPERATIVA)
  - Posições 30-37: Data (20180524)
  - Posições 38-45: Lote (LOTE0001000010)
  - Posições 46-51: Quantidade de registros

- **Linhas C1-CN (Cartões)**:
  - Posição 1: Identificador da linha (C)
  - Posições 2-7: Numeração no lote
  - Posições 8-26: Número de cartão completo

- **Última linha (Footer)**:
  - Posições 1-8: Lote
  - Posições 9-14: Quantidade de registros

## 🧪 Testes

O projeto utiliza **JUnit 5 (Jupiter)** com **Mockito** para testes unitários. Todos os testes são mockados e não dependem de integração com banco de dados ou contexto Spring completo.

### Estrutura de Testes

- **CardServiceTest** - Testes unitários do serviço de cartões (6 testes)
- **FileProcessingServiceTest** - Testes unitários do processamento de arquivos (7 testes)
- **EncryptionServiceTest** - Testes unitários do serviço de criptografia (7 testes)
- **CardControllerTest** - Testes unitários do controller de cartões (8 testes)
- **AuthControllerTest** - Testes unitários do controller de autenticação (5 testes)
- **JwtTokenProviderTest** - Testes unitários do provedor JWT (9 testes)
- **CardRepositoryTest** - Testes unitários do repositório (8 testes)

**Total: 50 testes unitários** cobrindo cenários felizes e tristes.

### Executando os Testes

Execute todos os testes:
```bash
mvn test
```

Execute um teste específico:
```bash
mvn test -Dtest=CardServiceTest
```

Execute com logs detalhados:
```bash
mvn test -X
```

### Padrões Utilizados

- **@ExtendWith(MockitoExtension.class)** - Extensão Mockito para JUnit 5
- **@Mock** - Criação de mocks
- **@InjectMocks** - Injeção de dependências mockadas
- **@DisplayName** - Nomes descritivos para os testes
- **Given-When-Then** - Padrão de organização dos testes

## 🔧 Configuração

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_HOST` | Host do MySQL | localhost |
| `DB_PORT` | Porta do MySQL | 3306 |
| `DB_NAME` | Nome do banco | carddb |
| `DB_USER` | Usuário do banco | root |
| `DB_PASSWORD` | Senha do banco | root |
| `JWT_SECRET` | Chave secreta para JWT | (ver application.yml) |
| `JASYPT_PASSWORD` | Senha para criptografia | (ver application.yml) |

### application.yml

Edite `src/main/resources/application.yml` para configurar:
- Conexão com banco de dados
- Configurações JWT
- Configurações de criptografia
- Logging

## 📊 Logging

A aplicação utiliza **Logstash Logback Encoder** para formatação estruturada de logs.

### O que é registrado:
- Todas as requisições HTTP (método, URI, IP, headers)
- Todas as respostas HTTP (status, tempo de resposta)
- Operações de cadastro e consulta de cartões
- Processamento de arquivos (início, fim, quantidade processada)
- Erros e exceções com stack trace completo
- Operações de autenticação e autorização

### Formato dos Logs:
```
2024-01-01 12:00:00 - Incoming request - ID: xxx, Method: POST, URI: /api/cards/register
2024-01-01 12:00:01 - Outgoing response - ID: xxx, Status: 201, Duration: 150ms
2024-01-01 12:00:02 - Registering card - Batch: BATCH001, Sequence: 1
2024-01-01 12:00:02 - Card registered successfully with ID: 1
```

### Níveis de Log:
- **DEBUG**: Informações detalhadas (com.hyperativa, org.springframework.security)
- **INFO**: Operações normais da aplicação
- **WARN**: Situações de atenção (cartão já existe, etc.)
- **ERROR**: Erros e exceções

## 🔒 Segurança dos Dados

### Criptografia
- **Jasypt**: Criptografia AES-256 com algoritmo `PBEWITHHMACSHA512ANDAES_256`
- **Salt Generator**: RandomSaltGenerator para geração de salt único
- **IV Generator**: RandomIvGenerator para vetores de inicialização aleatórios
- **Números de cartão**: Criptografados antes de serem armazenados no banco

### Hash
- **SHA-256**: Algoritmo de hash usado para criar índices de busca
- **Busca eficiente**: Permite verificar existência sem descriptografar
- **Unicidade**: Garante que cada cartão tenha um hash único

### Autenticação e Autorização
- **JWT (JSON Web Token)**: Tokens assinados com HMAC-SHA512
- **Expiração**: Tokens com tempo de expiração configurável (padrão: 24 horas)
- **Roles**: Controle de acesso baseado em roles (CARD_REGISTER, CARD_QUERY)
- **Stateless**: Autenticação sem estado, sem necessidade de sessão

### Recomendações de Produção
- **HTTPS**: Configure HTTPS para garantir segurança end-to-end
- **Chaves Secretas**: Use chaves secretas fortes e armazene-as de forma segura
- **Rotação de Chaves**: Implemente rotação periódica de chaves JWT e Jasypt
- **Rate Limiting**: Considere implementar rate limiting para prevenir ataques

## 🚀 Escalabilidade e Performance

### Processamento Assíncrono
- **@Async**: Processamento de arquivos em threads separadas
- **CompletableFuture**: Retorno assíncrono para não bloquear requisições
- **Thread Pool**: Configurável via `AsyncConfig`
  - Core Pool Size: 5 threads
  - Max Pool Size: 10 threads
  - Queue Capacity: 100 tarefas

### Banco de Dados
- **Índices**: Índice único no campo `cardHash` para busca eficiente
- **JPA**: Otimizações automáticas de queries
- **Connection Pool**: Pool de conexões gerenciado pelo Spring Boot

### Arquitetura
- **Stateless**: Aplicação sem estado, preparada para horizontal scaling
- **RESTful**: API REST stateless e escalável
- **Microserviços**: Arquitetura preparada para decomposição em microserviços

### Otimizações
- **Lazy Loading**: Carregamento sob demanda de entidades
- **Batch Processing**: Processamento em lote de arquivos grandes
- **Caching**: Preparado para implementação de cache (Redis, etc.)

## 📝 Notas Importantes

### Antes de Produção
1. **Senhas e Chaves**: Altere todas as senhas padrão e chaves de criptografia
2. **Variáveis de Ambiente**: Use variáveis de ambiente para configurações sensíveis
3. **HTTPS**: Configure HTTPS/TLS para garantir segurança end-to-end
4. **Firewall**: Configure regras de firewall adequadas
5. **Rate Limiting**: Implemente rate limiting para prevenir abuso

### Operações
1. **Backup**: Implemente estratégia de backup automático para o banco de dados
2. **Monitoramento**: Adicione ferramentas de monitoramento (Prometheus, Grafana, ELK Stack)
3. **Alertas**: Configure alertas para erros críticos e performance
4. **Logs**: Configure rotação e retenção de logs adequada
5. **Health Checks**: Implemente endpoints de health check para orquestradores

### Desenvolvimento
1. **Testes**: Mantenha cobertura de testes acima de 80%
2. **Code Review**: Sempre faça code review antes de merge
3. **Documentação**: Mantenha documentação atualizada
4. **Versionamento**: Use versionamento semântico (SemVer)

## 📄 Licença

Este projeto é um desafio técnico desenvolvido para Hyperativa.

## 👨‍💻 Cleverson de Melo

Desenvolvido como parte do desafio técnico Hyperativa.


