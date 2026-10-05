# 🔗 URL Shortener

<p align="center">
  <strong>Scalable URL Shortening Service</strong>
</p>

<p align="center">
  Serviço de encurtamento de URLs desenvolvido com Java e Spring Boot,
  com foco em performance, caching, processamento assíncrono,
  resiliência e escalabilidade horizontal.
</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge\&logo=springboot\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?style=for-the-badge\&logo=postgresql\&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge\&logo=redis\&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-4-FF6600?style=for-the-badge\&logo=rabbitmq\&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge\&logo=apachemaven\&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge\&logo=junit5\&logoColor=white)

</p>

<p align="center">
  <a href="https://github.com/alfredobaptista/url-shortener">GitHub Repository</a>
</p>

---

## 📌 Sobre o Projecto

O **URL Shortener** é uma implementação própria de um serviço de encurtamento de URLs, desenvolvida a partir de um desafio/conteúdo de **System Design** sobre a construção de sistemas capazes de lidar com elevados volumes de tráfego.

Mais do que implementar apenas o encurtamento e redireccionamento de URLs, o projecto explora problemas reais de engenharia de software e sistemas distribuídos:

* ⚡ Performance e baixa latência
* 🗄️ Persistência
* 🚀 Caching
* 🛡️ Rate limiting
* 🔄 Processamento assíncrono
* 📨 Mensageria
* 📊 Analytics
* ⏱️ Expiração de URLs
* 🔐 Validação e protecção contra abuso
* 📈 Escalabilidade horizontal
* 🧱 Clean Architecture / Ports & Adapters

A aplicação mantém a lógica de negócio isolada dos detalhes de infraestrutura, permitindo substituir mecanismos como PostgreSQL, Redis ou RabbitMQ sem acoplar essas tecnologias ao domínio.

---

# 🏗️ Arquitectura

A arquitectura geral do sistema é composta por quatro elementos principais:

```text
                         ┌──────────────────┐
                         │      Client      │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │  Spring Boot API │
                         └────────┬─────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
        ┌───────────┐       ┌────────────┐      ┌───────────┐
        │   Redis   │       │ PostgreSQL │      │ RabbitMQ  │
        └───────────┘       └────────────┘      └─────┬─────┘
                                                     │
                                                     ▼
                                             ┌───────────────┐
                                             │    Consumer   │
                                             └───────┬───────┘
                                                     │
                                                     ▼
                                               PostgreSQL
```

O sistema separa o **caminho crítico do redireccionamento** do processamento assíncrono das analytics.

---

## 🔗 Fluxo de criação

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
Validate URL
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

---

## 🚀 Fluxo de redireccionamento

```text
Client
  │
  ▼
GET /{shortCode}
  │
  ▼
Redis Cache
  │
  ├── HIT ────────────────────────┐
  │                               │
  └── MISS                        │
       │                          │
       ▼                          │
   PostgreSQL                     │
       │                          │
       ▼                          │
   Check Expiration               │
       │                          │
       ▼                          │
   Cache Redis                    │
       │                          │
       └──────────────────────────┘
                    │
                    ▼
             Publish Analytics
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

O evento de analytics é processado de forma assíncrona.

Se a publicação do evento falhar, o redireccionamento não é bloqueado.

---

# 🎯 Principais Decisões Técnicas

## 🚀 Redis — Cache-Aside

O Redis funciona como camada de cache para reduzir o número de consultas ao PostgreSQL durante os redireccionamentos.

```text
Request
   │
   ▼
 Redis
   │
   ├── HIT ──► Return URL
   │
   └── MISS
        │
        ▼
    PostgreSQL
        │
        ▼
      Redis
        │
        ▼
    Return URL
```

O cache utiliza TTL para controlar a validade das entradas.

Quando uma URL possui uma data de expiração, o TTL do Redis é calculado de acordo com essa validade.

Para URLs sem expiração, é utilizado um TTL configurado pela aplicação.

O PostgreSQL continua a representar a **fonte de verdade persistente**.

---

## 🛡️ Redis + Lua — Rate Limiting

O controlo de abuso utiliza Redis juntamente com um script Lua para executar as operações de contagem e expiração de forma atómica.

Configuração actual:

```text
10 requests / minuto / client
```

Quando o limite é excedido:

```http
429 Too Many Requests
```

A utilização de Lua permite que as operações necessárias sejam executadas atomicamente, reduzindo condições de corrida entre múltiplas operações Redis.

---

## 📨 RabbitMQ — Processamento Assíncrono

As analytics de acesso não são persistidas directamente no caminho crítico do redirect.

O fluxo principal é:

```text
HTTP Request
     │
     ▼
Redirect Service
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
RegisterRedirectAnalyticsUseCase
    │
    ▼
Analytics Repository
    │
    ▼
PostgreSQL
```

Esta abordagem reduz o trabalho executado durante o redirect e desacopla o processamento das métricas da resposta HTTP.

---

## 🔢 Base62

Os short codes são gerados utilizando um conjunto de 62 caracteres:

```text
a-z
A-Z
0-9
```

A configuração actual utiliza códigos de **6 caracteres**.

Exemplo:

```text
bbVAZ9
```

A geração utiliza `SecureRandom`.

O espaço teórico de combinações é:

```text
62⁶ = 56.800.235.584
```

A tabela `urls` possui uma restrição `UNIQUE` sobre `short_code`, protegendo a integridade dos dados perante colisões.

---

## ⏱️ Expiração de URLs

Uma URL pode possuir uma data de expiração:

```json
{
  "originalUrl": "https://www.example.com",
  "expiresAt": "2026-10-05T20:00:00"
}
```

Quando a URL expira, a aplicação retorna:

```http
410 Gone
```

Exemplo:

```json
{
  "timestamp": "2026-10-05T20:00:00",
  "status": 410,
  "error": "Gone",
  "message": "Esta URL curta expirou."
}
```

O TTL do Redis também é ajustado de acordo com a validade da URL, evitando que uma entrada permaneça indefinidamente no cache após a sua expiração.

---

# 🧱 Arquitectura de Código

O projecto segue **Clean Architecture / Ports & Adapters**, separando regras de negócio, casos de uso e detalhes de infraestrutura.

```text
src/main/java/com/github/alfredobaptista/

├── adapter
│   ├── in
│   │   ├── handler
│   │   ├── messaging
│   │   └── web
│   │       └── dto
│   │
│   └── out
│       ├── cache
│       ├── generator
│       ├── messaging
│       ├── persistence
│       │   ├── adapter
│       │   ├── entity
│       │   └── repository
│       └── security
│
├── application
│   ├── dto
│   ├── exception
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

### Separação de responsabilidades

```text
┌──────────────────────────┐
│          Domain          │
│     Regras de negócio    │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Application        │
│   Use Cases / Ports      │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│         Adapters         │
│                          │
│ REST                     │
│ PostgreSQL               │
│ Redis                    │
│ RabbitMQ                 │
│ Base62                   │
└──────────────────────────┘
```

A camada de domínio não depende de:

* Spring
* Spring Data
* JPA
* Redis
* RabbitMQ

Isto mantém as regras de negócio independentes da infraestrutura.

---

# 🔌 API

## Criar URL

```http
POST /api/v1/urls
Content-Type: application/json
```

### Request

```json
{
  "originalUrl": "https://www.example.com"
}
```

### Response

```json
{
  "shortCode": "bbVAZ9"
}
```

### Criar URL com expiração

```json
{
  "originalUrl": "https://www.example.com",
  "expiresAt": "2026-10-05T20:00:00"
}
```

### Possíveis respostas

```text
201 Created
400 Bad Request
429 Too Many Requests
```

---

## Redireccionar

```http
GET /{shortCode}
```

### Response

```http
HTTP/1.1 302 Found
Location: https://www.example.com
```

### Possíveis respostas

```text
302 Found
404 Not Found
410 Gone
```

Ferramentas como Bruno ou Postman podem apresentar `200 OK` caso estejam configuradas para seguir automaticamente o header `Location`.

Para testar directamente o `302`, os redirects automáticos devem estar desactivados.

---

## Consultar Analytics

```http
GET /api/v1/urls/{shortCode}/analytics
```

### Response

```json
{
  "shortCode": "bbVAZ9",
  "totalClicks": 2,
  "firstAccessedAt": "2026-10-05T19:13:41.733821",
  "lastAccessedAt": "2026-10-05T19:14:34.953744"
}
```

As métricas são registadas de forma assíncrona através do RabbitMQ.

---

## Eliminar URL

```http
DELETE /api/v1/urls/{shortCode}
```

### Response

```http
204 No Content
```

A operação:

1. verifica a existência da URL;
2. remove a URL do PostgreSQL;
3. invalida a entrada correspondente no Redis.

As analytics não possuem uma foreign key para `urls`. Desta forma, o histórico de acessos pode ser preservado mesmo depois da eliminação da URL.

---

# 🧪 Testes

O projecto utiliza:

* **JUnit 5**
* **Mockito**

Execução:

```bash
./mvnw clean test
```

Resultado actual:

```text
36 tests
0 failures
0 errors
```

Os testes cobrem principalmente:

* criação de URLs;
* redireccionamento;
* eliminação;
* analytics;
* geração de short codes;
* `OriginalUrl`;
* `ShortCode`;
* expiração;
* rate limiting;
* regras de negócio;
* tratamento de falhas na publicação de analytics.

---

# ⚡ Testes de Carga

Os testes de carga foram realizados com **k6**, utilizando o endpoint:

```http
GET /{shortCode}
```

Os redirects foram desactivados no cliente de teste:

```javascript
{
    redirects: 0
}
```

Desta forma, o benchmark mede o desempenho da API de redireccionamento sem seguir o destino externo.

## Resultados

| VUs | Duração | Requests |      Throughput |    Média |           p95 | Erros |
| --: | ------: | -------: | --------------: | -------: | ------------: | ----: |
|  10 |     30s |   90.921 | **3.030 req/s** |  2,99 ms |   **5,42 ms** |    0% |
|  50 |     30s |  102.766 | **3.422 req/s** | 14,29 ms |  **33,82 ms** |    0% |
| 100 |     30s |   33.318 | **1.106 req/s** | 89,81 ms | **249,01 ms** |    0% |

Todos os testes cumpriram os thresholds definidos:

```text
http_req_failed < 1%
p(95) < 500 ms
```

### Observações

Com 10 VUs, a aplicação apresentou latência muito baixa, com:

```text
p95 = 5,42 ms
```

Com 50 VUs, o sistema atingiu o maior throughput observado:

```text
≈ 3.422 req/s
p95 = 33,82 ms
```

Com 100 VUs, observou-se uma degradação significativa da latência e uma redução do throughput:

```text
≈ 1.106 req/s
p95 = 249,01 ms
```

Apesar da degradação sob maior concorrência, a taxa de erros permaneceu em:

```text
0%
```

Os resultados demonstram o comportamento da implementação sob diferentes níveis de concorrência no ambiente local utilizado para os testes.

> **Nota:** estes valores são resultados de um benchmark local e não representam capacidade garantida de produção. O desempenho real depende dos recursos de CPU, memória, JVM, rede, PostgreSQL, Redis, RabbitMQ e infraestrutura de execução.

---

# 📈 Escalabilidade

A arquitectura foi concebida para permitir a evolução de uma única instância da API para múltiplas instâncias.

## Arquitectura actual

```text
                    Client
                      │
                      ▼
               ┌─────────────┐
               │     API     │
               └──────┬──────┘
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
       Redis      PostgreSQL   RabbitMQ
```

## Evolução horizontal

```text
                         Client
                           │
                           ▼
                   ┌───────────────┐
                   │ Load Balancer │
                   └───────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
           API #1        API #2        API #3
              │            │            │
              └────────────┼────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
           Redis       PostgreSQL    RabbitMQ
```

Como Redis, PostgreSQL e RabbitMQ são externos às instâncias da API, a camada HTTP pode evoluir para múltiplas instâncias partilhando os mesmos serviços de dados e mensageria.

## Possíveis evoluções

* Load Balancer
* múltiplas instâncias da API
* Redis Cluster
* PostgreSQL Read Replicas
* múltiplos consumers RabbitMQ
* observabilidade distribuída
* métricas e tracing
* políticas de retenção de analytics
* optimização adicional da geração de códigos
* estratégias de retry
* Dead Letter Queues

Estas funcionalidades representam **possíveis evoluções arquitecturais** e não capacidades actualmente implementadas ou garantidas em produção.

---

# 🛠️ Stack Tecnológica

| Categoria       | Tecnologia                            |
| --------------- | ------------------------------------- |
| Linguagem       | Java 21                               |
| Framework       | Spring Boot 4.1.1                     |
| API             | REST                                  |
| Persistência    | PostgreSQL 18                         |
| Cache           | Redis 7                               |
| Mensageria      | RabbitMQ 4                            |
| Migrações       | Flyway                                |
| Testes          | JUnit 5 · Mockito                     |
| Load Testing    | k6                                    |
| Containerização | Docker · Docker Compose               |
| Build           | Maven                                 |
| Arquitectura    | Clean Architecture · Ports & Adapters |

---

# 🐳 Execução com Docker

## Pré-requisitos

* Docker
* Docker Compose
* Git

### Clonar o projecto

```bash
git clone git@github.com:alfredobaptista/url-shortener.git
cd url-shortener
```

### Construir e iniciar

```bash
docker compose up --build
```

A stack é composta por:

```text
┌─────────────────┐
│ Spring Boot API │
└────────┬────────┘
         │
    ┌────┼────┐
    ▼    ▼    ▼
 PostgreSQL Redis RabbitMQ
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

RabbitMQ Management:

```text
http://localhost:15672
```

### Credenciais locais do RabbitMQ

```text
username: admin
password: admin123
```

> As credenciais apresentadas destinam-se ao ambiente local de desenvolvimento e não devem ser utilizadas directamente num ambiente de produção.

### Healthchecks

O Docker Compose utiliza healthchecks para:

* PostgreSQL
* Redis
* RabbitMQ

A aplicação depende desses serviços estarem saudáveis antes de iniciar.

---

# 💻 Execução Local

Para executar a aplicação directamente com Maven:

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

As migrações da base de dados são executadas automaticamente através do **Flyway**.

---

# 🗄️ Serviços

## PostgreSQL

Responsável pela persistência de:

* URLs
* analytics
* timestamps
* estado persistente da aplicação

## Redis

Responsável por:

* cache das URLs;
* rate limiting;
* dados temporários.

## RabbitMQ

Responsável por:

* publicação de eventos;
* processamento assíncrono;
* desacoplamento entre redirect e analytics.

---

# 🔐 Considerações de Segurança

O projecto inclui mecanismos básicos para reduzir abuso da API:

* Rate limiting;
* validação das URLs;
* limitação do tamanho do short code;
* `SecureRandom` na geração dos códigos;
* execução do container com utilizador não-root;
* validação dos dados recebidos pela API.

A solução representa mecanismos básicos de protecção e não pretende substituir uma estratégia completa de segurança para ambientes de produção.

---

# 📊 Observações sobre Performance

Os benchmarks foram executados num ambiente local onde:

```text
Spring Boot
PostgreSQL
Redis
RabbitMQ
k6
```

podem partilhar os mesmos recursos computacionais.

O principal objectivo dos testes foi observar:

* evolução do throughput;
* comportamento da latência;
* taxa de erros;
* impacto do aumento da concorrência;
* comportamento do cache durante os redireccionamentos.

Os resultados devem ser interpretados como **benchmarks da implementação**, e não como garantias de capacidade para produção.

---

# 🧠 Conceitos Explorados

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
* Concorrência
* URL Expiration
* Analytics
* Horizontal Scaling
* Load Testing
* Performance Analysis
* Containerização
* Resiliência

---

# 📚 Referência

O projecto foi desenvolvido como uma **implementação própria inspirada num desafio/conteúdo de System Design sobre URL Shortening e escalabilidade**.

A arquitectura, decisões técnicas, implementação, testes e benchmarks foram desenvolvidos no âmbito deste projecto.

---

# 👨‍💻 Autor

**Alfredo Baptista**

Backend Developer | Java · Spring Boot · System Design · System Integration

<p>
  <a href="https://github.com/alfredobaptista">GitHub</a>
  ·
  <a href="https://www.linkedin.com/in/alfredobaptista/">LinkedIn</a>
</p>

---

<p align="center">
  <strong>Java • Spring Boot • PostgreSQL • Redis • RabbitMQ • Docker</strong>
</p>

<p align="center">
  Desenvolvido com foco em arquitectura, performance e escalabilidade.
</p>
