# ♟️ Chess Arena

Chess Arena is a full-stack, real-time multiplayer chess platform built as a Maven multi-module project: a **Spring Boot WebSocket server**, a **JavaFX desktop client**, and a shared **chess engine core**. Players authenticate with Auth0, get matched into rated Rapid/Blitz games over STOMP, and play live with draw offers, resignations, timeouts, and spectating — all backed by PostgreSQL and Redis.

## Architecture

The project is split into three Maven modules under a single parent POM:

| Module | Description |
|---|---|
| **`Core`** | Shared chess rules/move-validation engine, built on top of [`chesslib`](https://github.com/bhlangonijr/chesslib) (via JitPack). Consumed by both `Server` and `UI`. |
| **`Server`** | Spring Boot 4.1 backend exposing REST endpoints for auth/users/social and a STOMP-over-WebSocket API for real-time gameplay, matchmaking, and spectating. |
| **`UI`** | JavaFX 17 desktop client with dependency injection via Google Guice, a lobby/friends/matchmaking UI, and a live game board. |

```
Chess-Arena/
├── Core/    → chess engine & move validation (chesslib)
├── Server/  → Spring Boot backend (REST + STOMP, JPA, Redis, OAuth2)
├── UI/      → JavaFX desktop client
└── Dockerfile
```

## Tech Stack

**Backend (`Server`)**
- Java 17, Spring Boot 4.1
- Spring WebSocket + STOMP messaging for real-time game state
- Spring Security with an OAuth2 **resource server** validating JWTs issued by **Auth0**
- Spring Data JPA + **PostgreSQL** for persistent storage (users, games, moves, friendships)
- **Redis** (TLS) for matchmaking queue state and live game session storage
- Lombok, Bean Validation (`spring-boot-starter-validation`)

**Client (`UI`)**
- JavaFX 17 (`javafx-base`, `javafx-controls`, `javafx-fxml`, `javafx-graphics`)
- Google Guice for dependency injection, Guava utilities
- `java-keyring` for secure local storage of auth tokens
- STOMP WebSocket client for live game/lobby events

**Shared (`Core`)**
- [`chesslib`](https://github.com/bhlangonijr/chesslib) `1.3.3` for board representation, legal-move generation, and game-state (FEN) handling

**Infra**
- Multi-stage `Dockerfile` (Maven/Temurin 21 build stage → Temurin 21 JRE runtime), server listens on port `8080`

## How it works

### Authentication flow
Auth is built around Auth0's Authorization Code + PKCE flow, with the refresh token treated as the durable credential and the access token as a short-lived, in-memory one:

1. **On launch**, the client tries to pull a refresh token out of the OS-backed secure vault (`TokenStorage`, via `java-keyring`) — nothing is ever written to disk in plaintext.
2. **If no refresh token is found**, the user is redirected to Auth0's hosted login page (`AuthService.redirect()`, PKCE code verifier/challenge + `state` generated locally) and a short-lived local callback server (`localhost:3000/callback`) catches the authorization code.
3. **If a refresh token is found**, the client immediately exchanges it for a fresh access token (`AuthClient.exchangeRefreshToken`) instead of forcing the user through the login page again. `AuthService.isUserAuthenticated()` is effectively this "try silent refresh, fall back to login" check.
4. **The resulting access token** is cached in memory (`TokenStorage.accessToken`) and attached as a `Bearer` header to every REST call and to the STOMP `CONNECT` frame — it's the single credential used across HTTP and WebSocket for the rest of the session.
5. **Every REST call is wrapped in a self-healing mechanism**: `ApiClient.execute()` sends the request, and if the server rejects it with `401` because the access token expired, it transparently calls `tryRefresh()`, swaps in the new access token, and **retries the original request once** before surfacing anything to the caller. If the refresh token itself is rejected (expired/revoked), the vault entry is cleared and the user is dropped back to the login screen.

The server side mirrors this: `SecurityConfig` runs as an OAuth2 **resource server**, validating every inbound JWT against the Auth0 issuer (`NimbusJwtDecoder`) on both REST and the `/ws` STOMP handshake. Only `/api/v1/users/register` and `/ws/**` are public; everything else requires a valid bearer token.

### Realtime events: STOMP → Guava EventBus → UI
The client keeps a hard separation between "the code that talks to the wire" and "the code that updates the screen":

- `GameRealtimeGatewayStub` owns the actual STOMP session — it connects, subscribes to destinations like `/user/queue/matchmaking`, `/user/queue/game.events`, and `/topic/spectate/{playerId}`, and deserializes incoming frames into typed event objects (`GameFound`, `OpponentMove`, `MoveConfirmation`, `GameOverInfo`, `DrawOfferReceived`, `SpectatorResponse`, ...).
- Instead of calling into the UI directly, it hands every deserialized event to `GameEventPublisher`, a thin wrapper around a **Guava `EventBus`**.
- `GameSessionService` registers itself on that bus and exposes `@Subscribe`-annotated handlers (`onMatchFound`, `onOpponentMove`, `onConfirmMove`, `onGameOver`, `onDrawOffered`, ...) which update the JavaFX `GameView` on the FX Application Thread.

This means the STOMP/networking layer and the UI layer are fully decoupled and communicate only through published events — the gateway doesn't know or care who's listening, and the UI never touches the socket directly.

### Matchmaking (work in progress)
Matchmaking is currently a **minimal, temporary implementation for end-to-end testing**, not the final system. Players signal intent to play over WebSocket (`/app/start.search`, `/app/stop.search`), handled by `MatchmakingController`, which currently delegates straight to a simple matching entry point. The rating-aware queue (`MatchmakingQueueService`) and background pairing job (`MatchmakerJob`) that will eventually replace this are scaffolded in the codebase but not yet implemented — real Elo-based queuing/pairing logic is actively being built.

### Clock synchronization
Each client ticks its own local countdown for responsiveness, but the local clocks are **not treated as the source of truth**. On every move, the server sends back the authoritative `myRemainingMs` / `oppRemainingMs` in the `MoveConfirmation` event, and the client immediately re-syncs both clocks to those values (`GameView.syncClocks`). This keeps both players' clocks as accurate as possible and prevents client-side drift (e.g. from frame timing or a slow tab) from affecting the official remaining time — the server clock always wins.

### Live gameplay
Once matched, all game actions travel over the shared STOMP endpoint (`/ws`, app prefix `/app`, broker on `/queue` and `/topic`):

| Destination | Action |
|---|---|
| `/app/game.move` | Submit a move; validated server-side against `chesslib` legal moves |
| `/app/game.resign` | Resign the current game |
| `/app/game.draw.offer` | Offer a draw to the opponent |
| `/app/game.draw.accept` | Accept a pending draw offer |
| `/app/spectate.request` / `/app/spectate.accept` | Request to watch a live game, subject to player approval (`SpectatorApprovalRegistry`) |
| `/app/in.lobby` | Heartbeat/presence ping while in the lobby |

Games run through `GameLogicService` and `GameMoveValidation`, with automatic loss-on-timeout handled by a dedicated `TimeOutSchedulingService`. Every game and move is persisted (`GameRepo`, `GameMoveRepo`) with a `RAPID`/`BLITZ` type and a `WHITE_WIN` / `BLACK_WIN` / `DRAW` result.

### Rating & social
Every user has an Elo-style rating (defaults to `1200`) that's part of their profile. On top of matchmaking, a full friend system is exposed over REST:

- `POST /api/v1/users/register` — create an account
- `GET /api/v1/users/me` / `GET /api/v1/users/search` — profile & user search
- `POST /api/v1/users/social/invite` — send a friend request
- `PUT /api/v1/users/social/accept` / `PUT /api/v1/users/social/reject` — respond to a request
- `DELETE /api/v1/users/social/unSend` / `DELETE /api/v1/users/social/delete` — cancel a request / remove a friend
- `GET /api/v1/users/social/friends` — list friends

## Getting started

### Prerequisites
- JDK 17+
- Maven 3.9+
- PostgreSQL instance
- Redis instance (TLS-enabled, as configured)
- An Auth0 tenant (or your own OAuth2/OIDC issuer) for JWT issuance

### Environment variables (Server)
The server reads its datasource, cache, and auth config from the environment:

```bash
ISSUER=<your OAuth2/Auth0 issuer URI>
DB_URL=<postgres JDBC URL>
REDIS_HOST=<redis host>
REDIS_PASSWORD=<redis password>
```

### Run with Docker
```bash
docker build -t chess-arena-server .
docker run -p 8080:8080 \
  -e ISSUER=... -e DB_URL=... -e REDIS_HOST=... -e REDIS_PASSWORD=... \
  chess-arena-server
```

### Run locally with Maven
```bash
# from the repo root — builds Core, Server, and UI
mvn clean install

# run just the server
cd Server && mvn spring-boot:run

# run the JavaFX desktop client (Windows JavaFX natives are pinned in UI/pom.xml)
cd UI && mvn clean package
java -jar target/Chess-UI-1.0-SNAPSHOT.jar
```

> **Note:** `UI/pom.xml` currently pins JavaFX dependencies to the `win` classifier. macOS/Linux users will need to swap in the matching JavaFX classifier for their platform before building the client.

## Roadmap / in development

The following are actively being built and are **not yet complete**:

- 🕹️ **Game history with move-by-move replay** — the lobby already has a "Recent Games" panel that lists past results and Elo deltas, but clicking into a game to replay it move-by-move is still in progress.
- 🤝 **Direct game invites between friends** — friend requests and the friends list are functional today; challenging a specific friend to a game directly from that list is planned but not wired up yet.
- 👀 **Spectator experience polish** — the request/approve handshake for watching live games works at the protocol level; a richer spectator UI is planned.
- 📊 **Post-game stats & rating history** — surfacing rating progression over time beyond the current per-game Elo delta.
- 🎯 **Rating-based matchmaking** — matchmaking currently pairs players with a simple, temporary matcher used only for testing the end-to-end flow; a proper Elo/queue-based matcher (`MatchmakingQueueService` + `MatchmakerJob`) is under active development.

## Project structure highlights

```
Server/src/main/java/org/Core/
├── GameLogic/       → game sessions, move validation, matchmaking, draw/resign handling
├── User/             → registration, profile, presence
├── Social/            → friendships and game spectating
└── Configurations/     → Security (OAuth2/JWT), WebSocket (STOMP), Redis

UI/src/main/java/org/Core/
├── UI/OpeningScreens/  → splash, login (Auth0), mode select
├── UI/LobbyScreens/    → lobby, friends panel, profile
├── UI/Game/             → live game board, draw/resign/spectate dialogs, recent games
├── Auth/                 → Auth0 token exchange & secure keyring storage
├── Realtime/               → WebSocket gateway to the server
├── Auth/                 → Auth0 token exchange & secure keyring storage
└──Config                → ApiClient,DI objects creation
├── Social              → FriendShipClient handling all social interactions


No license file is currently included in this repository. Add a `LICENSE` file to clarify usage terms before distributing or accepting external contributions.