# Ambulancia · Plinio

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

**Plinio** is the backend service for **Ambulancia** — a Spring Boot API focused on **hospitals** and **addresses**: who is available, where they are, and how capacity looks on the ground. Built for clarity, validation-first payloads, and a clean layered layout so the product can grow into routing and dispatch without rewriting the core.

---

## Why this exists

Emergency and pre-hospital systems need a **single source of truth** for facilities and locations. This service models hospitals (name, availability, bed counts, linked address) and neighborhoods as first-class resources, exposed over a small, predictable REST surface.

---

## Stack

| Layer        | Choice |
|-------------|--------|
| Runtime     | Java 21 |
| Framework   | Spring Boot 4 (Web MVC, Data JPA) |
| Database    | PostgreSQL |
| Mapping     | MapStruct |
| Validation  | Jakarta Validation |

---

## Project layout

```
ambulancia/
└── plinio/                 ← Spring Boot application
    ├── src/main/java/com/ambulancia/plinio/
    │   ├── controller/     REST endpoints
    │   ├── service/        Business logic
    │   ├── repository/     JPA repositories
    │   ├── model/          Entities (Hospital, Address)
    │   ├── dto/            Request/response contracts
    │   └── mapper/         MapStruct mappers
    └── pom.xml
```

---

## API overview

Base path: context root as configured (default is `/`).

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/hospitals` | Create a hospital |
| `GET` | `/hospitals` | List all hospitals |
| `GET` | `/hospitals/{id}` | Get one hospital |
| `PUT` | `/hospitals/{id}` | Full update |
| `PATCH` | `/hospitals/{id}/address` | Update hospital address only |
| `DELETE` | `/hospitals/{id}` | Remove a hospital |
| `POST` | `/addresses` | Create an address |
| `GET` | `/addresses` | List addresses |
| `GET` | `/addresses/{id}` | Get one address |
| `PUT` | `/addresses/{id}` | Update an address |
| `DELETE` | `/addresses/{id}` | Remove an address |

Requests use JSON bodies validated with Jakarta Validation where DTOs declare constraints.

---

## Getting started

### Prerequisites

- **JDK 21**
- **PostgreSQL** reachable from your machine
- **Maven** (or use the included `./mvnw` wrapper)

### Database

Create a database for the app, then point Spring at it. Typical options:

- **Environment variables** (recommended for local dev):

  ```bash
  export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/plinio
  export SPRING_DATASOURCE_USERNAME=your_user
  export SPRING_DATASOURCE_PASSWORD=your_password
  ```

- Or extend `plinio/src/main/resources/application.yaml` with a `spring.datasource` block (keep secrets out of git).

JPA will manage schema according to your configuration (e.g. `ddl-auto` if you add it).

### Run

From the `plinio` directory:

```bash
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw -q package
java -jar target/plinio-0.0.1-SNAPSHOT.jar
```

The application name in config is **`plinio`**.

### Tests

```bash
./mvnw test
```

---

## Development notes

- **MapStruct** runs at compile time; use `./mvnw compile` after changing mappers or DTOs if your IDE does not trigger annotation processing.
- **DevTools** is on the classpath for a smoother local loop (optional, runtime scope).

---

## License

Specify your license here once the project ships one.

---

*Plinio — named for the idea of mapping the territory with care. Built for Ambulancia.*
