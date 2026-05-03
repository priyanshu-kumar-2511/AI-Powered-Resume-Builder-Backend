# ResumeAI: AI-Powered Resume Builder Platform (Backend)
**Current Branch:** `feature/UC7-Export-Service`

## 📌 Project Overview / Introduction

**ResumeAI** is a full-stack AI-Powered Resume Builder platform designed to empower job seekers to create, optimize, and export professional resumes with the help of cutting-edge AI language models. 

The platform utilizes an AI-powered content layer to generate and optimize resume content for multiple user flows. The current backend setup is configured around a **Groq-backed OpenAI-compatible API** (using models like `llama-3.1-8b-instant`). This allows the `ai-service` to generate professional summaries, write bullet points, provide ATS feedback, and translate resumes at lightning-fast speeds through a single provider.

Features include:
*   Intelligent content generation for Professional Summaries and work experience bullet points.
*   **ATS Compatibility Scoring** against target job descriptions.
*   Automated resume customization to perfectly match live job postings.
*   Exporting dynamic visual CV templates directly to PDF and DOCX formats.
*   **Job Linking:** Calculating a resume-to-job fit score and fetching live job data via **LinkedIn & Naukri APIs** (RapidAPI).

This repository holds the backend microservices ecosystem powering these platform features.

---

## 🏗 Phase 1 (Infrastructure Layer)

Phase 1 focuses exclusively on the core IT structural foundation that routes, monitors, and configures the future business logic deployments.

## Architecture Overview

This project is built using **Java 17** and **Spring Boot 3.5.13**, following popular distributed systems patterns alongside Spring Cloud.

### Infrastructure Microservices (Phase 1)

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`eureka-server`** | `8761` | **Service Discovery:** Acts as the phonebook for the architecture. |
| **`config-server`** | `8888` | **Centralized Configuration:** Provides a centralized location to manage properties. |
| **`admin-server`** | `9090` | **Health & Monitoring:** visual dashboard to monitor health and metrics. |
| **`api-gateway`** | `8080` | **Routing Gateway:** [Swagger UI](http://localhost:8080/swagger-ui.html) - Single entry point for all frontend client requests. |

## How to Run

To start the infrastructure layer locally, open four separate terminal windows and navigate into each directory. You must start the services using Maven (`mvn spring-boot:run`).

For the smoothest startup sequence, start the services in the following order:

1. **Start Eureka Server**
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   *Dashboard:* [http://localhost:8761](http://localhost:8761)

2. **Start Config Server**
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

3. **Start Admin Server**
   ```bash
   cd admin-server
   mvn spring-boot:run
   ```
   *Dashboard:* [http://localhost:9090](http://localhost:9090)

4. **Start API Gateway**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```
   *Swagger UI:* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🚀 Phase 2 (Business Logic)

This phase introduces the core business and user identity layer of the ecosystem.

### Auth Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`auth-service`** | `8081` | **Identity Management & Admin Power:** Handles registration, robust JWT authentication, LinkedIn/Google OAuth2 integration, Profile management, and an **Admin Dashboard** for user oversight and platform security. |

### How to Run Phase 2

For the business logic to function, ensure your infrastructure services (especially Eureka) and your MySQL database are running. 

1. **Start Auth Service**
   ```bash
   cd auth-service
   mvn spring-boot:run
   ```
   *Auth APIs Swagger UI:* [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## 🛠 Required Updates for Frontend & Google Auth

The following configurations are necessary to link the backend with the frontend and enable Google/LinkedIn OAuth2 login.

### 1. Environment Configuration (`.env`)
Create a `.env` file in the root directory to store sensitive Google and Database credentials:
```properties
GOOGLE_CLIENT_ID=your_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_secret_key
LINKEDIN_CLIENT_ID=your_linkedin_client_id
LINKEDIN_CLIENT_SECRET=your_linkedin_client_secret
API_GATEWAY_URL=http://localhost:8080
AUTH_DB_PASSWORD=your_mysql_password
# Other vars: AUTH_MAIL_USERNAME, AUTH_MAIL_PASSWORD, etc.
```

### 2. Google Cloud Platform Setup
To enable **Google Sign-In**, add the following to your OAuth 2.0 Credentials in GCP:
- **Authorized JavaScript origins:** `http://localhost:4200`
- **Authorized redirect URIs:** `http://localhost:8080/login/oauth2/code/google`

### 3. LinkedIn Developer Portal Setup
To enable **LinkedIn Sign-In**, add this redirect URL in the LinkedIn app Auth settings:
- **Authorized redirect URL:** `http://localhost:8080/login/oauth2/code/linkedin`

### 4. Frontend Connection via API Gateway
The Angular application (`localhost:4200`) should call the API Gateway:
- **Auth Endpoint:** `http://localhost:8080/api/v1/auth`
- **Google OAuth Initiation:** `http://localhost:8080/oauth2/authorization/google`
- **LinkedIn OAuth Initiation:** `http://localhost:8080/oauth2/authorization/linkedin`

---

## 🎨 Phase 3 (Template Management)

This phase introduces the visual templating engine of the ecosystem.

### Template Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`template-service`** | `8082` | **Resume Template Management:** Handles CRUD operations, rendering, and fetching of professional resume templates. Includes comprehensive Javadoc, standardized JUnit 5/Mockito testing with H2, and global stateless security refactoring. |

### How to Run Template Service

1. **Start Template Service**
   ```bash
   cd template-service
   mvn spring-boot:run
   ```
   *Template APIs Swagger UI:* [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

---

## 📄 Phase 4 (Resume Management)

This phase focuses on the creation and lifecycle management of user resumes.

### Resume Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`resume-service`** | `8083` | **Resume Lifecycle Management:** Handles creating, updating, duplicating, and publishing resumes. Includes integration for ATS scoring and links to templates. |

### How to Run Resume Service

1. **Start Resume Service**
   ```bash
   cd resume-service
   mvn spring-boot:run
   ```
   *Resume APIs Swagger UI:* [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## 📊 Database Seeding

The microservices are pre-configured to seed the database with professional data on the first run.

- **`auth-service`**: Seeds default `ROLE_USER`, `ROLE_ADMIN`, and a sample user `johndoe` (password: `password123`).
- **`template-service`**: Seeds 5 high-quality resume templates (Professional, Creative, Minimalist, etc.) with real HTML/CSS structures.
- **`resume-service`**: Seeds a sample "Software Engineer" resume for the demo user.

> **Note:** Ensure `spring.sql.init.mode=always` and `spring.jpa.defer-datasource-initialization=true` are active in `application.yml` to ensure data is inserted after schema creation.

---

## 📑 Phase 5 (Modular Content Management)

This phase introduces the `section-service` for modular resume data management.

### Section Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`section-service`** | `8084` | **Modular Resume Sections:** Manages individual resume blocks (Experience, Education, Projects). Optimized with Redis caching for high performance. |

### How to Run All Services (Fast Way)

Use the automated launcher script to start all services in the correct sequence:
```bash

---

## 🤖 Phase 6 (Intelligence Layer)

This phase introduces AI-powered content generation and optimization.

### AI Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`ai-service`** | `8085` | **AI Content Generation:** Uses **Groq's OpenAI-compatible API** (via Spring AI) to generate professional summaries, write bullet points, analyze ATS compatibility, and translate resumes. Implements Quota management for Free users and unlimited access for Premium users. |

### How to Run AI Service

1. **Start AI Service**
   ```bash
   cd ai-service
   mvn spring-boot:run
   ```
   *AI APIs Swagger UI:* [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)

---

## 🖨️ Phase 7 (Export Management)

Currently, the **feature/UC7-export-service** branch of this repository represents **Phase 7** of the architecture. This phase handles document generation and download capabilities.

### Export Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`export-service`** | `8086` | **Document Export:** Manages the conversion of web-rendered HTML resumes into portable formats. Handles asynchronous generation requests, integrates PDF rendering via headless browsers/libraries, and tracks export quotas. |

### How to Run Export Service

1. **Start Export Service**
   ```bash
   cd export-service
   mvn spring-boot:run
   ```
   *Export APIs Swagger UI:* [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html)

---

## 🐛 Troubleshooting & Debugging Logs

### Recent Critical Fixes
1. **OAuth2 User Context Fix**: Resolved an issue where OAuth2 logins (Google/LinkedIn) failed to include `userId` and `subscriptionPlan` in the generated JWT token. This caused `401 Unauthorized` errors during resume creation and fetching. `OAuth2SuccessHandler` was updated to accurately map claims.
2. **Template Creation Bug**: Fixed a cascading error where clicking "Use this template" threw a creation failure. The root cause was identified as the missing `userId` in the OAuth JWT, which prevented the backend `resume-service` from correctly linking the new resume to the current user profile.
3. **Circular Dependency Fix**: Resolved a deadlock during startup where `resume-service` and `section-service` were waiting on each other. Implemented `@Lazy` loading for cross-service Feign clients.
4. **Export Service Architecture Simplification**: Removed legacy Apache POI DOCX generation overhead and complex watchdog scheduler logic from the `export-service`. This drastically simplified the backend and eliminated premature timeout failures for long-running export jobs.
