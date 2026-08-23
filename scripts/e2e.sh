#!/usr/bin/env bash
# Build + run the app against a disposable test DB, then run the Hurl suite.
# Kept as a script (not inline in the Makefile) because macOS ships GNU Make 3.81,
# which predates .ONESHELL/.SHELLFLAGS and runs each recipe line in its own shell.
set -euo pipefail

HOST="${HOST:-http://localhost:8080}"
HURL_DIR="${HURL_DIR:-../realworld/specs/api/hurl}"
COMPOSE=(docker compose -f compose.test.yaml)

APP_PID=""
cleanup() {
  [ -n "$APP_PID" ] && kill "$APP_PID" 2>/dev/null || true
  "${COMPOSE[@]}" down -v
}
trap cleanup EXIT

# build first so a broken build fails fast, before touching Docker
./gradlew bootJar

# start the throwaway test DB and wait until it is healthy
"${COMPOSE[@]}" up --wait

# run the app against it. the jar excludes the dev docker-compose module,
# so it honours these env vars instead of auto-starting compose.yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:8096/test-app \
SPRING_DATASOURCE_USERNAME=admin \
SPRING_DATASOURCE_PASSWORD=password \
java -jar build/libs/*-SNAPSHOT.jar > app.log 2>&1 &
APP_PID=$!

echo "waiting for app readiness..."
READY=0
for _ in $(seq 1 60); do
  if curl -sf "$HOST/actuator/health/readiness" 2>/dev/null | grep -q '"status":"UP"'; then
    READY=1
    break
  fi
  kill -0 "$APP_PID" 2>/dev/null || { echo "app exited early:"; cat app.log; exit 1; }
  sleep 2
done
[ "$READY" = 1 ] || { echo "app did not become ready:"; cat app.log; exit 1; }

# run the Hurl suite against the running app
HOST="$HOST" "$HURL_DIR/run-hurl-tests.sh"
