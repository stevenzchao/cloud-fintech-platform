[![CI](https://github.com/stevenzchao/cloud-fintech-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/stevenzchao/cloud-fintech-platform/actions/workflows/ci.yml)
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
```


## Current Status
Day 1: Local distributed platform scaffold complete.

Day 2: – First Real Fintech Endpoint
* Implemented production-grade transaction creation with:
* POST /api/v1/transactions
* Idempotency-Key support
* SHA-256 payload hash validation
* Conflict detection (409)
* Flyway-managed schema
* PostgreSQL integration
* Transactional integrity
* Key design decisions:
* Idempotency handled via separate table
* Payload consistency enforced via request hashing
* Database migrations version-controlled
* Profiles isolated for local/docker/test

Day 3: – Improve Fintech Endpoint Service Layer Stability
* improve Test Coverage
* JPA open in view disable
* add Slf4j log 

Day 4: – JWT Skeleton
* Auth-Server JWT generation skeleton
* tx-service JWT verification skeleton


Day 5: – JWT Integration Test
* JWT integration test
* Nginx auth header added
* Flyway migration sql file fixed

Day 6: – GitHub CI V1
* Basic access verification add to JWT 
* Integration test to test basic access
* GitHub CI Workflow v1
* CI Add smoke test & smoke test script