# ResumeAI: AI-Powered Resume Builder Platform (Backend)
**Current Branch:** `feature/UC5-Section-Service`

## 📌 Project Overview / Introduction

**ResumeAI** is a full-stack AI-Powered Resume Builder platform designed to empower job seekers to create, optimize, and export professional resumes with the help of cutting-edge AI language models. 

The platform utilizes a robust dual-AI strategy with cross-model failover to ensure 100% uptime and optimal quality:
*   **Free Tier:** **Google Gemini (1.5 Flash)** is the primary model. If Gemini is unavailable, the system automatically fails over to **Anthropic Claude**.
*   **Premium Tier:** **Anthropic Claude (3.5 Sonnet)** is the primary model for professional-grade quality. If Claude is unavailable, the system automatically fails over to **Google Gemini**.

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

Currently, the **feature/UC5-section-service** branch of this repository represents **Phase 5** of the architecture. This phase introduces the `section-service` for modular resume data management.

### Section Service

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **`section-service`** | `8084` | **Modular Resume Sections:** Manages individual resume blocks (Experience, Education, Projects). Optimized with Redis caching for high performance. |

### How to Run All Services (Fast Way)

Use the automated launcher script to start all services in the correct sequence:
```bash
start_all_services.bat
```
