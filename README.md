# Party Map backend

Spring Boot 3.5 / Kotlin REST API behind the Party Map frontend: places, performers, events,
event plans with invitations, likes and search. Authentication is a Keycloak realm; the API
validates the JWT on every protected endpoint.

## Run the dev stack with Docker

```bash
docker compose up
```

That starts, in this order: the app database, Keycloak with its own database and the `party-map`
realm imported from `keycloak/party-map-realm.json`, and the backend in the `dev` profile (the
schema is recreated and seeded from `src/main/resources/data.sql` on every start).

| Service | URL | Credentials |
|---|---|---|
| Backend | http://localhost:8080/api/places | JWT from Keycloak for protected endpoints |
| Keycloak admin console | http://localhost:8081 | admin / admin |
| Keycloak dev users (realm `party-map`) | | e2e@partymap.local / e2e-password (all manager roles); adrian@szell.dev (reset the password in the console) |
| PostgreSQL | localhost:5432 | partymap / partymap |

The first `docker compose up` builds the backend image (Gradle runs inside Docker, a few minutes);
later starts reuse the cached layers. Rebuild after code changes with `docker compose up --build backend`.

Hot reload while coding: start only the dependencies and run the app on the host.

```bash
docker compose up db keycloak
SPRING_PROFILES_ACTIVE=dev SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8081/realms/party-map ./gradlew bootRun
```

The frontend runs from its own folder (`docker compose up` in `../party-map-frontend`, or `pnpm dev`).

## Tests

```bash
./gradlew test          # needs a reachable PostgreSQL, e.g. `docker compose up db`
./gradlew clean build   # compile + tests + jar
```

HTTP smoke requests for IntelliJ's HTTP client are in `rest/`.

## Keycloak realm

`keycloak/party-map-realm.json` is a full realm export (clients, roles, dev users with password
hashes). It is imported only when the Keycloak database volume is empty; changes made in the admin
console persist in the `keycloak-db-data` volume. To refresh the file from a running stack:

```bash
docker compose exec keycloak /opt/keycloak/bin/kc.sh export --dir /tmp/export --realm party-map --users realm_file
docker compose cp keycloak:/tmp/export/party-map-realm.json keycloak/party-map-realm.json
```

Clients: `partymap` (confidential, used by the old Next.js frontend) and `partymap-web` (public,
PKCE, used by the React single-page app). Production Keycloak is a separate instance and needs the
same clients with the production URLs.

## Production

`application.yml` reads the datasource from `SPRING_DATASOURCE_*` and validates tokens against
`https://auth.terkep.party/realms/party-map`. CI (`.github/workflows/deploy.yml`) builds the image
and pushes `ghcr.io/party-map/party-map-backend:latest` on every push to `main`.
