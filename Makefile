HOST     ?= http://localhost:8080
HURL_DIR ?= ../realworld/specs/api/hurl
COMPOSE  := docker compose -f compose.test.yaml

.PHONY: e2e test-db-up test-db-down

## e2e: build + run the app against a disposable test DB, then run the Hurl suite
e2e:
	HOST=$(HOST) HURL_DIR=$(HURL_DIR) ./scripts/e2e.sh

test-db-up:
	$(COMPOSE) up --wait

test-db-down:
	$(COMPOSE) down -v
