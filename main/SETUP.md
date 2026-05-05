# AI Resume Builder — Setup & Run Guide

## Architecture Overview

```
Frontend (Angular 17) → Port 4200
    ↓ proxy (proxy.conf.json)
API Gateway → Port 8080
    ├── Auth Service      → Port 8081  (MySQL: airesume_auth,      Redis)
    ├── Template Service  → Port 8082  (MySQL: airesume_templates,  Redis)
    ├── Resume Service    → Port 8083  (MySQL: airesume_resume,     Redis)
    ├── Section Service   → Port 8084  (MySQL: airesume_section,    Redis)
    └── AI Service        → Port 8085  (MySQL: airesume_ai,  Groq API)

Infrastructure:
    Eureka Server  → Port 8761  (service discovery)
    Config Server  → Port 8888  (optional config)
    Admin Server   → Port 9090  (Spring Boot Admin)
```

---

## Option A: Docker Compose (Recommended — easiest)

### Prerequisites
- Docker + Docker Compose installed
- Node.js 18+ (for Angular frontend)

### Steps

```bash
# 1. Clone / extract the backend
cd AI-Resume-Builder-Backend

# 2. Copy and fill in your secrets
cp .env.example .env
# Edit .env — set your MySQL password, Google/LinkedIn OAuth credentials, Groq API key, etc.

# 3. Start all backend services (MySQL + Redis + all Spring Boot services)
docker-compose up -d

# 4. Wait ~3-4 minutes for everything to start.
#    Check health at: http://localhost:8761  (Eureka dashboard)

# 5. Start the Angular frontend (separate terminal)
cd ../AI-Resume-Builder-Frontend
npm install
ng serve --proxy-config proxy.conf.json

# 6. Open browser: http://localhost:4200
```

### Stop everything

```bash
docker-compose down
```

---

## Option B: Manual / Maven (Windows .bat or Linux .sh)

### Prerequisites
- Java 21 (JDK)
- Maven 3.9+
- MySQL 8.0 running locally
- Redis running locally
- Node.js 18+ + Angular CLI

### Step 1 — Configure environment

Edit `.env` in the backend root and fill in:
- `AUTH_DB_PASSWORD` — your MySQL root password
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` — from Google Cloud Console
- `LINKEDIN_CLIENT_ID` / `LINKEDIN_CLIENT_SECRET` — from LinkedIn Developer
- `GROQ_API_KEY` — from https://console.groq.com
- Email SMTP credentials (Gmail app password)

### Step 2 — Create MySQL databases

```sql
CREATE DATABASE airesume_auth;
CREATE DATABASE airesume_templates;
CREATE DATABASE airesume_resume;
CREATE DATABASE airesume_section;
CREATE DATABASE airesume_ai;
```

Or just let each service auto-create its DB — all JDBC URLs have `createDatabaseIfNotExist=true`.

### Step 3 — Start backend services

**Windows:**
```bat
start_all_services.bat
```

**Linux / Mac:**
```bash
chmod +x start_all_services.sh stop_all_services.sh
./start_all_services.sh
```

### Step 4 — Start frontend

```bash
cd AI-Resume-Builder-Frontend
npm install
ng serve --proxy-config proxy.conf.json
```

Open: **http://localhost:4200**

---

## Service Startup Order (Important!)

Always start in this order — each service registers with Eureka before the next one starts:

1. **Eureka Server** (8761) — service registry, must be first
2. **Config Server** (8888) — optional; services use local YMLs if unavailable
3. **Auth Service** (8081) — all other services validate JWT against this
4. **Template Service** (8082)
5. **Resume Service** (8083)
6. **Section Service** (8084)
7. **AI Service** (8085)
8. **API Gateway** (8080) — must start AFTER services are registered in Eureka
9. **Admin Server** (9090)

---

## Useful URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Spring Boot Admin | http://localhost:9090 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Auth Swagger | http://localhost:8081/swagger-ui.html |

---

## OAuth2 Setup (Google & LinkedIn)

### Google OAuth
1. Go to https://console.cloud.google.com → APIs & Services → Credentials
2. Create OAuth 2.0 Client ID (Web Application)
3. Add **Authorized redirect URI**: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret to `.env`

### LinkedIn OAuth
1. Go to https://developer.linkedin.com → My Apps → Create App
2. Add redirect URL: `http://localhost:8080/login/oauth2/code/linkedin`
3. Request scopes: `openid`, `profile`, `email`
4. Copy Client ID and Secret to `.env`

---

## Groq AI Setup

1. Sign up at https://console.groq.com
2. Create an API key
3. Set `GROQ_API_KEY` in `.env`
4. Default model: `llama-3.1-8b-instant` (free tier available)

---

## Common Issues & Fixes

### "Role not found" on first register
Auth Service auto-inserts roles via `data.sql`. If it doesn't, run:
```sql
USE airesume_auth;
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
```

### Services not registering in Eureka
Ensure Eureka is fully started (wait 15-20 seconds) before starting other services.

### Redis connection refused
Start Redis: `redis-server` or `docker run -d -p 6379:6379 redis:7-alpine`

### AI Service quota errors
Each free user gets 10 AI requests. Reset for testing:
```sql
USE airesume_ai;
UPDATE user_quota SET used_summary = 0, used_ats = 0;
```

---

## Project Structure

```
AI-Resume-Builder-Backend/
├── .env                    ← your secrets (do NOT commit)
├── .env.example            ← template for .env
├── docker-compose.yml      ← start everything with Docker
├── init-db.sql             ← creates all 5 databases
├── start_all_services.bat  ← Windows startup
├── start_all_services.sh   ← Linux/Mac startup
├── stop_all_services.sh    ← Linux/Mac stop
├── eureka-server/          ← port 8761
├── config-server/          ← port 8888
├── auth-service/           ← port 8081
├── template-service/       ← port 8082
├── resume-service/         ← port 8083
├── section-service/        ← port 8084
├── ai-service/             ← port 8085
├── api-gateway/            ← port 8080
└── admin-server/           ← port 9090

AI-Resume-Builder-Frontend/
├── proxy.conf.json         ← proxies /api → localhost:8080
├── src/app/
│   ├── core/               ← auth service, guards, interceptors
│   ├── shared/models/      ← all TypeScript interfaces
│   └── features/           ← auth, builder, ai, resume, templates
└── package.json
```
