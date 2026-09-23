# Product Management System

A fullstack monorepo for a Product Management System. The backend is a complete Spring Boot 3 REST API and the Vue 3 frontend provides a responsive product operations workspace.

## Technology stack

- Java 17 and Spring Boot 3.3
- Spring MVC, Bean Validation, Spring Data JPA and Hibernate
- PostgreSQL 16
- Flyway database migrations
- Springdoc OpenAPI / Swagger UI
- Docker Compose with PostgreSQL, backend and frontend services

## Project structure

```text
product-management/
├── backend/
│   ├── src/main/java/com/example/productmanagement/
│   ├── src/main/resources/db/migration/
│   ├── src/test/java/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                    # Vue 3 + Vite + Pinia application
├── docker-compose.yml
└── README.md
```

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

PostgreSQL is exposed on `localhost:55432` and the API is exposed on `localhost:18080`. Flyway creates the schema and inserts five sample products on the first startup.

If those ports are already in use, override them without changing the application:

```powershell
$env:SERVER_PORT = "18080"
$env:POSTGRES_PORT = "55432"
$env:VITE_API_BASE_URL = "http://localhost:18080"
docker compose up --build -d
```

The current development stack on this machine uses `http://localhost:18080` for the API and `localhost:55432` for PostgreSQL.

The frontend is available at `http://localhost:5173` when the Compose frontend service is enabled. Its browser-facing API URL is configured with `VITE_API_BASE_URL`. The interface is localized in Vietnamese and displays product prices in VND.

To stop the services:

```bash
docker compose down
```

To remove the database volume as well, use `docker compose down -v`. This permanently removes the local PostgreSQL data volume.

## Build and test locally

The local machine must have Java 17 and Maven 3.9+ installed. PostgreSQL must be available with the defaults from `backend/src/main/resources/application.yml`, or the datasource environment variables can be overridden.

```bash
cd backend
./mvnw clean verify
./mvnw spring-boot:run
```

For a development profile with SQL logging:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Swagger and OpenAPI

- Swagger UI: [http://localhost:18080/swagger-ui/index.html](http://localhost:18080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:18080/v3/api-docs](http://localhost:18080/v3/api-docs)

## API behavior

Base path: `/api/v1/products`

Analytics endpoints use `/api/v1/analytics` and calculate live inventory from non-deleted products. Historical trends remain empty until the scheduled daily snapshot job has captured at least two distinct dates.

- Product codes must follow `PRD-xxxx`, where `x` is a digit.
- Product code is immutable after creation.
- A soft-deleted product keeps its product code reserved because the database unique constraint is intentionally global.
- `price` must be greater than zero in API requests and is stored with two decimal places.
- Deleted products are soft-deleted and excluded from all read endpoints.
- List requests support `page`, `size`, `sort`, `keyword`, and `status` query parameters.
- All endpoints return the `ApiResponse<T>` envelope. List responses use `PageResponse<T>`.

## cURL smoke test

The commands below use `curl.exe` so they work in Windows PowerShell as well as common Unix shells.

### 1. Create

```bash
curl.exe -i -X POST http://localhost:8080/api/v1/products -H "Content-Type: application/json" -d "{\"productCode\":\"PRD-0100\",\"name\":\"Demo Laptop Stand\",\"description\":\"Aluminium adjustable stand\",\"price\":49.90,\"stockQuantity\":25,\"status\":\"ACTIVE\"}"
```

### 2. List with search and pagination

```bash
curl.exe "http://localhost:8080/api/v1/products?page=0&size=10&sort=name,asc&keyword=wireless&status=ACTIVE"
```

### 3. Get one product

```bash
curl.exe http://localhost:8080/api/v1/products/1
```

### 4. Update one product

```bash
curl.exe -i -X PUT http://localhost:8080/api/v1/products/1 -H "Content-Type: application/json" -d "{\"name\":\"Mechanical Keyboard K87 Pro\",\"description\":\"Updated product description\",\"price\":99.90,\"stockQuantity\":100,\"status\":\"ACTIVE\",\"version\":0}"
```

### 5. Soft-delete one product

```bash
curl.exe -i -X DELETE http://localhost:8080/api/v1/products/1
```

The commands are shown with `curl.exe` for Windows. On Linux/macOS, replace `curl.exe` with `curl`.

Updates use optimistic locking. Read the current `data.version` from `GET` first and send that value in the `PUT` request. A stale version returns `409 Conflict`.

## Configuration

The main configuration is in `backend/src/main/resources/application.yml`:

| Variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/product_management` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `product_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `product_password` | Database password |
| `SERVER_PORT` | `8080` | API port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Frontend origins, comma-separated |

## Production profile and health endpoints

Use `SPRING_PROFILES_ACTIVE=prod` in production and provide the datasource environment variables without relying on defaults. Copy `.env.example` to a local environment file and replace all example secrets.

The application uses a server-side session with an HttpOnly cookie for the admin workspace. Local development uses `admin` / `admin123` by default; these values are only demo credentials and must be replaced in production. Set `APP_API_USERNAME`, `APP_API_PASSWORD`, `SERVER_SESSION_COOKIE_SECURE=true` and a suitable `CORS_ALLOWED_ORIGINS` in production.

Authentication endpoints:

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/csrf`

The browser sends credentials with `include`, keeps no access credential in localStorage, and includes the CSRF token from the `XSRF-TOKEN` cookie on state-changing requests.

- Liveness/readiness health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Request correlation: every API response includes an `X-Request-Id` header; clients may provide their own bounded value.

## Testcontainers integration tests

`mvn verify` runs unit tests and PostgreSQL integration tests through Testcontainers. Docker Desktop must be running for the integration tests. If Docker is unavailable, use `mvn -Dtest=ProductMapperTest test` for the unit test only.
