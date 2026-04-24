# Paper Review System
### Spring Boot Full-Stack Mini Project | SY B.Tech | EAD (Enterprise Application Development)

---

## Overview

The **Paper Review System** is a full-stack web application that digitizes the academic marks review process in an engineering college. Before this system, students had to physically approach teachers to question their marks, with no structured tracking of queries. This system brings the entire process online — from marks entry to query resolution — with proper role-based access and analytics.

**Problem Solved:**
- Students could not verify their marks digitally
- Teachers had no structured system to manage queries
- Admins had no visibility into performance patterns
- Marks data was inconsistent due to duplicate entries

**Solution Provided:**
- Three-role secure system: Admin, Teacher, Student
- Digital marks entry with duplicate prevention (upsert logic)
- Structured query submission and resolution workflow
- Real-time socket-based alert when a query is resolved
- Admin analytics dashboard for performance monitoring

---

## Features

### Admin Features

| Feature | Description |
|---|---|
| Login | Secure login with role-based redirect to /admin.html |
| Create Teacher | Register teacher account with username, password, name |
| Assign Subjects | Assign comma-separated subject IDs to each teacher (e.g., "1,3") |
| Assign Divisions | Assign comma-separated divisions to each teacher (e.g., "A,B") |
| Edit Teacher | Change a teacher's subject and division assignments |
| Delete Teacher | Remove teacher from both the teacher table and users table |
| View All Teachers | Table showing all teachers with their assignments |
| View Performance | Per-student average marks sorted by percentage |
| View Users (Tabbed) | View Student accounts, Teacher accounts, or All users separately |
| Analytics Report | Division-wise and subject-wise performance bar charts |
| Manage Marks | Access marks entry and query pages directly |

### Teacher Features

| Feature | Description |
|---|---|
| Login | Secure login with redirect to /teacher.html |
| View My Assignments | Dashboard shows assigned subjects and divisions |
| Bulk Marks Entry | Select division + subject + question count → dynamic table generated |
| Dynamic Questions | Teacher chooses how many questions (1–15), Q1...QN generated automatically |
| Upsert Marks | Entering marks updates existing row — no duplicate rows created |
| Access Restriction | Subject and division dropdowns disable options not assigned to teacher |
| View Queries | See all student queries with Pending/Resolved status |
| Resolve Query | Update marks and resolve query in one step; triggers socket alert |
| Socket Alert | On resolution, a TCP socket message is sent to QueryAlertServer |

### Student Features

| Feature | Description |
|---|---|
| Self-Registration | Register with PRN, name, division, username, password |
| Login | Secure login with redirect to /student.html |
| Auto PRN | PRN is read from session — student never types their own PRN manually |
| View My Marks | Thymeleaf server-rendered page showing all marks |
| Subject Filter | Dropdown to filter marks by specific subject |
| Raise Query | Submit a query for any specific question with a message |
| Query Tracking | Query status shows as Pending or Resolved |

---

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Backend Framework | Spring Boot 3.5.13 | Application configuration, embedded Tomcat, auto-wiring |
| Language | Java 21 | Core programming language |
| Security | Spring Security | Authentication, role-based URL protection, login/logout |
| ORM | Spring Data JPA + Hibernate 6 | Maps Java entities to MySQL tables, auto-generates CRUD queries |
| Raw SQL | JDBC + PreparedStatement | Batch insert and fetch operations (CO1 requirement) |
| Template Engine | Thymeleaf | Server-side HTML rendering (student marks page — CO2) |
| Validation | Spring Boot Validation | @NotBlank, @Size, @Min, @Max on entity fields |
| Database | MySQL 8 | Relational database — paper_system schema |
| Frontend | HTML5 + CSS3 + JavaScript | Static pages for all UI interactions |
| Networking | Java Socket / ServerSocket | Real-time TCP alert when query is resolved (CO4) |
| Concurrency | @Async + @Scheduled | Background notifications and scheduled tasks (CO1) |
| Transaction | @Transactional | Database rollback on failure during query resolution |
| Build Tool | Maven | Dependency management and project build |
| Server Port | 8082 | Embedded Apache Tomcat |

---

## Architecture

### MVC Architecture

This project follows the standard Spring Boot MVC pattern:

```
Browser (Client)
    |
    |  HTTP Request (GET / POST)
    v
Spring Security Filter
    |  checks role → allows or blocks
    v
Controller Layer  (@RestController / @Controller)
    |  receives request params, calls service
    v
Service Layer  (@Service)
    |  business logic, upsert, resolve query, validate teacher access
    v
Repository Layer  (@Repository / JpaRepository)
    |  generates SQL, communicates with DB
    v
MySQL Database (paper_system)
    |
    | [On query resolve only]
    v
Socket Client (@Async Thread)
    |  opens TCP connection to port 9090
    v
Socket Server (background daemon thread)
    |  prints alert
```

### Request-Response Flow Example (Student views marks)

```
1. Student visits http://localhost:8082/view/marks
2. Spring Security checks: is the user logged in with STUDENT role?
3. If yes → StudentViewController.viewMyMarks() is called
4. Controller reads logged-in username from Spring Security session
5. Finds the User record → gets the PRN linked to that username
6. Calls MarksService.getMarksByPrn(prn)
7. MarksService calls MarksRepository.findByPrn(prn)
8. Hibernate generates SQL: SELECT * FROM marks WHERE prn = ?
9. Returns List<Marks> to service → to controller
10. Controller adds data to Thymeleaf Model
11. Thymeleaf renders student-marks.html with marks injected
12. Complete HTML page sent to browser (no further API calls needed)
```

---

## Database Schema

| Table | Purpose | Important Fields |
|---|---|---|
| `users` | Login accounts for all roles | id, username, password, role, prn |
| `student` | Academic records for students | prn (PK), name, division |
| `teacher` | Teacher profiles with access info | id (PK AUTO), name, username (UNIQUE), subject_ids, divisions |
| `subject` | Subject master list | id (PK), name |
| `marks` | Marks records with upsert guarantee | id, prn (FK), subject_id (FK), question, marks_obtained, max_marks |
| `query` | Student queries for marks review | id, prn, subject_id, question, message, status |

### Key Design Decisions

**users.prn**: Links a STUDENT login account to their academic student record. For TEACHER and ADMIN accounts, this field is NULL.

**teacher.username**: Links the Teacher entity (with subject/division assignments) to the User entity (login credentials). Same value as users.username for that teacher.

**teacher.subject_ids**: Stores assigned subjects as a comma-separated string, e.g., `"1,3"`. Simple approach — no extra join table needed at SY level. Parsed using Java String.split(",").

**teacher.divisions**: Stores assigned divisions as a comma-separated string, e.g., `"A,B"`. Same approach.

**marks UNIQUE KEY**: The marks table has a unique constraint on (prn, subject_id, question). This prevents duplicate marks rows for the same question. When marks are entered again, the existing row is updated (upsert) instead of inserting a duplicate.

### ER Diagram (Text Format)

```
USERS (id PK, username UNIQUE, password, role, prn)
  |
  |-- prn links to --> STUDENT (prn PK, name, division)
  |
  |-- username links to --> TEACHER (id PK, name, username UNIQUE, subject_ids, divisions)

STUDENT (prn PK, name, division)
  |
  |--< MARKS (id PK, prn FK, subject_id FK, question, marks_obtained, max_marks)
              UNIQUE KEY (prn, subject_id, question)

SUBJECT (id PK, name)
  |
  |--< MARKS.subject_id FK --> SUBJECT.id

QUERY (id PK, prn, subject_id, question, message, status)
  [No FK enforced — kept simple at SY level]
```

---

## Key Functionalities Explained

### 1. Login System
Spring Security intercepts all requests. The `CustomUserDetailsService` loads the user from the `users` table using case-insensitive username matching. On successful login, a `successHandler` reads the user's role and redirects:
- ADMIN → /admin.html
- TEACHER → /teacher.html
- STUDENT → /student.html

### 2. Role-Based Access
`SecurityConfig.java` defines which URLs are allowed for which role using `hasAuthority()`. Static pages and API endpoints are each individually protected. If an unauthorized user tries to access a restricted URL, Spring Security returns 403 Forbidden.

### 3. Marks Entry (Upsert — No Duplicates)
When a teacher submits marks for a student:
1. `MarksController.addMarks()` is called
2. It checks teacher's allowed subjects and divisions (restriction enforcement)
3. Calls `MarksService.upsertMarks(prn, subjectId, question, marks, maxMarks)`
4. Service calls `MarksRepository.findExact(prn, subjectId, question)` — JPQL query
5. If a row exists → UPDATE `marks_obtained`
6. If no row → INSERT new Marks object
7. `marksRepo.save(m)` handles both cases

### 4. Query System
Student submits a query → stored with status "Pending". Teacher opens `/viewQueries.html`, clicks Resolve → navigates to `/updateMarks.html`. Teacher enters new marks and submits → `POST /marks/update` → `MarksService.updateMarksAndResolveQuery()` → updates marks AND calls `QueryService.resolveQuery()` which:
1. Sets status = "Resolved" (`@Transactional` — rolled back if anything fails)
2. Calls `NotificationService.sendResolutionAlert()` — runs `@Async` in background thread
3. Calls `QueryAlertClient.sendResolvedAlert()` — sends TCP socket message to QueryAlertServer

### 5. Teacher Access Restriction
Teacher's assigned subjects and divisions are stored in the `teacher` table. On marks entry:
- Backend: `MarksController` checks `teacher.isAllowedSubject(subjectId)` and `teacher.isAllowedDivision(division)` → returns 403 if not allowed
- Frontend: `marksEntry.html` calls `GET /teacher/my-info` on load → gets assigned subjects/divisions → disables dropdown options the teacher is not assigned to

### 6. Analytics
Admin visits `/admin-report.html` which loads:
- `GET /admin/report/division` → groups students by division, sums all marks, calculates percentage
- `GET /admin/report/subject` → groups all marks by subject ID, calculates average percentage
- `GET /admin/performance` → per-student total marks sorted by percentage descending

---## 🔹 JDBC Batch Insert (CO1 Requirement)

The system demonstrates low-level database interaction using JDBC in addition to Hibernate ORM.

A dedicated DAO class `JdbcMarksDao` is used to perform direct SQL operations using:
- DriverManager for connection
- PreparedStatement for parameterized queries
- addBatch() and executeBatch() for bulk insertion

This allows inserting multiple marks records in a single database call, improving performance.

Flow:
Browser/API → JdbcMarksController → JdbcMarksService → JdbcMarksDao → Database

Example Endpoint:
GET /jdbc/marks/batch

This endpoint inserts multiple records at once using batch processing.

---

## 🔹 Hibernate vs JDBC (Design Choice)

| Feature | Hibernate (JPA) | JDBC |
|--------|----------------|------|
| Abstraction | High (ORM) | Low (manual SQL) |
| Code Complexity | Low | Higher |
| Performance | Moderate | High (batch) |
| Usage in Project | Main system (CRUD) | Batch insert demo |

Hibernate is used for most operations due to ease of development, while JDBC is used to demonstrate efficient batch operations.

---

## 🔹 Duplicate Query Prevention

The system prevents duplicate queries from being submitted by students.

Before inserting a new query, the backend checks:
- PRN
- Subject ID
- Question

If a query already exists with status "Pending", the system rejects the new request.

This ensures:
- No duplicate query spam
- Clean query management

---

## 🔹 Admin Query Management

Admins have full control over queries.

They can:
- View all queries
- Filter queries
- Delete inappropriate or duplicate queries

---

## 🔹 Teacher Access Restriction (VERY IMPORTANT)

Teachers are restricted to operate only on assigned:

- Subjects
- Divisions

Each teacher has:
- subject_ids → e.g. "1,3"
- divisions → e.g. "A,B"

Restrictions applied on:
- Marks Entry
- Update Marks
- Resolve Queries

Teacher CANNOT:
- Access other subjects
- Access other divisions
- Resolve queries outside their assignment

This restriction is enforced at:
- Backend (validation)
- Frontend (filtered UI)

---

## 🔹 Servlet Filter (Request Logging)

A custom Servlet Filter is implemented to intercept incoming HTTP requests.

The filter:
- Logs request URI
- Executes before controller logic

This demonstrates Servlet API usage.

---

## 🔹 Concept Coverage (CO Mapping)

✔ OOP (Entity classes, layered architecture)  
✔ JDBC (JdbcMarksDao, batch insert)  
✔ Hibernate (JPA repositories)  
✔ Servlet API (Filter implementation)  
✔ Spring Security (Role-based login)  
✔ Multithreading (Socket + Scheduler)  
✔ Socket Programming (Real-time alerts)  
✔ MVC Architecture (Controller–Service–Repository)

## How to Run

**Prerequisites:** Java 21, Maven, MySQL 8

**Step 1: Create Database**
```sql
CREATE DATABASE paper_system;
```

**Step 2: Configure application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/paper_system
spring.datasource.username=root
spring.datasource.password=root
server.port=8082
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Step 3: Start the Application**
```bash
cd paperreview
mvn spring-boot:run
```
Wait for: `Started PaperreviewApplication` in the console. Also look for:
```
[SOCKET SERVER] Starting on port 9090...
[SOCKET SERVER] Ready. Listening for alerts (thread-per-client).
```

**Step 4: Run One-Time SQL Setup**
```sql
-- Insert admin user
INSERT IGNORE INTO users (username, password, role, prn) 
VALUES ('admin', 'admin123', 'ADMIN', NULL);

-- Insert subjects
INSERT IGNORE INTO subject (id, name) VALUES (1,'ADS'), (2,'Java'), (3,'TOC'), (4,'DBMS');

-- Add unique constraint to marks (prevents duplicate entries)
ALTER TABLE marks 
ADD UNIQUE INDEX IF NOT EXISTS uq_marks_combo (prn, subject_id, question);
```

**Step 5: Open in Browser**
```
http://localhost:8082/login.html
```

---

## Sample Credentials

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Teacher | sharma (created via admin) | sharma123 |
| Student | priya (self-registered) | priya123 |

---

## Testing Guide

### Test 1 — Student Registration
```
1. Open http://localhost:8082/login.html
2. Click Register link → fill: PRN=S001, Name=Priya, Division=A, Username=priya, Password=priya123
3. Click Register
4. Expected: "Student registered! PRN: S001, Username: priya"
5. Check DB: SELECT * FROM users WHERE username='priya'; → should show role=STUDENT, prn=S001
```

### Test 2 — Teacher Creation and Assignment
```
1. Login as admin
2. Click "Add Teacher" → registerTeacher.html
3. Fill: Name=Prof Sharma, Username=sharma, Password=sharma123
4. Check boxes: Subjects=TOC(3), Divisions=B
5. Click Create
6. Login as sharma → teacher.html should show: Subjects: TOC | Divisions: B
```

### Test 3 — Teacher Access Restriction
```
1. Login as sharma (assigned only TOC, Division B)
2. Go to marksEntry.html
3. Division dropdown: A, C, D should appear disabled (greyed out)
4. Subject dropdown: ADS, Java, DBMS should appear disabled
5. Select Division B + TOC → load students → should work
6. Select Division A → click Load → backend returns 403 Access denied
```

### Test 4 — Duplicate Marks Prevention (Upsert)
```
1. Login as teacher
2. Enter Q1=8 for student S001, Subject TOC → Save Row
3. Enter Q1=9 for same student S001, Subject TOC → Save Row again
4. Check DB: SELECT COUNT(*), marks_obtained FROM marks 
   WHERE prn='S001' AND subject_id=3 AND question='Q1';
   Expected: COUNT=1, marks_obtained=9 (updated, not duplicated)
```

### Test 5 — Socket Alert on Query Resolution
```
1. Student S001 raises query: Subject=TOC, Question=Q1, Message="My answer was correct"
2. Teacher resolves query: updates marks, clicks submit
3. Watch console output:
   [SOCKET CLIENT] Sending alert: QUERY RESOLVED | Student PRN: S001 | Question: Q1
   [SOCKET ALERT] QUERY RESOLVED | Student PRN: S001 | Question: Q1
   [SOCKET ALERT] Handled by: socket-client-XXXXX
```

### Test 6 — Admin Analytics
```
1. Login as admin → click View Analytics
2. admin-report.html loads
3. Division bar chart shows percentages for each division
4. Subject bar chart shows each subject's average performance
5. Student table shows all students sorted by percentage descending
```

---

## UI Pages Description

| Page | URL | Role | Description |
|---|---|---|---|
| Login | /login.html | All | Username/password form. Shows error on failure. Demo credentials shown. |
| Student Dashboard | /student.html | STUDENT | Shows logged-in name and PRN (auto-fetched). Two cards: View Marks, Raise Query. |
| Student Marks | /view/marks | STUDENT | Thymeleaf page. Shows all marks. Subject dropdown filter. Raise Query links per row. |
| Raise Query | /query.html | STUDENT | Pre-filled from URL params. Textarea for message. Success screen on submit. |
| Teacher Dashboard | /teacher.html | TEACHER | Shows assigned subjects/divisions. Cards: Enter Marks, Update Marks, View Queries. |
| Marks Entry | /marksEntry.html | TEACHER/ADMIN | Select division + subject + question count. Dynamic table with N questions. Save per row or all at once. |
| View Queries | /viewQueries.html | TEACHER/ADMIN | Table with all queries. Filter: All/Pending/Resolved. Stats strip showing counts. |
| Update Marks | /updateMarks.html | TEACHER/ADMIN | Pre-filled from URL. Enter new marks + resolve query in one step. |
| Admin Dashboard | /admin.html | ADMIN | Action cards. Teacher management table with Edit/Delete. Performance table. Users section (tabbed). |
| Register Teacher | /registerTeacher.html | ADMIN | Form with name, username, password. Checkboxes for subject and division selection. |
| Analytics Report | /admin-report.html | ADMIN | Division bar chart, Subject bar chart, Student ranking table. |

---

## Future Scope

1. **Email notifications** when a query is resolved (replace console print in NotificationService)
2. **BCrypt password hashing** — replace NoOpPasswordEncoder for production security
3. **Pagination** on marks and query tables for large datasets
4. **Export to PDF/Excel** for admin performance reports
5. **Student marks comparison** — compare student with division average
6. **Push notifications** using WebSocket instead of basic TCP socket
7. **Audit log** — track who changed which marks and when
8. **Multiple subjects per question** — currently one subject per marks entry session
9. **Mobile-responsive UI** — current CSS is desktop-first

---

## SDG Alignment

This project aligns with **SDG 4 — Quality Education**. It digitizes the academic review process in educational institutions, making marks review transparent, accessible, and traceable. Students get fair access to query their marks; teachers maintain structured records; admins track performance to identify weak divisions and improve teaching focus.
