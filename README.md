# TaskMaster: Collaborative Task Tracking System

A production-ready **Spring Boot 3** backend REST API for collaborative task management with **JWT authentication**, **MySQL**, **real-time WebSocket notifications**, and full **Swagger UI** documentation.

---

## Architecture Overview

```
Client (Browser / Postman / Mobile App)
            │
            ▼
  ┌─────────────────────────────┐
  │   Spring Security Layer     │  ← JWT Authentication Filter
  └─────────────┬───────────────┘
                │
  ┌─────────────▼───────────────┐
  │     REST Controllers        │  ← JSON over HTTP
  │   + WebSocket Endpoint      │  ← STOMP / SockJS
  └─────────────┬───────────────┘
                │
  ┌─────────────▼───────────────┐
  │       Service Layer         │  ← Business Logic
  └─────────────┬───────────────┘
                │
  ┌─────────────▼───────────────┐
  │  Repository Layer (JPA)     │  ← Spring Data JPA
  └─────────────┬───────────────┘
                │
  ┌─────────────▼───────────────┐
  │        MySQL 8.x            │
  └─────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Build Tool | Gradle 8 |
| Database | MySQL 8.x |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + JWT (jjwt 0.12) |
| Real-time | WebSocket + STOMP + SockJS |
| API Docs | SpringDoc OpenAPI 2.3 (Swagger UI) |
| Java | Java 17 |

---

## API Endpoints Reference

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login — receive JWT access + refresh tokens |
| POST | /api/auth/refresh | Refresh access token |
| POST | /api/auth/logout | Logout (stateless — client discards token) |

### User Profile
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/users/me | Get own profile |
| PUT | /api/users/me | Update profile |
| GET | /api/users/{id} | Get user by ID |
| GET | /api/users | List all users |

### Tasks
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/tasks | Create task |
| GET | /api/tasks/{id} | Get task by ID |
| GET | /api/tasks/my?status=OPEN&sortBy=dueDate | My tasks (filter + sort) |
| GET | /api/tasks/team/{teamId}?status=IN_PROGRESS | Team tasks |
| GET | /api/tasks/search?keyword=login&teamId=1 | Full-text search |
| PUT | /api/tasks/{id} | Update task |
| PATCH | /api/tasks/{id}/complete | Mark as completed |
| DELETE | /api/tasks/{id} | Delete task |

### Teams
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/teams | Create team |
| GET | /api/teams/my | My teams |
| GET | /api/teams/{id} | Get team |
| POST | /api/teams/join/{inviteCode} | Join by invite code |
| POST | /api/teams/{id}/members/{userId} | Add member (owner only) |
| DELETE | /api/teams/{id}/members/{userId} | Remove member (owner only) |
| POST | /api/teams/{id}/regenerate-code | New invite code |

### Comments & Attachments
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/tasks/{id}/comments | Add comment |
| GET | /api/tasks/{id}/comments | List comments |
| PUT | /api/comments/{id} | Edit comment |
| DELETE | /api/comments/{id} | Delete comment |
| POST | /api/tasks/{id}/attachments | Upload file (multipart) |
| GET | /api/tasks/{id}/attachments | List attachments |
| GET | /api/attachments/{id}/download | Download file |
| DELETE | /api/attachments/{id} | Delete attachment |

### Notifications
| Method | Endpoint | Description |
|---|---|---|
| GET | /api/notifications | Get all notifications |
| GET | /api/notifications/unread-count | Unread count |
| PATCH | /api/notifications/mark-all-read | Mark all read |
| PATCH | /api/notifications/{id}/read | Mark one read |

---

## Real-Time Notifications (WebSocket)

Connect via SockJS: `http://localhost:8080/ws`

1. Connect and send STOMP CONNECT frame with header `Authorization: Bearer <token>`
2. Subscribe to `/user/queue/notifications`
3. Receive JSON notification payloads in real-time when tasks are assigned, updated, or commented on

---

## Quick Start

### Prerequisites
- Java 17+
- MySQL 8.x (running locally)
- Gradle 8+

### 1. Clone / Extract the Project
```bash
cd taskmaster/
```

### 2. Create MySQL Database
```sql
CREATE DATABASE taskmaster_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    username: YOUR_MYSQL_USER
    password: YOUR_MYSQL_PASSWORD
```

### 4. Run the Application
```bash
./gradlew bootRun
```

### 5. Load Sample Data
```bash
mysql -u root -p taskmaster_db < src/main/resources/data.sql
```

### 6. Open Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## Sample Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"alice","password":"password123"}'
```

### Sample Users (password: `password123`)
| Username | Email | Role |
|---|---|---|
| alice | alice@example.com | USER |
| bob | bob@example.com | USER |
| carol | carol@example.com | USER |
| admin | admin@taskmaster.com | ADMIN |

---

## Project Structure

```
src/main/java/com/taskmaster/
├── TaskMasterApplication.java
├── config/          SecurityConfig, WebSocketConfig, OpenApiConfig, AppConfig
├── controller/      AuthController, UserController, TaskController,
│                    TeamController, CommentController, AttachmentController,
│                    NotificationController
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, UpdateProfileRequest,
│   │                TaskRequest, CommentRequest, TeamRequest
│   └── response/    ApiResponse, AuthResponse, UserResponse, TaskResponse,
│                    TeamResponse, CommentResponse, AttachmentResponse,
│                    NotificationResponse
├── entity/          User, Team, Task, Comment, Attachment, Notification
├── exception/       GlobalExceptionHandler, ResourceNotFoundException,
│                    BadRequestException
├── repository/      UserRepository, TeamRepository, TaskRepository,
│                    CommentRepository, AttachmentRepository, NotificationRepository
├── security/        JwtTokenProvider, JwtAuthenticationFilter,
│                    CustomUserDetailsService
└── service/         AuthService, UserService, TaskService, TeamService,
                     CommentService, AttachmentService, NotificationService
```

---

## Class Diagram

See [`docs/class-diagram.md`](docs/class-diagram.md)

## Eclipse Setup

See [`ECLIPSE_SETUP_GUIDE.md`](ECLIPSE_SETUP_GUIDE.md) for step-by-step instructions
