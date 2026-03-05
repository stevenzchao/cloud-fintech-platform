#!/usr/bin/env bash
set -euo pipefail

# Adjust if your gateway publishes a different port
BASE_URL="http://localhost"

echo "==> Waiting for gateway / health..."
for i in {1..30}; do
  if curl -fsS "${BASE_URL}/health" >/dev/null 2>&1; then
    echo "OK: gateway health"
    break
  fi
  sleep 2
  if [ "$i" -eq 30 ]; then
    echo "ERROR: gateway did not become healthy"
    exit 1
  fi
done

echo "==> Waiting for tx-service route to be reachable (no 502/503)..."
# Use an endpoint that exists in tx-service, or use a known secured path even if 404 is okay.
# If your tx endpoint is /api/v1/transactions/123, keep it.
for i in {1..30}; do
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/transactions/does-not-matter")
  if [ "$HTTP_CODE" != "502" ] && [ "$HTTP_CODE" != "503" ]; then
    echo "OK: tx route reachable (status=${HTTP_CODE})"
    break
  fi
  sleep 2
  if [ "$i" -eq 30 ]; then
    echo "ERROR: tx route still returning ${HTTP_CODE} after waiting"
    exit 1
  fi
done

echo "==> Expect 401 when calling secured endpoint without token"
$HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/transactions/does-not-matter")
if [ "$HTTP_CODE" != "401" ]; then
  echo "ERROR: expected 401, got ${HTTP_CODE}"
  exit 1
fi
echo "OK: missing token rejected (401)"

echo "==> Login to get token"
# Update login path + JSON fields to match your auth-service
TOKEN=$(curl -fsS -X POST "${BASE_URL}/auth/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"pass1"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [ -z "${TOKEN}" ]; then
  echo "ERROR: could not extract accessToken from login response"
  exit 1
fi
echo "OK: token acquired"

echo "==> Call secured endpoint with token; must be not-401"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${TOKEN}" \
  "${BASE_URL}/api/v1/transactions/does-not-matter")

if [ "$HTTP_CODE" = "401" ]; then
  echo "ERROR: got 401 with valid token"
  exit 1
fi
echo "OK: valid token accepted (status=${HTTP_CODE})"