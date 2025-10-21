# party-map-backend/Makefile
SHELL := /bin/bash

INFRA := ../party-map-infra

# Compose commands
INFRA_COMPOSE   := docker compose -f $(INFRA)/docker-compose.yml --env-file $(INFRA)/.env
BACKEND_COMPOSE := docker compose -f docker-compose.yml $(if $(wildcard .env),--env-file .env)

NET := party-map-net

.PHONY: up down start stop restart build build-infra build-backend logs ps \
        up-infra up-backend down-infra down-backend create-network dump-keycloak restore-keycloak \
        ports

## ensure shared network exists
create-network:
	@docker network inspect $(NET) >/dev/null 2>&1 || docker network create $(NET)

## High-level shortcuts
start: up                    # alias
stop:  down                  # alias

## Bring EVERYTHING up (no rebuild)
up: create-network up-infra up-backend

## Bring EVERYTHING down (preserve volumes)
down: down-backend down-infra

## Recreate (no rebuild)
restart: down up

## Build images only (no start)
build: build-infra build-backend

build-infra:
	$(INFRA_COMPOSE) build

build-backend:
	$(BACKEND_COMPOSE) build

## Infra only
up-infra:
	$(INFRA_COMPOSE) up -d

down-infra:
	$(INFRA_COMPOSE) down

## Backend only
up-backend:
	$(BACKEND_COMPOSE) up -d

down-backend:
	$(BACKEND_COMPOSE) down

## Logs (infra + backend)
logs:
	@echo "Tailing logs (Ctrl+C to stop)…"
	$(INFRA_COMPOSE) logs -f keycloak db_keycloak & \
	$(BACKEND_COMPOSE) logs -f backend db

## Status for both stacks
ps:
	$(INFRA_COMPOSE) ps
	$(BACKEND_COMPOSE) ps

dump-keycloak:
	@$(MAKE) -C $(INFRA) dump-keycloak

restore-keycloak:
	@$(MAKE) -C $(INFRA) restore-keycloak FILE=$(FILE)

ports:
	@{ command -v lsof >/dev/null && lsof -nP -iTCP:5432 -sTCP:LISTEN || true; } || true
	@{ command -v lsof >/dev/null && lsof -nP -iTCP:5433 -sTCP:LISTEN || true; } || true
	@{ command -v lsof >/dev/null && lsof -nP -iTCP:8080 -sTCP:LISTEN || true; } || true
	@{ command -v lsof >/dev/null && lsof -nP -iTCP:8081 -sTCP:LISTEN || true; } || true
