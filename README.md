# Redis Demo — Spring Boot + PostgreSQL + Redis

A hands-on project for learning Redis fundamentals and integrating Redis caching with a Spring Boot application.

The main goal of this project is to understand **Redis as a backend/distributed-systems concept**, rather than relying immediately on framework abstractions.

---

## 🛠️ Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA
* Spring Data Redis
* PostgreSQL
* Redis
* Docker & Docker Compose
* Maven
* Lombok

---

## 🏗️ Architecture

The project uses PostgreSQL as the source of truth and Redis as a fast cache.

```text
                    ┌──────────────┐
                    │   Client     │
                    └──────┬───────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │   Spring Boot   │
                  │      API        │
                  └────────┬────────┘
                           │
                           ▼
                     ┌───────────┐
                     │   Redis   │
                     │   Cache   │
                     └─────┬─────┘
                           │
                     Cache Miss
                           │
                           ▼
                    ┌──────────────┐
                    │  PostgreSQL  │
                    │ Source of    │
                    │    Truth     │
                    └──────────────┘
```

---

## 🐳 Docker Services

The project runs PostgreSQL and Redis using Docker Compose.

### PostgreSQL

```yaml
postgres:
  image: postgres:17
  container_name: redis-demo-postgres
  environment:
    POSTGRES_DB: redis_demo
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  ports:
    - "5433:5432"
```

PostgreSQL is exposed on host port `5433`.

### Redis

```yaml
redis:
  image: redis:8
  container_name: redis-demo-redis
  ports:
    - "6379:6379"
```

Redis is exposed on port `6379`.

---

## ⚙️ Spring Boot Configuration

Example `application.yml`:

```yaml
spring:
  application:
    name: redis-demo

  datasource:
    url: jdbc:postgresql://localhost:5433/redis_demo
    username: postgres
    password: postgres
    hikari:
      pool-name: Hikari

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  data:
    redis:
      host: localhost
      port: 6379
```

---

# 📚 What I Learned

## 1. Redis Basics

Redis is an in-memory data store that can be used for:

* Caching
* Sessions
* Counters
* Rate limiting
* Queues
* Pub/Sub
* Distributed locks
* Temporary data

Redis stores data using keys and values.

For example:

```redis
SET name Behzad
GET name
```

---

## 2. Redis CLI

Connect to Redis running inside Docker:

```bash
sudo docker exec -it redis-demo-redis redis-cli
```

Useful commands:

```redis
SET name Behzad
GET name
DEL name
```

Check all keys:

```redis
KEYS *
```

> `KEYS *` is useful for learning/debugging, but should generally be avoided in production on large databases because it can block Redis while scanning the keyspace.

---

# ⏱️ TTL — Time To Live

Redis keys can automatically expire.

Example:

```redis
SET temporary "hello" EX 60
```

The key will expire after 60 seconds.

Check the TTL:

```redis
TTL temporary
```

Possible results:

```text
> 0    Remaining seconds
-1     Key exists without expiration
-2     Key does not exist
```

Another example:

```redis
SET test "hello" EX 30
TTL test
```

Eventually the key disappears:

```redis
GET test
```

returns:

```text
(nil)
```

---

# ☕ Spring Data Redis

The project uses:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

For basic String operations, Spring provides:

```java
StringRedisTemplate
```

For example:

```java
redisTemplate.opsForValue().set("name", "Behzad");
```

is conceptually equivalent to:

```redis
SET name Behzad
```

And:

```java
redisTemplate.opsForValue().get("name");
```

is equivalent to:

```redis
GET name
```

---

# 🔄 Serialization

Redis fundamentally stores bytes.

A Java object such as:

```java
Product
```

cannot simply be stored in Redis without converting it into a representation Redis can store.

In this project, a `Product` is converted to JSON:

```text
Java Product
     │
     ▼
 ObjectMapper
     │
     ▼
 JSON String
     │
     ▼
   Redis
```

Example:

```json
{
  "id": "77aef323-7c37-45ae-ac8e-72f88fe708c9",
  "name": "MacBook Pro",
  "price": 2000
}
```

When reading:

```text
Redis
  │
  ▼
JSON String
  │
  ▼
ObjectMapper
  │
  ▼
Java Product
```

---

# ⚡ Cache-Aside Pattern

The main Redis exercise in this project implements the **Cache-Aside** pattern.

The application first checks Redis.

```text
                 GET Product
                      │
                      ▼
                   Redis
                  /     \
               HIT       MISS
                │          │
                │          ▼
                │      PostgreSQL
                │          │
                │          ▼
                │        Product
                │          │
                │          ▼
                │         Redis
                │          │
                └────┬─────┘
                     ▼
                  Response
```

### Cache HIT

If the Product exists in Redis:

```text
Redis → Product → Response
```

PostgreSQL does not need to be queried.

### Cache MISS

If the Product isn't in Redis:

```text
Redis → MISS
         ↓
    PostgreSQL
         ↓
      Product
         ↓
       Redis
         ↓
     Response
```

---

# 🗝️ Redis Key Design

Products are stored using keys like:

```text
product:{uuid}
```

Example:

```text
product:77aef323-7c37-45ae-ac8e-72f88fe708c9
```

This provides a simple namespace for Product cache entries.

---

# ⏳ Product Cache TTL

Products are cached with a 5-minute TTL:

```java
redisTemplate.opsForValue()
        .set(key, json, Duration.ofMinutes(5));
```

Conceptually Redis receives:

```redis
SET product:<uuid> <json> EX 300
```

This means the cached Product automatically expires after five minutes.

---

# 🧪 Testing

## Create a Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","price":2000}'
```

The API returns a Product with its generated UUID.

---

## First GET — Cache Miss

```bash
curl http://localhost:8080/api/v1/products/<UUID>
```

Expected application log:

```text
CACHE MISS
Hibernate: select ...
```

The application retrieves the Product from PostgreSQL and stores it in Redis.

---

## Second GET — Cache Hit

Run the same request again:

```bash
curl http://localhost:8080/api/v1/products/<UUID>
```

Expected:

```text
CACHE HIT
```

There should be **no PostgreSQL SELECT** for the cached request.

---

# 🔍 Inspect Redis

Connect to Redis:

```bash
sudo docker exec -it redis-demo-redis redis-cli
```

List Product cache keys:

```redis
KEYS product:*
```

Read a cached Product:

```redis
GET product:<UUID>
```

Check expiration:

```redis
TTL product:<UUID>
```

Example:

```text
299
```

The exact value decreases as time passes.

---

# 🧠 Important Concepts

### PostgreSQL vs Redis

```text
PostgreSQL
    ↓
Source of truth
Persistent storage
Relational database
```

```text
Redis
    ↓
Fast temporary data
In-memory
Cache
```

Redis does **not** replace PostgreSQL in this architecture.

---

## Cache Hit vs Cache Miss

```text
CACHE HIT

Application → Redis → Data
```

Fast path.

```text
CACHE MISS

Application → Redis → PostgreSQL → Redis → Data
```

Slower path, but the result is cached for future requests.

---

# 🚧 Next Redis Topics

After the basic caching exercise, the next concepts to learn are:

* Redis Hashes
* Redis Lists
* Redis Sets
* Redis Sorted Sets
* Counters
* Rate limiting
* Pub/Sub
* Transactions
* Distributed locks
* Cache invalidation
* Cache expiration strategies
* Redis persistence
* Redis replication
* Redis Sentinel
* Redis Cluster
* Production Redis patterns

---

# 🎯 Learning Goal

The purpose of this project is not just to learn Spring Data Redis APIs.

The goal is to understand:

```text
What problem does Redis solve?
        ↓
Why use Redis instead of PostgreSQL?
        ↓
How does caching work?
        ↓
What is a cache hit?
        ↓
What is a cache miss?
        ↓
How does TTL work?
        ↓
How are objects serialized?
        ↓
What problems does caching introduce?
```

Once these concepts are understood, the same Redis principles can be applied to other backend frameworks such as NestJS, .NET, FastAPI, and others.
