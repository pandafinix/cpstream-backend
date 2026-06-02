# CPStream Backend

CPStream Backend is the Spring Boot API server for CPStream, a competitive-programming focused livestreaming platform where users can stream problem-solving sessions, manage stream metadata, chat with viewers, and discover live programming streams.

This backend handles stream management, LiveKit ingress creation, LiveKit token generation, webhook-based live status updates, PostgreSQL persistence, Redis-backed chat rate limiting, chat APIs, and follow/block features.

---

## Features

* Spring Boot REST API
* PostgreSQL persistence with Spring Data JPA
* Redis-backed chat spam/rate limiting
* LiveKit RTMP ingress generation
* LiveKit viewer token generation
* LiveKit webhook handling for live/offline status updates
* Stream metadata management
* Public live stream listing
* Stream search by title, platform, language, and difficulty
* Chat message APIs
* Chat settings support:

  * enable/disable chat
  * delayed chat
  * followers-only chat
* Follow system
* Block system
* User synchronization APIs
* Public stream responses protected from exposing sensitive stream keys

---

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Redis
* LiveKit Server SDK
* Maven

---

## Repository Structure

```txt
src/
  main/
    java/com/cpstream/backend/
      block/        Block feature
      chat/         Chat APIs and chat rate-limit flow
      config/       Backend configuration
      follow/       Follow system
      health/       Health check endpoints
      livekit/      LiveKit token, ingress, and webhook logic
      stream/       Stream metadata and live stream APIs
      user/         User sync and user APIs
      BackendApplication.java

    resources/
      application-example.properties
      application.properties   # local only, ignored by Git

  test/
    java/com/cpstream/backend/
      BackendApplicationTests.java

pom.xml
mvnw
mvnw.cmd
```

---

## System Architecture

```txt
OBS
 ↓ RTMP
LiveKit Ingress
 ↓
LiveKit Room
 ↓ webhook
Spring Boot Backend
 ↓
PostgreSQL / Redis
 ↓
Next.js Frontend
```

---

## Live Streaming Flow

1. Streamer requests a new streaming connection from the frontend dashboard.
2. Backend creates a LiveKit RTMP ingress.
3. Backend stores the generated ingress details for the streamer.
4. Streamer copies the generated `serverUrl` and `streamKey` into OBS.
5. OBS streams to LiveKit.
6. LiveKit sends webhook events to the backend.
7. Backend updates the stream live status.
8. Frontend fetches the updated stream state and displays the stream as live.

---

## Prerequisites

* Java 17+
* Maven
* PostgreSQL
* Redis
* LiveKit project
* CPStream frontend running separately

---

## Configuration

Create the local config file:

```txt
src/main/resources/application.properties
```

Use this file as reference:

```txt
src/main/resources/application-example.properties
```

Example configuration:

```properties
spring.application.name=backend

spring.datasource.url=jdbc:postgresql://localhost:5432/cpstream
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

livekit.ws-url=wss://YOUR_LIVEKIT_PROJECT.livekit.cloud
livekit.api-key=YOUR_LIVEKIT_API_KEY
livekit.api-secret=YOUR_LIVEKIT_API_SECRET

spring.data.redis.host=localhost
spring.data.redis.port=6379

chat.rate-limit.window-seconds=10
chat.rate-limit.max-messages=5

server.error.include-stacktrace=never
server.error.include-message=always
server.error.include-binding-errors=never
```

Do not commit real credentials. `application.properties` is ignored by Git.

---

## Local Setup

### 1. Start PostgreSQL

Create a PostgreSQL database:

```txt
cpstream
```

Update your database username and password in:

```txt
src/main/resources/application.properties
```

### 2. Start Redis

Using Docker:

```bash
docker run --name cpstream-redis -p 6379:6379 -d redis:7-alpine
```

If the Redis container already exists:

```bash
docker start cpstream-redis
```

### 3. Run the backend

Using Maven:

```bash
mvn spring-boot:run
```

Or using Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Default backend URL:

```txt
http://localhost:8080
```

---

## Main API Areas

### Streams

```txt
GET    /api/streams
GET    /api/streams/live
GET    /api/streams/search?term=...
GET    /api/streams/user/{username}
PATCH  /api/streams/{streamId}
```

### LiveKit

```txt
POST   /api/livekit/token
POST   /api/livekit/ingress
POST   /api/livekit/webhook
```

### Chat

```txt
Chat APIs are used for stream messages and chat state.
Redis is used to rate-limit repeated messages.
```

### Users, Follow, and Block

```txt
User sync APIs
Follow APIs
Block APIs
```

---

## Security Notes

Public stream APIs must not expose sensitive streaming credentials such as:

```txt
streamKey
serverUrl
ingressId
```

These values should only be available to the authenticated streamer through dashboard-specific flows.

The public stream response should only expose safe metadata such as:

* stream title
* thumbnail
* username
* live status
* platform
* difficulty
* language
* chat settings

---

## Frontend Integration

This backend is intended to work with the CPStream frontend repository.

The frontend should point to the backend using:

```env
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

For production, replace it with the deployed backend URL.

---

## LiveKit Webhook Setup

For local testing with ngrok:

```bash
ngrok http 8080
```

Use the generated HTTPS URL in the LiveKit dashboard webhook settings:

```txt
https://your-ngrok-url.ngrok-free.app/api/livekit/webhook
```

For production, configure LiveKit webhook to point to the deployed backend:

```txt
https://your-backend-domain.com/api/livekit/webhook
```

---

## Deployment Notes

For production deployment:

1. Deploy PostgreSQL.
2. Deploy Redis.
3. Deploy the Spring Boot backend.
4. Add production database, Redis, and LiveKit configuration.
5. Configure LiveKit webhook to point to the deployed backend.
6. Set the deployed backend URL in the frontend environment.

Example frontend environment value:

```env
NEXT_PUBLIC_BACKEND_URL=https://your-backend-domain.com
```

---

## Current Status

* LiveKit streaming tested successfully
* OBS RTMP ingress tested successfully
* LiveKit webhook tested successfully
* Live badge updates tested successfully
* Chat tested successfully
* Redis chat rate limiting tested successfully
* Public stream key exposure fixed
* Secret configuration protected from Git commits
