# 🔗 URL Shortener

<p align="center">
  <strong>Scalable URL Shortening Service</strong>
</p>

<p align="center">
  Serviço de encurtamento de URLs desenvolvido com Java e Spring Boot, com foco em performance, caching, processamento assíncrono, resiliência e escalabilidade horizontal.
</p>

<p align="center">
  <a href="https://github.com/alfredobaptista/url-shortener">GitHub Repository</a>
</p>

---

## 📌 Sobre o Projecto

O **URL Shortener** é uma implementação própria de um serviço de encurtamento de URLs, desenvolvida a partir de um desafio/conteúdo de **System Design** sobre a construção de um serviço capaz de lidar com grandes volumes de tráfego.

O projecto foi desenvolvido com foco não apenas na funcionalidade de encurtamento, mas principalmente nos problemas de engenharia envolvidos num sistema deste tipo:

* Performance e baixa latência
* Caching
* Rate limiting
* Concorrência
* Processamento assíncrono
* Persistência
* Analytics
* Resiliência
* Escalabilidade horizontal

A implementação utiliza **Clean Architecture / Ports & Adapters**, mantendo a lógica de negócio desacoplada dos mecanismos de persistência, cache, mensageria e infraestrutura.

---

## 🏗️ Arquitectura

```mermaid
flowchart LR
    Client[Client]

    API[URL Shortener API]

    Redis[(Redis)]
    PostgreSQL[(PostgreSQL)]
    RabbitMQ[(RabbitMQ)]

    Consumer[Analytics Consumer]

    Client --> API

    API --> Redis
    API --> PostgreSQL

    API --> RabbitMQ
    RabbitMQ --> Consumer

    Consumer --> PostgreSQL
```

### Fluxo de criação

```text
Client
  │
  ▼
POST /api/v1/urls
  │
  ▼
Rate Limiting
  │
  ▼
Generate Base62 Code
  │
  ▼
Persist PostgreSQL
  │
  ▼
Cache Redis
  │
  ▼
Return Short Code
```

### Fluxo de redireccionamento

```text
Client
  │
  ▼
GET /{shortCode}
  │
  ▼
Redis Cache
  │
  ├── HIT ──────────────┐
  │                     │
  └── MISS              │
       │                │
       ▼                │
   PostgreSQL            │
       │                │
       ▼                │
   Cache Redis           │
       │                │
       └────────────────┘
                │
                ▼
          Publish Event
                │
                ▼
          RabbitMQ
                │
                ▼
       Analytics Consumer
                │
                ▼
          PostgreSQL
```

---

## 🎯 Principais Decisões Técnicas

### Redis — Cache-Aside

O Redis é utilizado como camada de cache para evitar consultas desnecessárias ao PostgreSQL durante os redireccionamentos.

```text
Request
   │
   ▼
Redis
   │
   ├── HIT  → Return URL
   │
   └── MISS
        │
        ▼
    PostgreSQL
        │
        ▼
    Redis + Return
```

O TTL do cache é calculado de acordo com a validade da URL.

---

### Redis + Lua — Rate Limiting

O controlo de abuso utiliza Redis com um script Lua para realizar as operações de incremento e expiração de forma atómica.

Configuração actual:

```text
10 requests / minuto / client
```

Quando o limite é excedido, a API retorna:

```http
429 Too Many Requests
```

A utilização de Lua permite evitar condições de corrida entre múltiplas operações Redis.

---

### RabbitMQ — Processamento Assíncrono

Os eventos de acesso às URLs não bloqueiam o redireccionamento.

Em vez de executar a persistência das métricas directamente no request:

```text
HTTP Request
     │
     ▼
Redirect
     │
     ▼
Publish Event
     │
     ▼
Return 302
```

O processamento das analytics ocorre posteriormente:

```text
RabbitMQ
    │
    ▼
Analytics Consumer
    │
    ▼
PostgreSQL
```

Isto mantém o caminho crítico do redireccionamento mais simples e desacoplado do processamento das métricas.

---

### Base62

Os códigos das URLs são gerados utilizando caracteres alfanuméricos:

```text
0-9
A-Z
a-z
```

O projecto utiliza códigos de **6 caracteres** como configuração actual, por exemplo:

```text
bbVAZ9
```

A geração utiliza `SecureRandom`.

---

### Expiração de URLs

Uma URL pode possuir uma data de expiração.

Quando a URL expira:

```text
Request
   │
   ▼
URL Lookup
   │
   ▼
Expired?
   │
   └── Yes → UrlExpiredException
```

O TTL do Redis também é ajustado à data de expiração da URL.

---

## 🧱 Arquitectura de Código

O projecto utiliza **Clean Architecture / Ports & Adapters**.

```text
src/main/java/com/github/alfredobaptista/

├── adapter
│   ├── in
│   │   ├── handler
│   │   ├── messaging
│   │   └── web
│   │
│   └── out
│       ├── cache
│       ├── messaging
│       ├── persistence
│       ├── generator
│       └── security
│
├── application
│   ├── dto
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
│
├── config
│
├── domain
│   ├── exception
│   ├── model
│   └── valueobject
│
└── UrlShortenerApplication.java
```

---

## 🔌 API

### Criar URL

```http
POST /api/v1/urls
Content-Type: application/json
```

Request:

```json
{
  "originalUrl": "https://www.example.com"
}
```

Response:

```json
{
  "shortUrl": "bbVAZ9"
}
```

---

### Redireccionar

```http
GET /{shortCode}
```

Response:

```http
HTTP/1.1 302 Found
Location: https://www.example.com
```

---

### Consultar Analytics

```http
GET /api/v1/urls/{shortCode}/analytics
```

Exemplo:

```json
{
  "shortCode": "bbVAZ9",
  "totalClicks": 2,
  "firstAccessedAt": "2026-09-28T07:47:33.665781",
  "lastAccessedAt": "2026-09-28T07:49:38.640115"
}
```

---

### Eliminar URL

```http
DELETE /api/v1/urls/{shortCode}
```

A operação remove a URL da persistência e invalida a entrada correspondente no Redis.

---

## 🧪 Testes

O projecto possui testes unitários utilizando:

* JUnit 5
* Mockito

Execução:

```bash
./mvnw clean test
```

Resultado actual:

```text
35 tests
0 failures
0 errors
```

Principais componentes testados:

* criação de URLs
* redireccionamento
* eliminação
* analytics
* geração de códigos
* value objects
* regras de negócio

---

## ⚡ Testes de Carga

Os testes de carga foram realizados com **k6** em ambiente local.

Os resultados abaixo representam o comportamento observado no ambiente de desenvolvimento e **não constituem uma medição de capacidade de produção**.

| VUs | Requests |   Throughput | Failures |      Avg |       p95 |
| --: | -------: | -----------: | -------: | -------: | --------: |
|  10 |  ~48.012 | ~1.600 req/s |       0% |  5.89 ms |  12.61 ms |
|  50 |  ~77.635 | ~2.585 req/s |       0% |    19 ms |  45.71 ms |
| 100 |  ~72.377 | ~2.408 req/s |       0% | 41.18 ms | 117.01 ms |

### Observação

Com o aumento da concorrência, o throughput deixa de crescer de forma linear enquanto a latência aumenta.

Isto evidencia a importância de distribuir a carga entre múltiplas instâncias da aplicação e utilizar componentes partilhados para estado e dados.

---

## 📈 Escalabilidade

A arquitectura foi concebida para permitir evolução de uma única instância para múltiplas instâncias da API.

### Arquitectura actual

```text
             ┌──────────────┐
             │    Client    │
             └──────┬───────┘
                    │
                    ▼
             ┌──────────────┐
             │     API      │
             └──────┬───────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
     Redis      PostgreSQL   RabbitMQ
```

### Evolução horizontal

```text
                    Client
                      │
                      ▼
              ┌──────────────┐
              │ Load Balancer│
              └───────┬──────┘
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
       API #1       API #2       API #3
          │           │           │
          └───────────┼───────────┘
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
     Redis        PostgreSQL     RabbitMQ
```

Como Redis, PostgreSQL e RabbitMQ são externos às instâncias da API, várias instâncias podem partilhar o mesmo estado.

### Possíveis evoluções

* Load Balancer
* múltiplas instâncias da API
* Redis Cluster
* PostgreSQL com read replicas
* RabbitMQ com múltiplos consumers
* observabilidade distribuída
* métricas e tracing
* políticas de retenção de analytics
* optimização adicional da geração de códigos

Estas são **evoluções arquitecturais**, não capacidades que o projecto actualmente reivindica possuir em produção.

---

## 🛠️ Stack Tecnológica

| Categoria       | Tecnologias                           |
| --------------- | ------------------------------------- |
| Linguagem       | Java 21                               |
| Framework       | Spring Boot                           |
| API             | REST                                  |
| Persistência    | PostgreSQL                            |
| Cache           | Redis                                 |
| Mensageria      | RabbitMQ                              |
| Migrações       | Flyway                                |
| Testes          | JUnit 5 · Mockito                     |
| Load Testing    | k6                                    |
| Containerização | Docker · Docker Compose               |
| Arquitectura    | Clean Architecture · Ports & Adapters |
| Build           | Maven                                 |

---

## 🐳 Execução com Docker

### Pré-requisitos

* Docker
* Docker Compose
* Git

Clonar o projecto:

```bash
git clone git@github.com:alfredobaptista/url-shortener.git
cd url-shortener
```

Construir e iniciar os serviços:

```bash
docker compose up --build
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

RabbitMQ Management:

```text
http://localhost:15672
```

Credenciais locais:

```text
username: admin
password: admin123
```

---

## 💻 Execução Local

Caso pretenda executar a aplicação directamente com Maven:

```bash
./mvnw spring-boot:run
```

O ambiente de desenvolvimento utiliza:

```text
PostgreSQL
localhost:5432

Redis
localhost:6379

RabbitMQ
localhost:5672
```

As migrações da base de dados são executadas através do **Flyway**.

---

## 🗄️ Serviços

### PostgreSQL

Responsável pela persistência de:

* URLs
* analytics
* timestamps
* estado persistente da aplicação

### Redis

Responsável por:

* cache das URLs
* rate limiting
* controlo temporário de acesso

### RabbitMQ

Responsável por:

* publicação de eventos
* processamento assíncrono
* desacoplamento entre redirect e analytics

---

## 🔐 Considerações de Segurança

O projecto inclui mecanismos básicos para reduzir abuso da API, nomeadamente:

* Rate limiting
* Validação das URLs
* Limitação do tamanho do short code
* Utilização de `SecureRandom` na geração dos códigos
* Execução do container da aplicação com utilizador não-root

A aplicação não pretende representar uma solução completa de segurança para um ambiente de produção.

---

## 📊 Observações sobre Performance

Os benchmarks apresentados foram realizados numa máquina local, onde:

```text
Application
PostgreSQL
Redis
RabbitMQ
k6
```

podem partilhar os mesmos recursos computacionais.

Consequentemente, os resultados servem principalmente para observar o comportamento da implementação sob carga e não para afirmar uma capacidade específica de produção.

O principal objectivo do teste foi observar:

* evolução do throughput
* comportamento da latência
* taxa de erros
* impacto do aumento da concorrência

---

## 🧠 Conceitos Explorados

Este projecto foi utilizado para aprofundar conceitos de:

* Clean Architecture
* Ports & Adapters
* Domain-Driven Design
* Cache-Aside
* Distributed Rate Limiting
* Redis Lua Scripts
* Message Queues
* Event-Driven Architecture
* Asynchronous Processing
* Idempotência
* Concorrência
* URL Expiration
* Analytics
* Horizontal Scaling
* Load Testing
* Performance Analysis
* Containerização

---

## 📚 Referência

O projecto foi desenvolvido como uma **implementação própria inspirada num desafio/conteúdo de System Design sobre URL Shortening e escalabilidade**.

A implementação, decisões técnicas, estrutura do código, testes e validação foram realizados no âmbito deste projecto.

---

## 👨‍💻 Autor

**Alfredo Baptista**

Backend Developer | Java · Spring Boot · System Design · System Integration


[GitHub](https://github.com/alfredobaptista) · [LinkedIn](https://www.linkedin.com/in/alfredobaptista/)

---

<p align="center">
  <strong>Java • Spring Boot • Redis • PostgreSQL • RabbitMQ • Docker</strong>
</p>

<p align="center">
  Desenvolvido com foco em arquitectura, performance e escalabilidade.
</p>
