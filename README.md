# Party Map backend

Spring Boot 4.1 / Kotlin 2.4 REST API (Java 25) behind the Party Map frontend: places, performers, events, event
plans with invitations, likes and search. Authentication is a Keycloak realm; the API validates the bearer token and
reads the realm roles from its `roles` claim.

## Run the dev stack with Docker

```bash
docker compose up
```

That starts the app database (PostgreSQL 18), Keycloak 26 with its own database and the `party-map` realm imported
from `keycloak/party-map-realm.json`, and the backend in the `dev` profile. The dev profile drops and re-creates the
schema with Flyway on every start and then loads the demo data from `src/main/resources/db/seed/afterMigrate.sql`.

| Service | URL | Credentials |
|---|---|---|
| Backend | http://localhost:8080/api/places | bearer token from Keycloak for protected endpoints |
| OpenAPI document | http://localhost:8080/api/openapi | |
| Keycloak admin console | http://localhost:8081 | admin / adminpass |
| Keycloak dev users (realm `party-map`) | | e2e@partymap.local / e2e-password (all manager roles and `partymap_admin`); roles-target@partymap.local (no roles, no password: the target of the admin e2e test) |
| PostgreSQL | localhost:5432 | partymap / partymap |

Rebuild after code changes with `docker compose up --build backend`. For hot reload, start only the dependencies and
run the app on the host (JDK 25 on `JAVA_HOME`):

```bash
docker compose up db keycloak
SPRING_PROFILES_ACTIVE=dev SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8081/realms/party-map ./gradlew bootRun
```

## Quality gates

Every change runs `./gradlew check` (Docker must be running) before it is committed; CI runs the same command and only
then builds the image.

| Gate | Tool | Rule |
|---|---|---|
| Static analysis and formatting | detekt 2 with the ktlint rules (`config/detekt/detekt.yml`) | no findings; `./gradlew detekt --auto-correct` fixes formatting |
| Tests | JUnit 6, MockMvc, spring-security-test, Testcontainers (PostgreSQL 18) | all green |
| Coverage | JaCoCo (`build/reports/jacoco/test/html`) | lines >= 90 %, branches >= 80 %, never lowered |

Integration tests extend `support/IntegrationTest`: a real database migrated by Flyway, MockMvc, the `TestData`
factories and `tokenFor(sub, roles)` for bearer tokens. Tests are not transactional (requests commit as in production);
the tables are emptied after each test. A bug fix starts with a test that fails on the old code.

## Database and migrations

Flyway owns the schema (`src/main/resources/db/migration`); Hibernate only validates the mapping (`ddl-auto: validate`).

- `V1__baseline.sql` is the schema Hibernate generated before Flyway, as dumped from production. Production was
  baselined at version 1 (`spring.flyway.baseline-on-migrate`), so V1 only runs on empty databases.
- `V2__entity_fixes.sql` and later files change the schema. Never edit an applied migration; add `V<n>__what.sql`.
- The dev seed is a Flyway `afterMigrate` callback that only the `dev` profile loads.

## API conventions

- Errors are RFC 9457 problem details (`application/problem+json` with `status`, `detail`, and `errors[]` of
  `{ field, message }` for invalid bodies): 400 invalid input, 401 no or bad token, 403 wrong role or not the owner,
  404 unknown id, 409 state conflict (already invited, not publishable yet).
- Mutations without a result answer 204. Creating or updating returns the saved object.
- `GET /api/places?bbox=minLon,minLat,maxLon,maxLat` returns only the places inside the map viewport.
- A place must lie inside Hungary (`domain/common/geo/Hungary.kt`, the country's outline in `resources/geo/hungary.json`,
  the same file the frontend's basemap uses): creating or moving one beyond the border is 400. Events happen at places,
  so they are inside too.
- `/api/admin/**` is for the `partymap_admin` realm role only (reads included): `GET /api/admin/users?q=&page=&size=`
  (size up to 50), `GET /api/admin/users/{id}`, `PUT` and `DELETE /api/admin/users/{id}/roles/{role}` for the three
  manager roles (204, idempotent; any other role is 400). Users and roles live in Keycloak; a Keycloak failure is 502.

## OpenAPI

The running app serves its OpenAPI 3.1 document at `/api/openapi` (springdoc). `OpenApiExportTest` also writes it to
`build/openapi.json`; the frontend generates its API types from a copy of that file:

```bash
./gradlew test --tests '*OpenApiExportTest*'   # then copy build/openapi.json to ../party-map-frontend/openapi.json
```

HTTP smoke requests for IntelliJ's HTTP client are in `rest/` (they expect the dev seed).

## Keycloak realm

`keycloak/party-map-realm.json` is a full realm export (clients, roles, dev users with password hashes). It is imported
only when the Keycloak database volume is empty. To refresh the file from a running stack:

```bash
docker compose exec keycloak /opt/keycloak/bin/kc.sh export --dir /tmp/export --realm party-map --users realm_file
docker compose cp keycloak:/tmp/export/party-map-realm.json keycloak/party-map-realm.json
```

### Admin service account

The admin endpoints call Keycloak's Admin REST API as the service account of the confidential client
`partymap-backend` (`app.keycloak.admin.*` in `application.yml`; env `APP_KEYCLOAK_ADMIN_URL`,
`APP_KEYCLOAK_ADMIN_CLIENT_SECRET`). Its service account holds the `realm-management` client roles `view-users`,
`query-users` and `manage-users`, nothing more (role ids are read from the user's role mappings, so `view-realm` is
not needed). Without a secret the application starts and only the admin endpoints answer 502. The dev realm export
contains the client with the secret `partymap-backend-dev-secret`, which the dev profile and `docker-compose.yml` use.

The realm role `partymap_admin` (a composite of `user`) is granted in Keycloak only; the API never grants it.

## Server-rendered shells (SEO)

`/events/{id}`, `/places/{id}` and `/performers/{id}` are also HTML routes (`web/shell`): the backend fetches the
frontend's `index.html` (`app.shell.template-url`, cached for `app.shell.template-ttl`, the last good copy kept on a
failed refresh, `shell/fallback-index.html` before the first success) and replaces its three comment regions
(`<!--pm:head-->`, `<!--pm:body-->`, `<!--pm:data-->`) with the page's `<title>`, description, canonical, Open Graph
and Twitter tags, a schema.org JSON-LD graph (`Event` with `location`, `performer` and `offers`; `Place`;
`MusicGroup`), a crawler-readable body and a `<script id="pm-data" type="application/json">` carrying exactly what
the app's page hook would fetch, so React renders without a first request. Unknown ids answer the app's 404 shell
with `noindex`; `/sitemap.xml` lists the places, performers and the events that have not ended. The frontend's
nginx (and its dev server) proxies only those routes here; `springdoc.paths-to-match` keeps them out of the OpenAPI
document. Production needs `APP_SHELL_TEMPLATE_URL=http://frontend:8080/index.html` and
`APP_SHELL_PUBLIC_BASE_URL=https://terkep.party` in the backend's environment.

## Production

The `prod` profile allows CORS from `https://terkep.party`. The datasource comes from `SPRING_DATASOURCE_*` (or the
`APP_DB_*` variables in `application.yml`), tokens are validated against `https://auth.terkep.party/realms/party-map`.
CI (`.github/workflows/deploy.yml`) runs `./gradlew check` on every push and pull request and, on `main`, pushes
`ghcr.io/party-map/party-map-backend` tagged `latest` and with the commit SHA. The image runs as a non-root user and
reports its health through `/api/openapi`.

### Enabling the admin endpoints in production (once, on https://auth.terkep.party, realm `party-map`)

1. Realm roles: create `partymap_admin`, description "Party Map platform administrator", and add `user` as its
   associated (composite) role.
2. Clients: create `partymap-backend` (OpenID Connect, client authentication on, service accounts roles on; standard
   flow, direct access grants and implicit flow off). Under "Service accounts roles" assign the `realm-management`
   roles `view-users`, `query-users` and `manage-users`. Copy the secret from "Credentials".
3. On the server: add `APP_KEYCLOAK_ADMIN_CLIENT_SECRET=<secret>` to the backend's environment in
   `/root/partymap/app` (`APP_KEYCLOAK_ADMIN_URL` defaults to `https://auth.terkep.party`) and recreate the backend.
4. Users: assign `partymap_admin` to the platform admin; they sign out and in again so the token carries it.
5. Smoke test: `GET https://api.terkep.party/api/admin/users?size=1` with that user's token answers 200.
