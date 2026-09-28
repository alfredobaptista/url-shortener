# URL Shortener

## Sobre o projecto

O URL Shortener é um serviço para criação e redireccionamento de URLs curtas, desenvolvido com foco na exploração de **escalabilidade, performance, caching, rate limiting e processamento assíncrono**.

O projecto foi desenvolvido como uma implementação independente inspirada num desafio de System Design apresentado num conteúdo sobre construção de um URL Shortener orientado à escalabilidade. A arquitectura, implementação, testes e decisões técnicas foram desenvolvidos no âmbito deste projecto.

O serviço permite:

* criar URLs curtas;
* redireccionar para a URL original;
* aplicar rate limiting na criação de URLs;
* utilizar cache para reduzir acessos à base de dados;
* registar acessos de forma assíncrona;
* consultar métricas básicas de utilização;
* eliminar URLs existentes.

---

## Contexto

Um URL Shortener parece, à primeira vista, um sistema simples: receber uma URL, gerar um código curto e redireccionar o utilizador.

O desafio aumenta quando o sistema precisa de lidar com um volume elevado de redireccionamentos.

O caminho de redireccionamento é, por isso, tratado como o principal caminho crítico da aplicação. A arquitectura utiliza caching para reduzir a dependência do PostgreSQL e processamento assíncrono para retirar o registo de analytics do fluxo principal da resposta.

O projecto também explora mecanismos necessários para evolução futura da aplicação, como rate limiting e escalabilidade horizontal.

---

## Arquitectura

A implementação actual segue uma abordagem baseada em **Clean Architecture** e **Ports and Adapters**, separando o domínio, os casos de uso e os mecanismos externos de persistência, cache e mensageria.

### Arquitectura actual

```text
                         Client
                    Bruno / Browser / k6
                           │
                           ▼
                  ┌────────────────────┐
                  │  URL Shortener API │
                  │     Spring Boot    │
                  └──────┬─────┬───────┘
                         │     │
             ┌───────────┘     └────────────┐
             ▼                              ▼
       ┌───────────┐                  ┌────────────┐
       │   Redis   │                  │ PostgreSQL │
       │           │                  │            │
       │ URL Cache │                  │ URLs       │
       │ Rate Limit│                  │ Analytics  │
       └───────────┘                  └────────────┘
                         │
                         ▼
                  ┌────────────┐
                  │  RabbitMQ  │
                  │            │
                  │ Analytics  │
                  │   Events   │
                  └─────┬──────┘
                        │
                        ▼
                ┌──────────────────┐
                │ Analytics Consumer│
                └────────┬─────────┘
                         │
                         ▼
                    PostgreSQL
```

A API permanece stateless relativamente ao processamento das requisições, enquanto o estado necessário é mantido nos serviços externos.

---

## Fluxo de redireccionamento

O redireccionamento foi desenhado para minimizar o acesso ao PostgreSQL.

```text
Client
  │
  │ GET /{shortCode}
  ▼
URL Shortener API
  │
  ▼
Redis
  │
  ├── Cache Hit ──────────────► 302 Redirect
  │
  └── Cache Miss
          │
          ▼
      PostgreSQL
          │
          ▼
       Redis
          │
          ▼
      302 Redirect

Em paralelo:

URL Shortener API
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

O evento de analytics não bloqueia o redireccionamento. Se a publicação do evento falhar, a aplicação mantém a resposta de redireccionamento.

---

## Decisões de arquitectura

### Cache-Aside com Redis

O Redis é utilizado como cache para o mapeamento entre o código curto e a URL original.

No fluxo de redireccionamento:

1. a aplicação consulta o Redis;
2. em caso de cache hit, a URL é devolvida directamente;
3. em caso de cache miss, a aplicação consulta o PostgreSQL;
4. o resultado é colocado no Redis;
5. o redireccionamento é efectuado.

O TTL do cache é definido de acordo com a validade da URL. Para URLs sem data de expiração, é utilizado um TTL padrão.

Esta abordagem reduz a frequência de consultas ao PostgreSQL no caminho crítico.

### Rate Limiting

A criação de URLs possui um mecanismo de controlo de requisições baseado em Redis.

O contador é incrementado através de um script Lua executado atomicamente no Redis.

A implementação actual utiliza uma janela de:

```text
10 requisições / minuto / client key
```

Quando o limite é excedido, a API responde com:

```text
HTTP 429 Too Many Requests
```

A utilização de uma operação atómica evita condições de corrida entre múltiplas requisições concorrentes.

### Processamento assíncrono

Os acessos às URLs são publicados como eventos no RabbitMQ.

O fluxo é:

```text
Redirect
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

Desta forma, a persistência do analytics não precisa de fazer parte do caminho síncrono do redireccionamento.

### Analytics

Cada redireccionamento gera um evento de analytics contendo o código curto e o momento do acesso.

A aplicação disponibiliza informações básicas:

* total de acessos;
* primeiro acesso;
* último acesso.

O armazenamento dos eventos é efectuado de forma assíncrona pelo consumer.

### Geração de Short Codes

Os códigos curtos são gerados através de um alfabeto Base62:

```text
a-z
A-Z
0-9
```

Cada código possui actualmente 6 caracteres.

A geração utiliza `SecureRandom`, evitando uma sequência previsível de códigos.

---

## Estrutura da aplicação

A aplicação está organizada de acordo com a separação entre domínio, casos de uso e adaptadores externos.

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
│       ├── generator
│       ├── messaging
│       ├── persistence
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
└── domain
    ├── exception
    ├── model
    └── valueobject
```

O domínio não depende directamente de Spring, Redis, RabbitMQ ou PostgreSQL.

As dependências externas são introduzidas através das portas da camada de aplicação e implementadas pelos respectivos adapters.

---

## API

### Criar URL

```http
POST /api/v1/urls
```

Request:

```json
{
  "originalUrl": "https://example.com",
  "expiresAt": null
}
```

Response:

```json
{
  "shortUrl": "bbVAZ9"
}
```

### Redireccionar

```http
GET /{shortCode}
```

Exemplo:

```http
GET /bbVAZ9
```

Response:

```text
HTTP 302 Found
Location: https://example.com
```

### Consultar analytics

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

### Eliminar URL

```http
DELETE /api/v1/urls/{shortCode}
```

A operação remove o registo persistido e invalida a entrada correspondente no Redis.

---

## Testes

A aplicação possui **35 testes automatizados**, cobrindo as principais regras de domínio, casos de uso e componentes da aplicação.

A organização dos testes segue o padrão:

```text
Arrange → Act → Assert
```

Entre os cenários testados estão:

* criação de URLs;
* rate limiting;
* persistência de URLs;
* utilização do cache;
* cache miss;
* URLs expiradas;
* URLs inexistentes;
* falhas na publicação de analytics;
* eliminação de URLs;
* analytics;
* validação de `ShortCode`;
* validação de `OriginalUrl`;
* geração de códigos Base62;
* inicialização do contexto Spring.

Resultado actual:

```text
Tests run: 35
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

## Benchmark de performance

Foi utilizado o k6 para avaliar o comportamento do endpoint de redireccionamento sob diferentes níveis de concorrência.

O teste foi realizado localmente utilizando o mesmo código curto e com o cache activo.

O teste não representa capacidade de produção. O objectivo foi observar o comportamento do sistema à medida que a concorrência aumenta.

### 10 VUs

```text
Requests: 48 012
Throughput: ~1 600 req/s
HTTP failures: 0%
p95: 12,61 ms
```

### 50 VUs

```text
Requests: 77 635
Throughput: ~2 585 req/s
HTTP failures: 0%
p95: 45,71 ms
```

### 100 VUs

```text
Requests: 65 845
Throughput: ~2 184 req/s
HTTP failures: 0%
p95: 129,77 ms
```

Em todos os cenários, as verificações do teste confirmaram respostas HTTP `302`.

---

## Resultados

Os testes demonstraram que o sistema conseguiu manter respostas correctas sem erros HTTP durante os cenários executados.

À medida que a concorrência aumentou, o throughput deixou de crescer de forma linear e a latência aumentou.

O cenário de 100 VUs apresentou uma utilização significativamente maior dos recursos da máquina local, indicando que a capacidade computacional do ambiente passou a influenciar o comportamento observado.

Estes resultados devem ser interpretados como **benchmarks do ambiente de desenvolvimento**, e não como uma capacidade garantida de produção.

---

## Limitações

O benchmark apresenta algumas limitações:

* execução numa única máquina;
* uma única instância da aplicação;
* ambiente local;
* Redis, PostgreSQL e RabbitMQ executados localmente;
* ausência de Load Balancer;
* ausência de múltiplas instâncias da API;
* k6 e aplicação a partilhar os recursos da mesma máquina;
* utilização de um cenário de cache quente;
* teste concentrado principalmente no endpoint de redireccionamento.

Consequentemente, os valores obtidos não devem ser utilizados como estimativa directa de capacidade numa infraestrutura de produção.

---

## Evolução da arquitectura

Uma evolução natural do sistema consiste em executar múltiplas instâncias da API atrás de um Load Balancer.

```text
                         Client
                           │
                           ▼
                    ┌──────────────┐
                    │ Load Balancer│
                    └──────┬───────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
        ┌─────────┐   ┌─────────┐   ┌─────────┐
        │ API #1  │   │ API #2  │   │ API #3  │
        └────┬────┘   └────┬────┘   └────┬────┘
             │             │             │
             └─────────────┼─────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
         ┌────────┐               ┌────────────┐
         │ Redis  │               │ PostgreSQL │
         └────────┘               └────────────┘
                                        ▲
                                        │
                                   ┌────┴─────┐
                                   │ RabbitMQ │
                                   └──────────┘
```

A API foi estruturada de forma a permitir esta evolução sem depender de estado local entre instâncias.

Possíveis evoluções incluem:

* escalabilidade horizontal da API;
* Load Balancer;
* Redis partilhado;
* optimização e dimensionamento do PostgreSQL;
* escalabilidade dos consumers de RabbitMQ;
* testes de carga num ambiente distribuído.

---

## Execução local

### Pré-requisitos

É necessário ter instalado:

```text
Java 21
Docker
Docker Compose
```

### Infraestrutura

Iniciar os serviços de infraestrutura:

```bash
docker compose up -d
```

### Aplicação

Executar:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

### Testes

Executar todos os testes:

```bash
./mvnw clean test
```

### Build

```bash
./mvnw clean package
```
