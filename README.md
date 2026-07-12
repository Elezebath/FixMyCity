# FixMyCity

Civic incident tracking system with role-based access (Citizen, Manager, Employee, Admin), allowing citizens to report infrastructure issues and track their resolution.

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0** (Spring Framework 7)
- **Spring Data JPA** (Hibernate)
- **Spring Security** (BCrypt password hashing, stateless HTTP Basic auth)
- **MySQL 8.4** (Dockerized)
- **Springdoc OpenAPI 3.0.3** (Swagger UI)
- **Docker / Docker Compose**
- **Maven**
- **GitHub Actions** (CI/CD, self-hosted runner)

---

## Project Structure

```
FixMyCity
├── .github/workflows/
│   └── deploy.yml                 # CI/CD pipeline (self-hosted runner)
├── src/main/java/lv/acnbootcamp/fixmycity/
│   ├── FixmycityApplication.java  # Main entry point
│   ├── config/
│   │   ├── SecurityConfig.java    # Spring Security rules, password encoder, auth provider
│   │   └── OpenApiConfig.java     # Swagger/OpenAPI metadata
│   ├── controller/
│   │   ├── HealthController.java  # GET /ping - public health check
│   │   └── AuthController.java    # POST /api/auth/register
│   ├── service/
│   │   └── AuthService.java       # Registration business logic
│   ├── repository/
│   │   └── UserRepository.java    # Spring Data JPA repository for User
│   ├── entity/
│   │   ├── User.java              # User JPA entity (maps to "users" table)
│   │   └── Role.java              # Enum: CITIZEN, MANAGER, EMPLOYEE, ADMIN
│   ├── dto/
│   │   ├── RegisterRequest.java   # Registration request payload (validated)
│   │   └── UserResponse.java      # Registration response (excludes password)
│   ├── security/
│   │   ├── UserDetailsImpl.java        # Adapts User entity to Spring Security's UserDetails
│   │   └── UserDetailsServiceImpl.java # Loads users from DB during authentication
│   └── exception/
│       ├── EmailAlreadyExistsException.java
│       └── GlobalExceptionHandler.java # Maps exceptions to proper HTTP status codes
├── src/main/resources/
│   └── application.yaml           # MySQL connection, JPA, Swagger, server port config
├── Dockerfile                     # Copies pre-built jar into a runnable image
├── docker-compose.yml                    # app + mysql (+ adminer) services
├── .env                           # Local-only DB credentials (gitignored, not committed)
├── .dockerignore
└── pom.xml
```

---

## What's Done So Far

### 1. Project Setup
- Spring Boot 4.1.0 project generated via Spring Initializr (Maven, Java 21, YAML config).
- Dependencies: Spring Web, Spring Data JPA, MySQL Driver, Spring Security, Validation, Lombok, DevTools, Springdoc OpenAPI.

### 2. MySQL Database Setup
- `application.yaml` configured with env-variable-based JDBC connection (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`), defaulting to `localhost`/`root` for local dev.
- `compose.yml` defines an `app` service and a `mysql` service, networked together (`app` reaches MySQL via hostname `mysql`, not `localhost`, when running in Docker).
- `hibernate.ddl-auto: update` — auto-creates/updates tables from entities (fine for now; **should be changed to `validate`/migrations before production**).
- Adminer added as an optional service in `compose.yml` for visually inspecting the database at `http://localhost:8081` (server: `mysql`, user: `root`).

### 3. Docker Setup
- `Dockerfile` follows a **single-stage** build (based on the Team-6 project pattern): expects `target/*.jar` to already exist (built via `mvn clean package` *before* `docker build`), then just copies it into a JRE-based image. This keeps the Dockerfile simple and matches how the CI pipeline builds the app.
- `.dockerignore` excludes `.git`, `.idea`, `.env`, etc. from the build context.
- Server port is configurable via `SERVER_PORT` env variable (defaults to `3100`).

### 4. Spring Security
- `SecurityConfig`:
    - BCrypt password encoding.
    - Stateless sessions (`SessionCreationPolicy.STATELESS`) — no server-side session, standard for REST APIs.
    - CSRF disabled (not needed for a JSON REST API without cookie-based sessions).
    - Public endpoints: `/ping`, `/actuator/health`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/auth/register`.
    - `/api/incidents/**` → requires `CITIZEN`, `MANAGER`, or `ADMIN` role.
    - `/api/admin/**` → requires `ADMIN` role.
    - Everything else requires authentication.
    - **Currently uses HTTP Basic Auth** as a temporary mechanism for manual testing — will likely be replaced with JWT later.
- `User` entity + `UserRepository` + `UserDetailsServiceImpl`/`UserDetailsImpl` — authentication is backed by the database (no more default in-memory Spring Security user).
- `POST /api/auth/register` — public registration endpoint. Validates input (`@Valid`), hashes passwords before storing, rejects duplicate emails with `409 Conflict`.

### 5. OpenAPI / Swagger
- Available at `http://localhost:<port>/swagger-ui.html` once the app is running.
- Auto-documents any `@RestController` — no extra work needed when adding new endpoints.

### 6. CI/CD (`deploy.yml`)
- Triggered on push to `main`/`develop`, or manually via `workflow_dispatch`.
- Runs on a **self-hosted runner** (not yet provisioned — pipeline will stay queued until a runner is registered).
- Builds the jar with Maven, then runs `docker compose up -d --build`.
- DB password for deployment is pulled from a **GitHub Actions secret** (`DB_PASSWORD`), written into a `.env` file on the runner at deploy time — **not** the same password used in local `.env`, and never committed to the repo.
- Verifies deployment by polling container logs for the Spring Boot startup confirmation line.

---

## What's NOT Done Yet

- **Self-hosted runner** is not provisioned/registered — `deploy.yml` will not actually run until this is set up.
- **Portal deployment configuration** (`portal.acnbootcamp.lv`) — not yet connected; need to check with bootcamp docs/mentor on exact requirements.
- **Test infrastructure** (JUnit 5 + Mockito) — default `FixmycityApplicationTests` currently fails when run without a live MySQL instance (`@SpringBootTest` tries to load a real DataSource). Use `mvn clean package -DskipTests` for now. **This is being set up next** — likely via an H2 in-memory profile or Testcontainers for integration tests.
- **JWT-based authentication** — currently using HTTP Basic as a placeholder.
- **Core domain entities** (`Incident`, etc.) — not yet implemented. `User`/auth is the only domain logic so far.
- **Maven wrapper (`.mvn/`) may be missing** in some local copies of the project — if `./mvnw` doesn't work, regenerate it with `mvn wrapper:wrapper -Dmaven=3.9.6`.

---

## Running Locally

```bash
# Build the jar (skip tests until test infra is set up)
mvn clean package -DskipTests

# Start app + MySQL (+ Adminer)
docker compose up --build
```

- App: `http://localhost:3100`
- Swagger UI: `http://localhost:3100/swagger-ui.html`
- Adminer (DB viewer): `http://localhost:8081` (server: `mysql`, user: `root`, password: see local `.env`)

Local DB credentials live in `.env` (gitignored — ask a teammate or check onboarding notes if you don't have one set up, or just create your own with any local password).

## Testing an Endpoint

```bash
curl -X POST http://localhost:3100/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","fullName":"Test User","role":"CITIZEN"}'
```