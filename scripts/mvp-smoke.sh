#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
TENANT_ID="${TENANT_ID:-default}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin123}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required" >&2
  exit 1
fi

echo "Checking ping endpoint..."
curl -fsS "${BASE_URL}/api/v1/ping" >/dev/null

echo "Requesting auth token for tenant '${TENANT_ID}'..."
TOKEN="$(
  curl -fsS -X POST "${BASE_URL}/api/v1/auth/token" \
    -H "Content-Type: application/json" \
    -H "X-Tenant-Id: ${TENANT_ID}" \
    -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}" \
    | jq -r '.accessToken'
)"

if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
  echo "Failed to obtain access token" >&2
  exit 1
fi

echo "Calling authenticated employee list..."
curl -fsS "${BASE_URL}/api/v1/employees" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Tenant-Id: ${TENANT_ID}" >/dev/null

echo "Calling capabilities endpoint..."
curl -fsS "${BASE_URL}/api/v1/capabilities/modules/employee" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Tenant-Id: ${TENANT_ID}" >/dev/null

echo "MVP smoke flow completed successfully."
