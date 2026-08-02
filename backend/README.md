# Backend

This directory contains the Spring Boot backend application.

## Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/   # Java source code (controllers, services, repositories)
│   │   └── resources/  # Application configuration
│   └── test/       # Unit and integration tests
└── pom.xml         # Maven build file
```

## Setup

Ensure you have Java 17+ and Maven installed.

```bash
cd backend
./mvnw install -DskipTests
```

## Run

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Test

```bash
./mvnw test
```
