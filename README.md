# cloud-fintech-platform

A minimal local platform scaffold to demo cloud + distributed fundamentals (gateway routing, multi-service, DB, health checks).

## Architecture (local)

## Services
- **nginx**: routes `/auth/` -> auth-service, `/api/` -> tx-service
- **auth-service** (Spring Boot): placeholder endpoints + health
- **tx-service** (Spring Boot): placeholder endpoints + health
- **postgres**: shared DB for now (can split later)

## How to run locally (Docker)
Prereqs: Docker + Docker Compose.

```bash
docker compose up --build