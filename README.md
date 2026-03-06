[![CI](https://github.com/stevenzchao/cloud-fintech-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/stevenzchao/cloud-fintech-platform/actions/workflows/ci.yml)
# cloud-fintech-platform

A minimal local platform scaffold to demo cloud + distributed fundamentals (gateway routing, multi-service, DB, health checks).

## Architecture (local)

## Services
- **nginx**: routes `/auth/` -> auth-service, `/api/` -> tx-service
- **auth-service** (Spring Boot): placeholder endpoints + health
- **tx-service** (Spring Boot): placeholder endpoints + health
- **postgres**: shared DB for now (can split later)

## Quick Demo:How to run locally (Docker)
- Prereqs: Docker + Docker Compose.
    ```bash
    docker compose up --build
    ```
- health via gateway(should return: nginx-ok)
  ```bash
  curl --location --request GET 'http://localhost/health'
    ```
- login to get token
  ```bash
    curl --location --request POST 'http://localhost/auth/auth/login' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "username": "user1",
    "password": "pass1"
    }'
    ```
    ```bash
  expected:  
  {
    "tokenType": "Bearer",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjbG91ZC1maW50ZWNoLWF1dGgiLCJhdWQiOiJjbG91ZC1maW50ZWNoLXR4Iiwic3ViIjoidXNlcjEiLCJpYXQiOjE3NzI3NjYyNDMsImV4cCI6MTc3Mjc2NzE0Mywicm9sZXMiOlsiUk9MRV9VU0VSIl19.zDEMZ0V9ktwDpy2q6yA-Fw1mZ60Aipnv3TJnmm-eLsA"
    }
    ```

- secured endpoint 401 without token, not-401 with token
    ```bash
    curl --location --request POST 'http://localhost/api/v1/transactions' \
    --header 'Idempotency-Key: 6' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "amount": "120.50",
    "currency": "TWD",
    "merchant": "FamilyMart1",
    "description": "late snack"
    }'
    ```
    ```bash
    expected:    
    {
    "timestamp": "2026-03-06T03:06:35.007300607Z",
    "status": 401,
    "error": "UNAUTHORIZED",
    "message": "Missing/invalid token",
    "path": "/v1/transactions",
    "details": []
    }
   ```

    ```bash
    curl --location --request POST 'http://localhost/api/v1/transactions' \
    --header 'Idempotency-Key: 6' \
    --header 'Authorization: Bearer {access_token_from_login}}' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "amount": "120.50",
    "currency": "TWD",
    "merchant": "FamilyMart1",
    "description": "late snack"
    }'
    ```
    ```bash
    expected:    
    {
    "id": "e47f53f7-398a-426c-bac3-fe9b077eb637",
    "amount": 120.50,
    "currency": "TWD",
    "merchant": "FamilyMart1",
    "description": "late snack",
    "status": "CREATED",
    "createdAt": "2026-03-01T12:53:48.326730Z"
    }   
    ```

## CI Overview
- auth-service (build + test)
- tx-service (build + test)
- docker (build images)
- smoke(docker compose +curl)



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