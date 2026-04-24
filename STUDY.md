# STUDY.md — Viva Preparation Guide
## Paper Review System | EAD Mini Project

---

# SECTION 1: CONCEPTS USED IN THIS PROJECT

---

## 1. Spring Boot

### What it is
Spring Boot is a framework built on top of the Spring Framework that removes the need for manual XML configuration. It uses "convention over configuration" — you write less setup code and more business logic.

### Why used in this project
- We needed to build a web application quickly with REST APIs, security, and database access
- Spring Boot auto-configures everything: embedded Tomcat server, JPA, Security
- No need to deploy to a separate Tomcat — the app starts with `mvn spring-boot:run`

### Where used in this project

| Component | Class | Role |
|---|---|---|
| Main app | PaperreviewApplication.java | Entry point, enables @Async and @Scheduled |
| Config | SecurityConfig.java | Configures security rules |
| REST APIs | MarksController, QueryController, etc. | Handles HTTP requests |
| Service layer | MarksService, QueryService, etc. | Business logic |

**Key annotations:**
- `@SpringBootApplication` = @Configuration + @EnableAutoConfiguration + @ComponentScan
- `@EnableAsync` = allows @Async methods to run on separate threads
- `@EnableScheduling` = allows @Scheduled methods to run on timer

---

## 2. Spring Security

### What it is
Spring Security is a module that handles authentication (who are you?) and authorization (what can you do?).

### Authentication flow in this project

```
User submits username + password at /login
         |
Spring Security intercepts the POST /login request
         |
Calls CustomUserDetailsService.loadUserByUsername(username)
         |
loadUserByUsername() searches the users table
(uses equalsIgnoreCase for safe case-insensitive match)
         |
Returns a UserDetails object with username, password, role
         |
Spring compares the submitted password with DB password
(uses NoOpPasswordEncoder — plain text for demo)
         |
If match → creates a SecurityContext (session) for the user
         |
successHandler reads the role → redirects to correct dashboard
```

### Role-based access in this project

In `SecurityConfig.java`:
```java
.requestMatchers("/student.html", "/query.html", "/view/marks")
    .hasAuthority("STUDENT")

.requestMatchers("/teacher.html")
    .hasAuthority("TEACHER")

.requestMatchers("/admin.html", "/admin-report.html")
    .hasAuthority("ADMIN")
```

When a STUDENT tries to access `/teacher.html` → Spring Security blocks it (403 Forbidden) before the request ever reaches the controller.

### How login internally works
1. `DaoAuthenticationProvider` is configured with our `CustomUserDetailsService`
2. Spring Security calls `loadUserByUsername()` during every login attempt
3. The returned `UserDetails` contains granted authorities (roles)
4. Spring stores this in the `SecurityContextHolder` (server-side session)
5. Every subsequent request checks this context — no re-authentication needed

### How we get logged-in user in controller
```java
@GetMapping("/view/marks")
public String viewMyMarks(Authentication authentication, Model model) {
    String username = authentication.getName(); // from SecurityContext
    User user = userRepository.findByUsername(username);
    String prn = user.getPrn(); // link to student table
}
```

---

## 3. JPA / Hibernate (ORM)

### What ORM means
ORM = Object-Relational Mapping. It lets you work with Java objects instead of writing raw SQL. Hibernate (the JPA implementation) automatically converts Java class operations to SQL queries.

### Entity mapping in this project

`@Entity` tells Hibernate to create a database table for this class.

```java
@Entity
@Table(name = "student")
public class Student {
    @Id
    private String prn;           // PRIMARY KEY
    private String name;
    private String division;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Marks> marksList; // One student → many marks
}
```

### Relationship types used

| Relationship | Where Used | Meaning |
|---|---|---|
| @OneToMany | Student → Marks | One student has many marks records |
| @OneToMany | Subject → Marks | One subject appears in many marks records |
| @ManyToOne | Marks → Student | Many marks belong to one student |
| @ManyToOne | Marks → Subject | Many marks belong to one subject |

### Important: insertable=false, updatable=false
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "prn", insertable = false, updatable = false)
private Student student;
```
This tells Hibernate: "Read the student from the prn column, but do NOT write to the prn column using this relationship — the plain `String prn` field handles that." Without this, Hibernate throws a "Repeated column" error.

### Spring Data JPA Repositories
```java
public interface MarksRepository extends JpaRepository<Marks, Integer> {
    List<Marks> findByPrn(String prn); // Auto-generates SQL
}
```
Spring Data generates: `SELECT * FROM marks WHERE prn = ?` — no SQL needed.

Custom JPQL query:
```java
@Query("SELECT m FROM Marks m WHERE m.prn = :prn AND m.subjectId = :subjectId AND m.question = :question")
Optional<Marks> findExact(...);
```
JPQL uses class/field names (Java), not table/column names (SQL).

### Why ddl-auto=update
`spring.jpa.hibernate.ddl-auto=update` tells Hibernate to automatically:
- Create tables that don't exist yet
- Add new columns when entity fields are added
- Never drop existing data

---

## 4. MySQL

### Role in this project
MySQL is the relational database. It stores all persistent data. The database name is `paper_system`.

### Tables and relationships

```sql
-- users table: all login accounts
users(id, username, password, role, prn)

-- student table: academic data
student(prn PK, name, division)

-- teacher table: teacher profiles with assignments
teacher(id PK AUTO_INCREMENT, name, username UNIQUE, subject_ids, divisions)

-- subject table: master list of subjects
subject(id PK, name)

-- marks table: all student marks (with unique constraint)
marks(id PK AUTO_INCREMENT, prn FK→student, subject_id FK→subject, 
      question, marks_obtained, max_marks)
-- UNIQUE KEY(prn, subject_id, question) prevents duplicate rows

-- query table: student queries
query(id PK AUTO_INCREMENT, prn, subject_id, question, message, status)
```

---

## 5. MVC Architecture

### What MVC means
MVC = Model-View-Controller. It separates the application into three parts:
- **Model** = Data (entities, repositories) 
- **View** = UI (HTML pages, Thymeleaf templates)
- **Controller** = Logic connector between model and view

### How MVC works in this project

```
MODEL:
  Entity classes → Marks.java, Student.java, Teacher.java, etc.
  Repositories → MarksRepository, StudentRepository, etc.
  Services → MarksService, QueryService, etc.

VIEW:
  Static HTML → admin.html, teacher.html, student.html, etc.
  Thymeleaf → student-marks.html (only server-rendered page)

CONTROLLER:
  @RestController → Returns JSON for AJAX calls from static HTML
  @Controller → Returns Thymeleaf template name for /view/marks
```

---

## 6. Socket Programming (CO4)

### What is a socket?
A socket is an endpoint for communication between two machines (or two processes on the same machine) over a network using TCP/IP protocol. Java's `Socket` class is the client; `ServerSocket` is the server.

### Why socket is used in this project
When a teacher resolves a student's query, an alert message is sent in real-time to the socket server. This demonstrates the CO4 requirement: real-time communication using Java Socket API.

### How it works in this project

**Server side (QueryAlertServer.java):**
```java
ServerSocket serverSocket = new ServerSocket(9090);  // binds to port 9090
while (true) {
    Socket clientSocket = serverSocket.accept();       // waits for connection
    Thread t = new Thread(() -> handleClient(clientSocket));
    t.start(); // handle each client on its own thread
}

private void handleClient(Socket clientSocket) {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
    String message = reader.readLine();  // read message from client
    System.out.println("[SOCKET ALERT] " + message);
    clientSocket.close();
}
```

**Client side (QueryAlertClient.java):**
```java
@Async  // runs on background thread — HTTP response not delayed
public void sendResolvedAlert(String prn, String question) {
    Socket socket = new Socket("localhost", 9090);  // connect to server
    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    writer.println("QUERY RESOLVED | PRN: " + prn + " | Question: " + question);
    socket.close();
}
```

**When does this happen?**
`QueryService.resolveQuery()` → calls `queryAlertClient.sendResolvedAlert()` → opens socket connection → sends message → `QueryAlertServer.handleClient()` receives and prints the alert.

---

## 7. Multithreading

### Thread-per-client model in socket server
```java
// QueryAlertServer.java
Socket clientSocket = serverSocket.accept();
Thread clientThread = new Thread(() -> handleClient(clientSocket));
clientThread.setDaemon(true);
clientThread.setName("socket-client-" + clientSocket.getPort());
clientThread.start();
// Immediately loops back to accept() — ready for next connection
```

**Why daemon thread?** A daemon thread automatically stops when the main JVM exits. No need to explicitly shut it down.

**Why thread-per-client?** If we handled one client at a time in the main loop, a slow client would block all other clients. Each client on its own thread means they run independently.

### Background server start
```java
// QueryAlertServer implements ApplicationRunner
// Spring Boot calls run() after the app fully starts
public void run(ApplicationArguments args) {
    Thread serverThread = new Thread(this::startListening);
    serverThread.setDaemon(true);
    serverThread.start(); // non-blocking — Spring Boot continues
}
```

---

## 8. @Async — Asynchronous Multithreading

### Purpose
`@Async` makes a method run on a separate thread from Spring's thread pool. The caller does not wait for the method to finish.

### Where used in this project

**NotificationService.java:**
```java
@Async
public void sendResolutionAlert(String prn, String question) {
    // Simulates sending email/SMS — takes 2 seconds
    Thread.sleep(2000);
    System.out.println("Notification sent to PRN: " + prn);
}
```

**QueryAlertClient.java:**
```java
@Async
public void sendResolvedAlert(String prn, String question) {
    // Opens socket connection — could be slow
    Socket socket = new Socket("localhost", 9090);
    // ...
}
```

**Without @Async:** The teacher's browser would wait 2+ seconds for the HTTP response while the notification completes.
**With @Async:** The HTTP response returns immediately. Notification runs in background.

**Requirement:** `@EnableAsync` must be on the main application class.

---

## 9. @Transactional

### Purpose
`@Transactional` ensures that ALL database operations within a method either ALL succeed or ALL are rolled back together.

### Where used
```java
// QueryService.java
@Transactional
public Query resolveQuery(Long id) {
    Query q = queryRepository.findById(id).orElseThrow(...);
    q.setStatus("Resolved");
    queryRepository.save(q);       // DB operation 1

    notificationService.sendResolutionAlert(...); // @Async — separate thread
    queryAlertClient.sendResolvedAlert(...);       // @Async — separate thread

    return q;
}
```

If `queryRepository.save(q)` throws an exception, the entire method rolls back — the status is NOT changed. This ensures data consistency.

---

## 10. @Scheduled — Background Scheduled Task

### Purpose
Runs a method automatically at fixed time intervals without any HTTP request triggering it.

### Where used
```java
// QueryService.java
@Scheduled(fixedRate = 60000) // runs every 60 seconds
public void checkPendingQueries() {
    long count = queryRepository.countByStatus("Pending");
    System.out.println("[SCHEDULER] Pending queries: " + count);
}
```

**Requirement:** `@EnableScheduling` must be on the main application class.

---

## 11. Validation Annotations

### Where used
```java
// User.java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50, message = "Username must be 3-50 characters")
private String username;

// Query.java
@NotBlank(message = "Message cannot be empty")
@Size(min = 5, max = 500, message = "Message must be 5-500 characters")
private String message;

// Marks.java
@Min(value = 0, message = "Marks cannot be negative")
@Max(value = 100, message = "Marks cannot exceed 100")
private int marksObtained;
```

**Why needed:** Without validation annotations, null or blank values could reach the database and cause data corruption or runtime errors.

---

## 12. Thymeleaf (CO2 — View Layer)

### What it is
Thymeleaf is a server-side template engine. Instead of JavaScript fetching data after the page loads, the server fills in the data BEFORE sending the HTML to the browser.

### Where used in this project
Only one page: `student-marks.html` served by `StudentViewController.java`.

### How it works
```java
// StudentViewController.java
@Controller  // NOT @RestController
public class StudentViewController {
    @GetMapping("/view/marks")
    public String viewMyMarks(Authentication authentication, Model model) {
        // Model is like a HashMap — put data into it
        model.addAttribute("marksList", marksList);
        model.addAttribute("prn", prn);
        model.addAttribute("subjects", subjects);
        return "student-marks"; // Spring finds templates/student-marks.html
    }
}
```

```html
<!-- student-marks.html Thymeleaf syntax -->
<tr th:each="m : ${marksList}">               <!-- for-each loop -->
    <td th:text="${m.question}">Q1</td>        <!-- outputs value -->
    <td th:classappend="${m.marksObtained >= 7} ? 'high' : 'low'">  <!-- conditional CSS -->
        <span th:text="${m.marksObtained}">0</span>
    </td>
</tr>
```

**Key difference from static HTML + JS:**
- Static HTML: browser loads blank page → JS calls API → JS builds table → table appears
- Thymeleaf: server builds complete page with data → sends finished HTML → table appears immediately

---

## 13. JDBC and PreparedStatement

### Where used
`JdbcMarksDao.java` — demonstrates raw JDBC alongside JPA (CO1 requirement).

### PreparedStatement (Single Insert)
```java
String sql = "INSERT INTO marks (prn, subject_id, question, marks_obtained, max_marks) VALUES (?, ?, ?, ?, ?)";
PreparedStatement ps = con.prepareStatement(sql);
ps.setString(1, m.getPrn());
ps.setInt(2, m.getSubjectId());
// ...
ps.executeUpdate();
```

### Batch Insert
```java
for (Marks m : list) {
    ps.setString(1, m.getPrn());
    // ...
    ps.addBatch();     // add to batch queue
}
ps.executeBatch();     // send all at once to DB — more efficient
```

**Why PreparedStatement?** Prevents SQL injection — parameters are sent separately from the SQL query structure.
**Why Batch?** More efficient — reduces round-trips to the database.

## 🔹 JDBC Flow in This Project

The project uses a DAO class (JdbcMarksDao) to demonstrate JDBC.

Flow:
Controller → Service → DAO → PreparedStatement → Database

Steps:
1. Controller receives request (/jdbc/marks/batch)
2. Calls JdbcMarksService
3. Service calls JdbcMarksDao.batchInsert()
4. DAO prepares SQL using PreparedStatement
5. Multiple records added using addBatch()
6. executeBatch() sends all queries at once

This improves performance.

---

## 🔹 Duplicate Query Prevention Logic

Before inserting a query:
- System checks PRN + subjectId + question

If a query exists with status "Pending":
→ New query is rejected

Implemented in:
- QueryController
- QueryRepository

---

## 🔹 Teacher Restriction Logic (IMPORTANT)

Teachers are restricted based on assigned:
- Subjects
- Divisions

Stored in database:
- subject_ids (e.g. "1,3")
- divisions (e.g. "A,B")

Applied in:
- Marks Entry
- Update Marks
- Resolve Queries

Backend validation ensures:
- Teacher cannot modify data outside assigned scope

Frontend:
- Dropdowns are filtered

---

## 🔹 Why Both Hibernate and JDBC Are Used

Hibernate:
- Used for normal operations
- Easy to use
- ORM-based

JDBC:
- Used for batch insert
- Faster for bulk operations
- Shows low-level DB interaction

Conclusion:
Hibernate = ease  
JDBC = performance + learning

---

# SECTION 2: IMPORTANT VIVA QUESTIONS & ANSWERS

**Q1: What is Spring Boot? Why did you use it?**
A: Spring Boot is a framework that auto-configures a Spring application. I used it because it eliminates boilerplate setup code, provides embedded Tomcat so no separate server deployment is needed, and simplifies database integration through Spring Data JPA.

**Q2: How does login work in your system?**
A: The browser posts username and password to `/login`. Spring Security calls `CustomUserDetailsService.loadUserByUsername()` which fetches the user from the database. The password is compared. If correct, Spring creates a security session and the `successHandler` redirects based on the user's role (ADMIN→/admin.html, TEACHER→/teacher.html, STUDENT→/student.html).

**Q3: What is the difference between @RestController and @Controller?**
A: `@RestController` automatically converts return values to JSON using Jackson. It combines `@Controller` and `@ResponseBody`. `@Controller` returns a view name (string) which Spring resolves to a Thymeleaf template. I used `@RestController` for all API endpoints and `@Controller` only for the Thymeleaf marks page (`StudentViewController`).

**Q4: What is ORM? How does Hibernate map your classes to tables?**
A: ORM (Object-Relational Mapping) converts Java objects to database rows and vice versa automatically. Hibernate reads `@Entity` annotations to identify which classes are tables, `@Id` for primary keys, `@Column` for column names, and `@ManyToOne`/`@OneToMany` for foreign key relationships. It then auto-generates SQL — I write Java code and Hibernate handles the SQL.

**Q5: What is @Transactional? Where did you use it?**
A: `@Transactional` wraps a method in a database transaction. If any operation inside the method fails, all database changes are rolled back. I used it in `QueryService.resolveQuery()` — it updates the query status to "Resolved". If the save fails, the status doesn't get partially saved.

**Q6: Explain your Socket programming implementation.**
A: When a teacher resolves a student's query, `QueryAlertClient.sendResolvedAlert()` (annotated with `@Async`) opens a TCP `Socket` connection to `localhost:9090`. It sends a formatted message using `PrintWriter`. The `QueryAlertServer` runs in the background (started via `ApplicationRunner`). It uses `ServerSocket.accept()` in a loop. Each accepted connection is handed to a new `Thread` (thread-per-client pattern) so multiple alerts can be processed simultaneously.

**Q7: What is @Async and why did you use it?**
A: `@Async` makes a method run on a separate thread from Spring's thread pool. The caller doesn't wait. I used it on `sendResolutionAlert()` (which sleeps 2 seconds simulating email) and `sendResolvedAlert()` (socket connection). Without @Async, the teacher's browser would wait while these complete. With @Async, the HTTP response returns immediately.

**Q8: What is the upsert logic and why did you implement it?**
A: Upsert = Insert if not exists, Update if exists. When a teacher enters marks for a student question that already has marks in the database, the old system always inserted a new row — causing duplicate entries. My fix: `MarksService.upsertMarks()` first calls `MarksRepository.findExact(prn, subjectId, question)` using a JPQL query. If a row is found, it updates that row's marks. If not, it creates a new row. I also added a UNIQUE KEY constraint in MySQL as a second layer of protection.

**Q9: How does teacher access restriction work?**
A: The `Teacher` entity has `subjectIds` (e.g., "1,3") and `divisions` (e.g., "A,B") stored as comma-separated strings. The `isAllowedSubject(int id)` and `isAllowedDivision(String div)` helper methods parse these strings and check if the given value is in the list. In `MarksController.addMarks()`, if the logged-in user is a TEACHER, we fetch their Teacher record and call these methods. If the check fails, we return HTTP 403. The frontend also disables dropdown options using the `/teacher/my-info` API response.

**Q10: What is @OneToMany and @ManyToOne? How did you handle the repeated column issue?**
A: `@OneToMany` means one entity (e.g., Student) links to multiple instances of another (Marks). `@ManyToOne` is the inverse. The repeated column issue occurs when both a plain field (`String prn`) and a `@ManyToOne` relationship use the same column. The fix is `insertable=false, updatable=false` on the `@JoinColumn` annotation — it tells Hibernate: "Use this relationship for reading only; the plain field handles writing."

**Q11: What is Thymeleaf? How is it different from plain HTML?**
A: Thymeleaf is a server-side template engine. In plain HTML + JS, the browser loads the HTML first, then JavaScript calls APIs to fetch data and builds the page. With Thymeleaf, the Spring controller puts data into a Model object, Thymeleaf reads the Model and fills in `th:text`, `th:each`, `th:if` attributes, and sends a complete HTML page to the browser. No client-side JS fetch calls needed for the data.

**Q12: What does ddl-auto=update do?**
A: Hibernate compares the entity classes with the actual database schema on every startup. If a class has a new field that doesn't exist as a column, Hibernate ALTERs the table to add it. If a table doesn't exist for an entity, Hibernate CREATEs it. It never drops existing tables or removes data.

**Q13: What is Spring Data JPA? How does findByPrn() work?**
A: Spring Data JPA generates SQL queries from method names in repository interfaces. `findByPrn(String prn)` automatically generates `SELECT * FROM marks WHERE prn = ?`. No SQL writing needed. For complex queries, I used `@Query` with JPQL (Java Persistence Query Language which uses class names, not table names).

**Q14: Why is NoOpPasswordEncoder used?**
A: `NoOpPasswordEncoder` stores passwords as plain text — no encryption. This is intentional for a demo/project environment where simplicity is more important than production security. In a real system, `BCryptPasswordEncoder` would be used, which hashes passwords using the BCrypt algorithm.

---

# SECTION 3: COMMON ERRORS AND FIXES

| Error | Cause | Fix |
|---|---|---|
| `User not found` on login | Case mismatch (e.g., "Admin" vs "admin") | `CustomUserDetailsService` uses `equalsIgnoreCase()` — already fixed |
| `Repeated column in mapping for entity` | Both plain field and @ManyToOne use same column | Add `insertable=false, updatable=false` on @JoinColumn |
| Duplicate marks rows | Old code always INSERT | Use `upsertMarks()` in MarksService which checks `findExact()` first |
| 403 on API call | SecurityConfig doesn't allow that URL for that role | Add the URL to the correct `.requestMatchers()` block |
| `SocketException: Connection refused` | QueryAlertServer not started | App starts it automatically via ApplicationRunner — check startup logs |
| Thymeleaf NullPointerException | `m.subject` is null when subject FK doesn't match | Use `${m.subject != null} ? ${m.subject.name} : ('Subject ' + ${m.subjectId})` |
| Teacher table not having columns | Old teacher table with just id/name/subject | Restart app — Hibernate adds `username`, `subject_ids`, `divisions` via ddl-auto=update |
| `@Async` not working (runs synchronously) | `@EnableAsync` missing on main class | Add `@EnableAsync` to `PaperreviewApplication` — already present |
| `@Scheduled` not running | `@EnableScheduling` missing | Add `@EnableScheduling` to main class — already present |
