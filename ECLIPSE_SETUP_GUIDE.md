# Eclipse IDE Setup Guide — TaskMaster

## Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Eclipse IDE for Enterprise Java | 2023-12+ | https://www.eclipse.org/downloads/ |
| MySQL Server | 8.x | https://dev.mysql.com/downloads/mysql/ |
| MySQL Workbench (optional) | Any | https://dev.mysql.com/downloads/workbench/ |

---

## Step 1 — Install Buildship (Gradle Plugin for Eclipse)

1. Open Eclipse → **Help → Eclipse Marketplace**
2. Search: **Buildship Gradle Integration 3.0**
3. Click **Install** → Accept license → Restart Eclipse

---

## Step 2 — Create MySQL Database

**Option A — MySQL Workbench:**
Open a query tab and run:
```sql
CREATE DATABASE taskmaster_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

**Option B — Terminal:**
```bash
mysql -u root -p -e "CREATE DATABASE taskmaster_db CHARACTER SET utf8mb4;"
```

---

## Step 3 — Update Database Credentials

Open `src/main/resources/application.yml` and update:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/taskmaster_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root          # ← your MySQL username
    password: your_password # ← your MySQL password
```

> **Tip:** If MySQL runs on a different port, change `3306` accordingly.

---

## Step 4 — Import Project into Eclipse

1. **File → Import**
2. Expand **Gradle** → select **Existing Gradle Project** → click **Next**
3. Click **Browse** next to "Project root directory"
4. Navigate to and select the `taskmaster` folder → click **OK**
5. Click **Next → Finish**
6. Eclipse will resolve and download all Gradle dependencies (~2 minutes)
7. Wait for the "Gradle Project" to appear in the **Package Explorer**

---

## Step 5 — Set Java 17 Compiler

1. Right-click the project → **Properties**
2. Go to **Java Compiler**
3. Enable project-specific settings → set **Compiler compliance level** to **17**
4. Go to **Java Build Path → Libraries**
5. If JRE System Library shows < 17:
   - Double-click it → **Alternate JRE** → **Installed JREs**
   - Click **Add → Standard VM** → browse to your JDK 17 folder
   - Apply and Close

---

## Step 6 — Run the Application

### Option A — Spring Boot Dashboard (Recommended)
1. **Window → Show View → Other → Spring → Spring Boot Dashboard**
2. The `taskmaster` app appears in the dashboard
3. Click the ▶ (Start) button

### Option B — Run As Java Application
1. Expand `src/main/java/com/taskmaster`
2. Right-click `TaskMasterApplication.java`
3. **Run As → Java Application**

### Option C — Gradle bootRun
1. **Window → Show View → Other → Gradle → Gradle Tasks**
2. Expand `taskmaster → application`
3. Double-click **bootRun**

### Expected Console Output
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
Started TaskMasterApplication in 4.532 seconds
Tomcat started on port(s): 8080 (http)
```

---

## Step 7 — Load Sample Data

After the app has started (Hibernate auto-creates all tables):

**Option A — MySQL Workbench:**
1. File → Open SQL Script
2. Select `src/main/resources/data.sql`
3. Click the lightning bolt ⚡ to execute

**Option B — Terminal:**
```bash
mysql -u root -p taskmaster_db < src/main/resources/data.sql
```

---

## Step 8 — Verify & Test with Swagger UI

1. Open: **http://localhost:8080/swagger-ui.html**
2. You will see the full interactive API documentation

### Step-by-step test:

**A) Register a new user**
```
POST /api/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "test1234",
  "fullName": "Test User"
}
```

**B) Login with sample user**
```
POST /api/auth/login
{
  "usernameOrEmail": "alice",
  "password": "password123"
}
```
→ Copy the `accessToken` from the response.

**C) Authorize in Swagger**
1. Click **Authorize 🔓** (top right of Swagger UI)
2. In the `bearerAuth` field enter: `Bearer eyJhbGci...` (paste your token)
3. Click **Authorize → Close**

**D) Create a Task**
```
POST /api/tasks
{
  "title": "My first task",
  "description": "Testing TaskMaster",
  "priority": "HIGH",
  "dueDate": "2026-05-01",
  "assigneeId": 2,
  "teamId": 1
}
```

**E) Get My Tasks**
```
GET /api/tasks/my?status=OPEN&sortBy=dueDate&sortDir=asc
```

---

## Sample Users (all passwords: `password123`)

| Username | Email | Role | ID |
|---|---|---|---|
| alice | alice@example.com | USER | 1 |
| bob | bob@example.com | USER | 2 |
| carol | carol@example.com | USER | 3 |
| admin | admin@taskmaster.com | ADMIN | 4 |

### Sample Teams
| Team | Invite Code |
|---|---|
| Alpha Team | ALPHA12345 |
| Beta Testers | BETA678910 |

---

## WebSocket Test (Real-time Notifications)

Use the **Simple WebSocket Client** Chrome extension or Postman:

1. Connect to: `http://localhost:8080/ws/websocket`
2. Use STOMP over SockJS
3. Send CONNECT frame with header `Authorization: Bearer <token>`
4. Subscribe to: `/user/queue/notifications`
5. Assign a task to yourself via REST API → notification arrives instantly

---

## Troubleshooting

| Problem | Solution |
|---|---|
| `Communications link failure` | Start MySQL: `sudo service mysql start` |
| `Access denied for user 'root'` | Check `application.yml` username/password |
| `Port 8080 already in use` | Add `server.port: 8081` to `application.yml` |
| Tables don't exist | Let the app start first (Hibernate creates them with `ddl-auto: update`) |
| `Invalid JWT signature` | Token expired — POST `/api/auth/login` again |
| Gradle import fails | Ensure Buildship plugin is installed; try **Gradle → Refresh Gradle Project** |
| `java.lang.UnsupportedClassVersionError` | Eclipse is using Java < 17; set JDK 17 in project properties |
