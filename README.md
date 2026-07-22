# FixMyCity

Civic incident tracking system with role-based access (Citizen, Manager, Company, Admin), allowing citizens to report infrastructure issues and track their resolution.

---

# Features

## Authentication & Security

- User registration
- User login with JWT authentication
- Role-based access control
- Password reset via email
- Protected API endpoints
- User account activation/deactivation

## Citizen Features

- Create incident reports
- Add descriptions and images to incidents
- View submitted incidents
- Track incident status
- Browse available incident categories

## Administrator Features

- Manage users
- View user details
- Change user roles
- Enable/disable users
- Manage incident categories
- View audit logs

---

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

## Frontend

- React
- Vite
- JavaScript
- CSS

---

## Running Locally

```bash
# Build the jar
mvn clean package

# Start app + MySQL (+ Adminer)
docker compose up --build
```
- App: `http://localhost:3150`
- Swagger UI: `http://localhost:3100/swagger-ui.html`
- Adminer (DB viewer): `http://localhost:3101`

Local DB credentials live in `.env`

---

## Contributors

- Aleena C R
- Estere Hmeļinska
- Vladlens Medvedevs
- Faustas Alekna

---

### Definition of Done
Every new feature must include related unit and/or integration tests.
  
### License
This project was created as part of the Accenture Java Bootcamp.