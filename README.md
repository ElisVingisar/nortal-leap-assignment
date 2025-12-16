<p align="center">
  <img src="logo.png" alt="Nortal LEAP Logo" width="180">
</p>
<h1 align="center">Nortal LEAP 2026 Coding Assignment</h1>

## Prerequisites (minimal)
- Node.js 20+ and npm
- JDK 21 (uses bundled Gradle wrapper; no separate Gradle install needed)

This repo holds two modules:
- `frontend/` - an Angular 20 app that shows the brief and lets you click through the library API.
- `backend/` - a Java 21 Gradle project (Spring Boot: core/persistence/api) containing the library logic you will extend.

## Quick start (fastest path)
- Doctor (auto-hints per OS): `node tools/doctor.mjs`
- Backend helpers (JDK 21):
  - Start API: `node tools/run-backend.mjs start` (port 8080, H2 in-memory with seeds)
  - Test: `node tools/run-backend.mjs test`
  - Format: `node tools/run-backend.mjs format`
  - Build (skip tests): `node tools/run-backend.mjs build-skip`
  - Perf smoke (short, local): `node tools/run-perf.mjs` (API must be running; set `PERF_BASE_URL`/`PERF_ITERATIONS` to override)
- Frontend helpers (Angular 20):
  - Install: `node tools/run-frontend.mjs install`
  - Run: `node tools/run-frontend.mjs start` (http://localhost:4200)

## Using the app end-to-end
1) Start backend: `cd backend && ./gradlew :api:bootRun` (seeds members m1–m4, books b1–b6; CORS open).
2) Start frontend: `cd frontend && npm start` -> http://localhost:4200
3) Exercise API (all JSON):
   - `GET /api/books` -> `{ items: [{ id, title, loanedTo, reservationQueue }] }`
   - `GET /api/members` -> `{ items: [{ id, name }] }`
   - `POST /api/books|members` `{ id, title|name }` -> `{ ok, reason? }`
   - `PUT /api/books|members` same body -> `{ ok, reason? }`
   - `DELETE /api/books|members` `{ id }` -> `{ ok, reason? }`
   - `POST /api/borrow` `{ bookId, memberId }` -> `{ ok, reason? }`
   - `POST /api/reserve` `{ bookId, memberId }` -> `{ ok, reason? }`
   - `POST /api/return` `{ bookId }` -> `{ ok, nextMemberId? }`
   - `GET /api/health` -> `{ status: "ok" }`

## Assignment (backend)
The backend is a Spring Boot application (modules: core/persistence/api). Domain rules are intentionally naive; your job is to fix them while keeping the API surface unchanged.

Key behaviors to implement/adjust:
1) **Prevent double loans and respect queues**  
   - A book already on loan must not be loaned again.  
   - If a reservation queue exists, only the head of the queue should receive the book (no line-jumping via direct borrow).
   - Returns should only succeed when initiated by the current borrower.

2) **Reservation lifecycle**
   - Members may reserve a loaned book; duplicate reservations by the same member should be rejected.  
   - Reserving an available book should immediately loan it to the reserver (if eligible).  
   - Returning a book must hand it to the next eligible reserver in order; skip ineligible/missing members and continue. Surface who received it.  
   - Keep the queue consistent when handoffs happen.

3) **Borrow-limit enforcement & clarity**  
   - Enforce the existing max-loan cap cleanly and efficiently (limit is already defined in code).  
   - Refactor the current approach so the rule is obvious and not needlessly expensive.

Visible tests are minimal; rely on behavior requirements above. Hidden scoring tests will validate correctness.

## Deliverables
- Fix backend behaviors and keep API runnable (`./gradlew :api:bootRun` and `npm start`).
- Run `./gradlew test` and `./gradlew spotlessApply` before sharing and ensure they pass.
- Document AI usage in `AI_USAGE.md` (brief, honest).
- Do not commit build (`node_modules`/`build`/`dist` etc.) artifacts; `.gitignore` already covers the usual suspects.

## How to submit
- Push your solution to your own git repo (or share a private fork) and send us the git URL.
- Keep the project structure unchanged; avoid force-pushes after sharing unless requested.
- Sharing your commit history is encouraged.

## Notes
- The frontend is a guide only; the substance is in the Java backend.
- If you make assumptions, note them in code comments or `AI_USAGE.md`.