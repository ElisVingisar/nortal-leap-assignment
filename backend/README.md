# Backend (Spring Boot) overview

Modules:
- `core` - domain entities (JPA) + `LibraryService` with intentionally naive rules.
- `persistence` - Spring Data JPA adapters for H2; simple schema via `schema.sql`.
- `api` - Spring Boot app, controllers, security, seed data.
- The assignment uses the Spring Boot stack (core/persistence/api).

## Run
- Install JDK 21.
- From `backend/`: `./gradlew :api:bootRun` (or `node tools/run-backend.mjs start`)
- H2 console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:library`).
- Dev seeds: members `m1..m4`, books `b1..b6`.

## Auth (JWT, RS256)
- Resource server wiring is present. Local default is relaxed: `library.security.enforce=false` (all routes open).
- To enforce auth: `LIBRARY_SECURITY_ENFORCE=true ./gradlew :api:bootRun`.
- A demo RSA keypair is embedded for DX; printing a demo token is **off by default**. To print one at startup (subject `m1`, 1h TTL), set `library.security.print-demo-token=true`. Use header `Authorization: Bearer <token>`.
- Generate new tokens: re-enable `library.security.print-demo-token=true` or sign with the embedded private key in `DevAuthConfig`.

## API surface
- `GET /api/books` -> `{ items: [{ id, title, loanedTo, reservationQueue }] }`
- `GET /api/members` -> `{ items: [{ id, name }] }`
- `POST /api/books|members` with `{ id, title|name }` -> `{ ok, reason? }`
- `PUT /api/books|members` same body -> `{ ok, reason? }`
- `DELETE /api/books|members` with `{ id }` -> `{ ok, reason? }`
- `POST /api/borrow` `{ bookId, memberId }` -> `{ ok, reason? }`
- `POST /api/reserve` `{ bookId, memberId }` -> `{ ok, reason? }`
- `POST /api/return` `{ bookId }` -> `{ ok, nextMemberId? }`
- `GET /api/health` -> `{ status: "ok" }`

## Useful properties
- `library.security.enforce` (default `false`) - toggle auth.
- `library.security.print-demo-token` (default `false`) - print a demo JWT at startup.
