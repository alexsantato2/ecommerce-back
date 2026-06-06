# ecommerce-back

API REST de e-commerce desenvolvida com Spring Boot 4 para a disciplina de Sistemas Distribuídos e Desenvolvimento Web (USP).

## Stack

- Java 25 + Spring Boot 4.0.6
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA + Hibernate 6
- PostgreSQL
- Spring WebSocket (STOMP/SockJS)
- Spring Cache + Caffeine
- Bucket4j (rate limiting)
- SpringDoc OpenAPI 2.8.8 (Swagger UI)
- Lombok

## Pré-requisitos

- Java 25
- Maven 3.9+ (ou usar o wrapper `./mvnw`)
- PostgreSQL rodando localmente

## Setup

1. Criar o banco de dados:
   ```sql
   CREATE DATABASE ecommerce_db;
   ```

2. Copiar o arquivo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```

3. Ajustar `.env` com suas credenciais do PostgreSQL (porta padrão: 5433).

4. Subir a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

A API sobe em `http://localhost:8080`.

## Variáveis de ambiente (`.env`)

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5433/ecommerce_db` |
| `DB_USERNAME` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` |
| `JWT_SECRET` | Chave secreta HS256 (64 chars hex) | — |
| `JWT_EXPIRATION` | Expiração do access token em ms | `86400000` (1 dia) |
| `REFRESH_TOKEN_EXPIRATION` | Expiração do refresh token em ms | `604800000` (7 dias) |
| `ADMIN_EMAIL` | E-mail do admin padrão criado no startup | `admin@store.com` |
| `ADMIN_PASSWORD` | Senha do admin padrão | `admin123` |
| `CORS_ALLOWED_ORIGIN` | Origem permitida pelo CORS | `http://localhost:5173` |

## Documentação da API

Swagger UI disponível em: `http://localhost:8080/swagger-ui.html`

O admin padrão é criado automaticamente no primeiro startup se não existir nenhum usuário com papel ADMIN.

## Principais endpoints

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/auth/register` | — | Cadastro de cliente |
| POST | `/api/auth/login` | — | Login, retorna access + refresh token |
| POST | `/api/auth/refresh` | — | Renova access token |
| POST | `/api/auth/logout` | — | Invalida refresh token |
| GET | `/api/products` | — | Catálogo com filtros e paginação |
| GET | `/api/products/{id}` | — | Detalhe do produto |
| POST | `/api/products` | ADMIN | Criar produto |
| GET | `/api/products/{id}/reviews` | — | Avaliações do produto |
| POST | `/api/products/{id}/reviews` | CUSTOMER | Avaliar produto (requer pedido aprovado) |
| GET | `/api/cart` | AUTH | Obter o carrinho do usuário autenticado |
| POST | `/api/cart/items` | AUTH | Adicionar ou atualizar quantidade de um item no carrinho |
| PATCH | `/api/cart/items/{productId}` | AUTH | Atualizar diretamente a quantidade de um produto específico |
| DELETE | `/api/cart/items/{productId}` | AUTH | Remover um produto do carrinho |
| DELETE | `/api/cart` | AUTH | Esvaziar o carrinho por completo |
| GET | `/api/cart/user/{userId}` | ADMIN | Obter o carrinho de qualquer usuário (Apenas Admin) |
| POST | `/api/orders` | CUSTOMER | Finalizar compra |
| GET | `/api/orders/my` | CUSTOMER | Histórico de pedidos |
| GET | `/api/orders` | ADMIN | Todos os pedidos com filtros |
| GET | `/api/orders/pending-count` | ADMIN | Contagem de pedidos pendentes |
| PATCH | `/api/orders/{id}/approve` | ADMIN | Aprovar pedido |
| PATCH | `/api/orders/{id}/reject` | ADMIN | Rejeitar pedido (restaura estoque) |
| GET | `/api/users/me` | AUTH | Perfil do usuário autenticado |
| PATCH | `/api/users/me` | AUTH | Atualizar nome |
| GET | `/api/users` | ADMIN | Listar todos os usuários |

## WebSocket

Endpoint STOMP: `ws://localhost:8080/ws` (com SockJS fallback)

| Tópico | Descrição |
|--------|-----------|
| `/topic/orders/{userId}` | Notificações de status de pedido para o usuário |
| `/topic/admin/orders` | Novo pedido criado (broadcast admin) |
| `/topic/stock` | Alteração de estoque |

## Arquitetura

```
controller/   → recebe requisições HTTP, delega para services
service/      → lógica de negócio, transações
repository/   → acesso ao banco via Spring Data JPA
model/        → entidades JPA
dto/          → objetos de transferência de dados (request e response)
security/     → JWT filter, rate limit filter, UserDetailsService
config/       → SecurityConfig, WebSocketConfig, CacheConfig, OpenApiConfig
exception/    → GlobalExceptionHandler, exceções de domínio
init/         → DataInitializer (admin padrão)
```
