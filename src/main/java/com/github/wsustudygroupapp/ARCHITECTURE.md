# Java Package Architecture

A quick reference for every package in this project — what it does, what lives there, and why.

---

## Request Flow

Every HTTP request travels through the app in this order:

```
HTTP Request → Filter → Controller → Service → Repository → Database
                                         ↑
                                   Logic lives here
```

WebSocket messages (chat) follow a different path:

```
WebSocket Message → ChatController → ChatService → MessageRepository → Database
                         ↓
               broadcast to all group members
```

---

## `model/`

**What it is:** Java classes that map directly to database tables.

Each field in the class = a column in the table. Hibernate reads these classes on startup and creates (or updates) the tables automatically. You never write `CREATE TABLE` SQL by hand.

| File | Table | Owner | Purpose |
|------|-------|-------|---------|
| `User.java` | `user_table` | Jose | Login credentials (email, hashed password, verification token) |
| `Profile.java` | `profile_table` | Maicheal | Student info (name, major, year, bio) |
| `Course.java` | `course_table` | Maicheal | Pre-seeded WSU course catalog (~500 courses) |
| `UserCourse.java` | `user_course_table` | Maicheal | Links a student to a course with their section + semester |
| `StudyGroup.java` | `study_group_table` | Hayden | A study group tied to a course, has many member profiles |
| `Message.java` | `message_table` | Brian | A chat message sent in a study group |

> `User` and `Profile` are intentionally separate. `User` handles auth only. `Profile` handles everything the student sees and edits in the app.

---

## `repository/`

**What it is:** The layer that talks to the database.

Each repository is an interface that extends `JpaRepository`. Spring generates the SQL automatically based on method names — you rarely write raw queries.

```java
// This method name alone generates: SELECT * FROM message_table WHERE study_group_id = ? ORDER BY sent_at ASC
List<Message> findByStudyGroupIdOrderBySentAtAsc(Long studyGroupId);
```

| File | Talks to | Owner | Key methods |
|------|----------|-------|-------------|
| `UserRepository.java` | `user_table` | Jose | `findByEmail`, `findByVerificationToken`, `existsByEmail` |
| `ProfileRepository.java` | `profile_table` | Maicheal | `findByUserId` |
| `CourseRepository.java` | `course_table` | Maicheal | `findByDepartmentCode`, `findByCourseCode`, `findByCourseNameContainingIgnoreCase` |
| `UserCourseRepository.java` | `user_course_table` | Maicheal | `findByProfileId`, `findClassmates` (custom query) |
| `StudyGroupRepository.java` | `study_group_table` | Hayden | `findByCourseId`, `findByMembersId`, `findByCreatedById` |
| `MessageRepository.java` | `message_table` | Brian | `findByStudyGroupIdOrderBySentAtAsc` |

> `findClassmates` in `UserCourseRepository` is the core matching query — it finds all students in the same course, section, and semester.

---

## `dto/`

**What it is:** Simple classes that define the shape of data coming in (requests) and going out (responses).

DTOs keep your entity classes out of the API layer. You never send a raw `User` or `Profile` entity directly to the frontend — you shape the data first.

**Request DTOs** use `@Data` (Lombok) because Jackson needs to set fields one by one when deserializing JSON.
**Response DTOs** can be records since they're just created and returned.

| File | Direction | Owner | Used for |
|------|-----------|-------|----------|
| `RegisterRequest.java` | Frontend → Backend | Jose | Body of `POST /auth/register` |
| `LoginRequest.java` | Frontend → Backend | Jose | Body of `POST /auth/login` |
| `AuthResponse.java` | Backend → Frontend | Jose | Response from login — contains the JWT token |
| `ProfileRequest.java` | Frontend → Backend | Maicheal | Body of `POST /profile` and `PUT /profile` |
| `CourseEnrollRequest.java` | Frontend → Backend | Maicheal | Body of `POST /courses/enroll` |
| `StudyGroupRequest.java` | Frontend → Backend | Hayden | Body of `POST /groups` |
| `MessageDTO.java` | Both directions | Brian | Sent over WebSocket — contains groupId, senderName, content, sentAt |

---

## `config/`

**What it is:** Instructions that tell Spring Boot how to set up and wire the app before it starts accepting requests.

Think of `config/` like the settings panel of the app. It doesn't do work — it tells Spring *how* to do the work. You write a config class once and Spring reads it on startup and applies the rules everywhere automatically.

A config class has two key annotations:
- `@Configuration` — tells Spring "read this class for setup instructions"
- `@Bean` — tells Spring "create this object and manage it for me, I'll need it elsewhere"

This pattern is called **dependency injection** — instead of creating objects yourself with `new`, you let Spring manage them and inject them wherever they're needed.

| File | Owner | Purpose |
|------|-------|---------|
| `SecurityConfig.java` | Jose | Defines public vs protected routes, disables CSRF, sets stateless JWT sessions, provides the BCrypt password hasher |
| `WebSocketConfig.java` | Brian | Sets up the STOMP WebSocket broker — frontend connects to `/ws`, sends to `/app/chat/{groupId}`, receives from `/topic/chat/{groupId}` |

> Nothing in `config/` contains business logic. If you find yourself writing if/else in a config class, it belongs in a service instead.

---

## `filter/`

**What it is:** Code that runs on every HTTP request before it reaches a controller.

| File | Owner | Purpose |
|------|-------|---------|
| `JwtAuthFilter.java` | Jose | Reads the `Authorization: Bearer <token>` header, validates the JWT, and tells Spring Security who the user is |

> If the token is missing or invalid, the request is rejected here — it never reaches the controller.

---

## `util/`

**What it is:** Reusable helper classes that don't belong to any specific layer.

| File | Owner | Purpose |
|------|-------|---------|
| `JwtUtil.java` | Jose | Generates JWT tokens on login, validates tokens on each request, extracts the user's email from a token |

---

## `service/`

**What it is:** Where all the business logic lives.

Services sit between controllers and repositories. They validate input, enforce rules, call repositories, and build responses. Controllers call services — services never call controllers.

| File | Owner | Purpose |
|------|-------|---------|
| `AuthService.java` | Jose | Validates `@westfield.ma.edu` domain, hashes password, sends verification email, handles login |
| `UserDetailsServiceImpl.java` | Jose | Loads a user from the DB by email — required by Spring Security for JWT validation |
| `ProfileService.java` | Maicheal | Get, create, and update a student's profile |
| `CourseService.java` | Maicheal | Enroll/drop courses, get classmates in the same section |
| `StudyGroupService.java` | Hayden | Create groups, join/leave groups, list groups by course |
| `ChatService.java` | Brian | Save incoming messages to the DB, load chat history when a group is opened |

---

## `controller/`

**What it is:** The entry point for HTTP requests. Maps URLs to Java methods.

Controllers are thin — they receive a request, call a service, and return a response. No logic lives here. `ChatController` is the exception — it uses `@MessageMapping` instead of `@GetMapping`/`@PostMapping` because it handles WebSocket messages, not HTTP.

| File | Base URL | Owner | Handles |
|------|----------|-------|---------|
| `AuthController.java` | `/auth` | Jose | `POST /register`, `GET /verify`, `POST /login` |
| `ProfileController.java` | `/profile` | Maicheal | `GET`, `POST`, `PUT` profile |
| `CourseController.java` | `/courses` | Maicheal | List courses, enroll, drop, get classmates |
| `StudyGroupController.java` | `/groups` | Hayden | List, create, join, leave groups + get chat history |
| `ChatController.java` | WebSocket | Brian | Receives messages at `/app/chat/{groupId}`, broadcasts to `/topic/chat/{groupId}` |

---

## `WsuStudyGroupAppApplication.java`

The entry point of the entire application. Contains the `main` method that boots Spring. You will never need to touch this file.
