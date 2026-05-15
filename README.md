
# 🎓 LM-University — Smart Enrollment Service Platform for Higher Education

> A full-stack information management system for university enrollment scenarios, supporting university/major management, student registration, admission tracking, AI-powered consulting, and more.  
> Tech Stack: Spring Boot 3 + Vue 3 + TypeScript + MyBatis-Plus + MySQL.

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-green)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](./LICENSE)

---

## 📖 Table of Contents

- [Project Introduction](#project-introduction)
- [System Architecture](#system-architecture)
- [Functional Modules](#functional-modules)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [AI LLM Configuration (using DeepSeek as an example)](#ai-llm-configuration)
- [Database Overview](#database-overview)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Contribution Guidelines](#contribution-guidelines)
- [License](#license)

---

## Project Introduction

**LM-University** is a complete university enrollment service system designed for two types of users: **Administrators** and **Students**.

- **Administrator**: Manage universities/majors, publish announcements, process registrations and admissions, configure AI LLMs, etc., through the admin backend.
- **Student**: Browse enrollment information, register online, check admission results, and use the AI assistant for intelligent consulting via the student portal.

The backend adopts a **layered architecture + DDD modular design**, providing RESTful APIs. The frontend is built with **Vue 3 + Element Plus**, fully decoupled from the backend.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend Applications               │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  admin‑web       │         │  portal‑web       │          │
│  │  (Admin Backend) │         │  (Student Portal) │          │
│  └────────┬─────────┘         └────────┬─────────┘          │
│           │                  ▲          │                   │
│           └──────────────────┼──────────┘                   │
│                              │                              │
├──────────────────────────────┼──────────────────────────────┤
│                     RESTful API (/api/v1/*)                 │
├──────────────────────────────┼──────────────────────────────┤
│                        Backend (Spring Boot 3)              │
│  ┌──────┐ ┌──────────┐ ┌──────┐ ┌───────┐ ┌──────┐        │
│  │ auth │ │ admission │ │  ai  │ │content│ │ file │        │
│  ├──────┤ ├──────────┤ ├──────┤ ├───────┤ ├──────┤        │
│  │application│interaction│account│common│  legacy │        │
│  └──────┘ └──────────┘ └──────┘ └───────┘ └──────┘        │
│           │                                                │
│           └─────────────────┬──────────────────────────────│
│                             │ MySQL                        │
└─────────────────────────────┴──────────────────────────────┘
```

- **Backend**: Spring Boot 3 + MyBatis-Plus, split into business domains such as `auth` (authentication), `admission` (enrollment), `ai` (AI consulting), `content` (content management), following DDD principles.
- **Frontend**: Two independent Vue 3 projects using Vite for building, Pinia for state management, and Vue Router for routing.
- **Database**: MySQL, table structures from `sql/university.sql` (core tables) and `docs/ai-chat-tables.sql` (AI chat tables).
- **Infrastructure**: Docker Compose located in `infra/compose.yml` for quick development environment setup.

---

## Functional Modules

### Admin Portal (`apps/admin-web`)

| Module | Description |
|--------|-------------|
| Province / University / Major Management | Maintain basic data |
| Announcement & News Management | Publish enrollment news |
| Registration Review | Manage student registration records |
| Admission Management | Publish admission results |
| Score Management | Enter/query student scores |
| Consultation Reply | Manage consultation messages |
| AI Configuration | LLM integration (DeepSeek supported) |
| System Settings | Site info, About Us, etc. |

### Student Portal (`apps/portal-web`)

| Module | Description |
|--------|-------------|
| Browse Enrollment Info | View universities, majors, announcements |
| Online Registration | Submit registration applications |
| Admission Result Inquiry | Check admission status |
| Score Inquiry | View personal scores |
| Favorites | Favorite universities and majors |
| AI Assistant | LLM-based enrollment consulting (floating button bottom right) |

---

## Tech Stack

| Layer | Technology |
|-------|-------------|
| Backend Framework | Spring Boot 3.3.2 |
| ORM | MyBatis-Plus 3.5.7 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | MySQL 8.x |
| Connection Pool | HikariCP (Spring Boot default) |
| API Documentation | SpringDoc OpenAPI 2.6.0 |
| Frontend Framework | Vue 3.4 + TypeScript |
| Build Tool | Vite 5 |
| State Management | Pinia 2 |
| UI Framework | Element Plus 2.7 (admin-web only) |
| AI Integration | Generic HTTP SSE streaming (supports DeepSeek, etc.) |
| Containerization | Docker Compose |

---

## Quick Start

### Requirements

- **JDK 17+**
- **Maven 3.8+**
- **Node.js 18+** / npm
- **MySQL 8.0+**

### 1. Clone Repository

```bash
git clone https://github.com/lsyaizyl/LM-University.git
cd LM-University
```

### 2. Initialize Database

Execute the following SQL scripts:

```bash
# Core table structure
mysql -u root -p < sql/university.sql

# AI chat related tables
mysql -u root -p < docs/ai-chat-tables.sql

# Student score field (if needed)
mysql -u root -p < docs/student-score-field.sql
```

### 3. Backend Configuration

Configure database connection in `backend/src/main/resources/application.properties` (or via environment variables):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/university?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Start Backend

**Windows (double-click)**:
```cmd
run-backend.cmd
```

**macOS / Linux**:
```bash
./mvnw spring-boot:run
```

Backend runs by default at `http://localhost:8080`.

### 5. Start Frontend

#### Admin Backend (admin-web)

**Windows (double-click)**:
```cmd
run-admin-web.cmd
```

**npm**:
```bash
cd apps/admin-web
cp .env.example .env
npm install
npm run dev
```

Admin backend runs by default at `http://localhost:5173`.

#### Student Portal (portal-web)

**Windows (double-click)**:
```cmd
run-portal-web.cmd
```

**npm**:
```bash
cd apps/portal-web
cp .env.example .env
npm install
npm run dev
```

Student portal runs by default at `http://localhost:5174`.

### 6. (Optional) Use Docker Compose

```bash
cd infra
cp .env.example .env
docker-compose -f compose.yml up -d
```

---

## Project Structure

```
LM-University/
├── apps/                        # Frontend applications
│   ├── admin-web/               # Vue 3 admin backend (Element Plus)
│   │   ├── src/
│   │   │   ├── api/             # API layer
│   │   │   ├── components/      # Common components
│   │   │   ├── router/          # Routing configuration
│   │   │   ├── stores/          # Pinia state management
│   │   │   ├── types/           # TypeScript type definitions
│   │   │   ├── views/           # Page views
│   │   │   ├── App.vue
│   │   │   └── main.ts
│   │   ├── package.json
│   │   ├── vite.config.ts
│   │   └── .env.example
│   └── portal-web/              # Vue 3 student portal
│       └── ... (same structure as admin-web)
├── backend/                     # Spring Boot backend
│   ├── src/main/java/com/university/backend/
│   │   ├── account/             # Account system (Account, Role)
│   │   ├── admission/           # Admission management
│   │   ├── ai/                  # AI LLM integration (DeepSeek, etc.)
│   │   ├── application/         # Registration management
│   │   ├── auth/                # Authentication & authorization (JWT)
│   │   ├── common/              # Common components (API response, security, config)
│   │   ├── content/             # Content management (announcements, news, About Us)
│   │   ├── file/                # File upload
│   │   ├── interaction/         # Interaction (favorites, consultations)
│   │   └── legacy/              # Legacy system compatibility layer
│   ├── src/test/                # Test cases
│   └── pom.xml
├── docs/                        # Project documentation
│   ├── ai-model-config-guide.md # AI LLM configuration tutorial
│   ├── data-model-and-migration.md # Data model explanation
│   ├── ai-chat-tables.sql       # AI chat table structures
│   └── student-score-field.sql  # Score field extension
├── infra/                       # Infrastructure
│   ├── compose.yml              # Docker Compose
│   ├── .env.example
│   └── sql/                     # Data quality reports
├── sql/                         # Database initialization scripts
│   └── university.sql
├── upload/                      # File upload directory
├── pom.xml                      # Maven parent POM
├── mvnw / mvnw.cmd              # Maven Wrapper
└── run-*.cmd                    # Windows one-click startup scripts
```

---

## AI LLM Configuration

The system integrates LLMs via a **generic HTTP API template**, not tied to any specific provider. Currently supports **DeepSeek**; other providers (OpenAI, Alibaba Bailian, etc.) can be integrated by adjusting the request template.

### Quick Configuration Steps

1. **Run the table creation script**:
   ```sql
   source docs/ai-chat-tables.sql;
   ```

2. **Go to Admin Backend → AI Configuration page**, fill in the following key items:

   | Configuration Item | Example Value |
      |--------------------|----------------|
   | API URL            | `https://api.deepseek.com/chat/completions` |
   | API Key            | Your DeepSeek API Key |
   | Model              | `deepseek-v4-flash` |
   | Streaming Protocol | SSE |
   | Text Path          | `choices.0.delta.content` |
   | End Marker         | `[DONE]` |

3. **Click "Test Connection"**, then enable the AI feature.

4. **Student portal validation**: Log in as a student, click the AI floating button at the bottom right, and ask questions.

> 📖 For detailed configuration tutorial, see: [docs/ai-model-config-guide.md](docs/ai-model-config-guide.md)

### Security Design

- API Key is **never returned in plaintext**; the backend only returns `apiKeySet: true/false`.
- Student AI context **only contains data of the current student**, no leakage of other students' information.
- The student chat endpoint does not accept `studentId` from the frontend; it is parsed from the JWT token by the backend.

---

## Database Overview

### Core Tables

| Table | Description |
|-------|-------------|
| `users` | Administrator accounts |
| `student` | Student accounts and profiles |
| `province` | Province data |
| `universityinformation` | University information |
| `professionalinformation` | Major information |
| `collegeapplication` | University registration |
| `professionalregistration` | Major registration |
| `admissionresults` | Admission results |
| `resultsinformation` | Score information |
| `news` | News and announcements |
| `aboutus` | About Us |
| `systemintro` | System introduction |
| `config` | Application configuration |
| `chat` | Consultation messages |
| `storeup` | Favorites |
| `ai_model_config` | AI LLM configuration |
| `ai_chat_conversation` | AI chat conversation |
| `ai_chat_message` | AI chat message |

> 📖 For complete data model explanation, see: [docs/data-model-and-migration.md](docs/data-model-and-migration.md)

---

## API Documentation

After starting the backend, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

All APIs share the common prefix: `/api/v1/*`

---

## Deployment

### Backend Deployment

```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/backend-1.0.0-SNAPSHOT.jar
```

### Frontend Deployment

```bash
# Admin backend
cd apps/admin-web
npm run build       # Output to dist/

# Student portal
cd apps/portal-web
npm run build       # Output to dist/
```

Deploy the `dist/` directory to Nginx or another web server.

### Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_URL` | Database connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing key (must be changed in production) |

---

## Contribution Guidelines

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## License

This project is open-sourced under the [MIT License](./LICENSE).

---

<p align="center">
  <b>LM-University</b> — Smarter, More Efficient University Enrollment 🚀
</p>
```