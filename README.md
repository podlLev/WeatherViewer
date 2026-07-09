<p align="center">
  <img src="src/main/resources/static/images/weather-app.png" width="100" alt="WeatherViewer logo">
</p>
<h1 align="center">WeatherViewer</h1>
 
<p align="center"><strong>A Spring Boot web app for tracking weather and forecasts across your favorite locations.</strong></p>
<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.5">
  <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL 17">
  <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white" alt="Redis 7">
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white" alt="Docker ready">
</p>
 
<p align="center">
  <a href="#overview">Overview</a> ·
  <a href="#features">Features</a> ·
  <a href="#tech-stack">Tech Stack</a> ·
  <a href="#getting-started">Getting Started</a> ·
  <a href="#api-documentation">API Docs</a> ·
  <a href="#running-tests">Testing</a> ·
  <a href="#project-structure">Structure</a>
</p>

---

## Overview

WeatherViewer is a personal weather dashboard for tracking the places you care about. Sign up, search for any city, and save it — your dashboard then shows current conditions for every saved location at a glance, with hourly and daily forecasts just a click away. Mark your most-checked spots as favorites to keep them front and center. Live weather and location data come from the OpenWeatherMap API, and Redis caching keeps everything running smoothly behind the scenes.

## Features

- **Authentication** — sign-up and sign-in with Spring Security, BCrypt password hashing, and role-based access (`USER` / `ADMIN`)
- **Location search** — look up cities by name via the OpenWeatherMap Geocoding API and save them to your account
- **Dashboard** — view current weather for all saved locations, sortable by date added, name, or favorite status
- **Favorites** — mark/unmark locations as favorites and filter the dashboard to show only those
- **Forecasts** — hourly and daily forecast views for any saved location
- **User profile** — update account details from a dedicated profile page
- **REST API** — JSON endpoints for users, locations, and weather data, documented with OpenAPI/Swagger
- **Caching** — weather, forecast, and geocoding responses are cached (Redis in production, in-memory for local/dev) to reduce external API calls
- **Database migrations** — schema is version-controlled and applied automatically via Liquibase

## Tech Stack


| Layer            | Technology                                                                               |
|:-----------------|:-----------------------------------------------------------------------------------------|
| Language         | Java 17                                                                                  |
| Framework        | Spring Boot 3.5 (Web, Security, Data JPA, Validation, Cache)                             |
| Templating       | Thymeleaf                                                                                |
| Database         | PostgreSQL                                                                               |
| Migrations       | Liquibase                                                                                |
| Caching          | Redis (Spring Cache)                                                                     |
| Mapping          | MapStruct                                                                                |
| API docs         | springdoc-openapi (Swagger UI)                                                           |
| Build tool       | Maven                                                                                    |
| Testing          | JUnit, Spring Boot Test, Spring Security Test, H2 (in-memory test DB), JaCoCo (coverage) |
| Containerization | Docker, Docker Compose                                                                   |
| External API     | [OpenWeatherMap](https://openweathermap.org/api) (current weather, forecast, geocoding)  |

## Prerequisites

- Java 17+
- Maven
- Docker and Docker Compose (recommended — handles Postgres and Redis for you)
- An [OpenWeatherMap API key](https://openweathermap.org/api) (free tier is sufficient)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/podlLev/WeatherViewer.git
cd WeatherViewer
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
POSTGRES_DB=weather
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/weather
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

WEATHER_API_KEY=your_openweathermap_api_key

SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_CACHE_TYPE=redis
```

> **Never commit your real `.env` file or API key.** Add `.env` to `.gitignore` and rotate any key that has previously been pushed to a public repo.

### 3. Start dependencies (Postgres + Redis)

For local development:

```bash
docker compose -f docker-compose.dev.yml up -d
```

This starts Postgres on port `5433` and Redis on port `6379`, matching the `.env` values above.

### 4. Run the application

Using the Maven wrapper:

```bash
mvn spring-boot:run
```

The app will be available at **http://localhost:8080**.

### Running everything in Docker

To run the full stack (app + Postgres + Redis) in containers:

```bash
docker compose up -d
```

This pulls the app image, applies Liquibase migrations on startup, and wires up all three services with health checks.

## API Documentation

Once running, interactive API docs are available at:

```
http://localhost:8080/swagger-ui.html
```

REST endpoints are namespaced under `/api/v1/` (e.g. `/api/v1/weather/city`, `/api/v1/locations/my`, `/api/v1/users`).

## Running Tests

```bash
mvn test
```

Tests run against an in-memory H2 database, so no external services are required. The suite includes unit tests, MVC/REST controller tests, repository tests, and full integration tests for auth, search, profile, and weather flows. JaCoCo generates a coverage report at `target/site/jacoco/index.html` after running tests.

## Project Structure

```
src/main/java/com/weatherviewer/
├── config/          # Security and app-level configuration
├── controller/       # Thymeleaf (MVC) controllers — sign-in/up, home, search, profile, forecast
├── rest/             # REST API controllers (/api/v1/...)
├── dto/              # Data transfer objects
├── model/            # JPA entities (User, Location) and enums
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic interfaces + implementations
│   └── integration/  # OpenWeatherMap API client and caching layer
├── security/         # Spring Security principal, success/failure handlers
├── validation/       # Custom annotations and validators (lat/lon, password, uniqueness)
├── mapper/           # MapStruct entity↔DTO mappers
└── exception/        # Custom exceptions and global exception handlers

src/main/resources/
├── templates/        # Thymeleaf views
├── static/           # CSS, JS, images
└── liquibase/        # Database changelogs
```
## License
 
MIT — see [LICENSE](LICENSE) for details.
