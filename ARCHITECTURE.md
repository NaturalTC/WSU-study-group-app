# WSU Study Group App — Architecture Overview

This document describes the full-stack architecture of the WSU Study Group App: how the backend and frontend are structured, how they communicate, and the key design decisions behind them.

---

## Table of Contents

1. [High-Level Overview](#high-level-overview)
2. [Backend — Spring Boot](#backend--spring-boot)
   - [Package Structure](#package-structure)
   - [Models & Database](#models--database)
   - [REST API Endpoints](#rest-api-endpoints)
   - [WebSocket & Real-Time Chat](#websocket--real-time-chat)
   - [Authentication & Security](#authentication--security)
   - [Exception Handling](#exception-handling)
3. [Frontend — React + Vite](#frontend--react--vite)
   - [Directory Structure](#directory-structure)
   - [Pages](#pages)
   - [Components](#components)
   - [Auth Context](#auth-context)
   - [API Layer](#api-layer)
   - [Routing](#routing)
   - [WebSocket Integration](#websocket-integration)
4. [How Frontend & Backend Connect](#how-frontend--backend-connect)
   - [Full Auth Flow](#full-auth-flow)
   - [Full Chat Flow](#full-chat-flow)
5. [Stubs & Upcoming Features](#stubs--upcoming-features)

---

## High-Level Overview

The app is split into two independent processes that run side by side:

| Layer | Technology | Port |
|---|---|---|
| Backend | Java 17 + Spring Boot 4 | `8080` |
| Frontend | React 19 + Vite | `5173` |
| Database | MySQL (Docker) | `3306` |

The frontend is a Single Page Application (SPA) that runs entirely in the browser. It communicates with the backend in two ways:

- **REST over HTTP** — for loading data (groups, profiles, messages history, courses)
- **WebSocket over STOMP** — for real-time group chat (sending and receiving messages instantly)

Every request to a protected endpoint must include a **JSON Web Token (JWT)** in the `Authorization: Bearer <token>` header. The frontend stores this token in `localStorage` and attaches it automatically to every request via an Axios interceptor.

---

## Backend — Spring Boot

### Package Structure

All backend source lives under:
```
src/main/java/com/github/wsustudygroupapp/
```

| Package | Responsibility |
|---|---|
| `config/` | Spring Security rules, WebSocket/STOMP setup |
| `controller/` | HTTP endpoints and WebSocket message handlers |
| `service/` | Business logic — sits between controllers and repositories |
| `repository/` | Database queries (Spring Data JPA interfaces) |
| `model/` | JPA entity classes that map to database tables |
| `dto/` | Data Transfer Objects — shapes of data sent to/from the client |
| `filter/` | HTTP request filters (JWT validation runs here) |
| `util/` | JWT generation and validation helpers |
| `exception/` | Custom exceptions and global error handler |

---

### Models & Database

The database is MySQL, managed by Hibernate JPA. The schema is auto-created on startup (`ddl-auto=update`) and seeded with courses and badges from `data.sql`.

#### Entities

**`User`** — Authentication account only. Holds the email, bcrypt-hashed password, and email verification state. Intentionally kept separate from student data.

**`Profile`** — Student-facing identity. Holds display name, major, academic year, bio, and points. Linked one-to-one with `User`.

**`Course`** — A course in the WSU catalog. Holds department code, course code, and course name. Pre-seeded at startup.

**`UserCourse`** — Join table linking a `Profile` to a `Course` with section and semester. This is what enables classmate matching — two students in the same course+section+semester are classmates.

**`StudyGroup`** — A group tied to a `Course`. Has a name, a creator (Profile), and a list of members (Profile). Members use `FetchType.EAGER` so Hibernate loads them immediately alongside the group.

**`Message`** — A single chat message. Holds the text content, the sender (Profile), the group (StudyGroup), and a `sentAt` timestamp. Persisted to the database so chat history survives reconnects.

**`MeetingSession`** — A scheduled study session. Linked to a StudyGroup, with location, date/time, and optional notes.

**`Badge`** — An achievement badge with a name, icon, and point value. Pre-seeded.

**`UserBadge`** — Records that a student earned a specific badge.

**`Notification`** — An in-app notification for a user, with a read/unread flag.

#### Key Relationships

```
User ──1:1──► Profile
Profile ──M:M──► Course  (through UserCourse, with section + semester)
Profile ──M:M──► StudyGroup  (members list, through study_group_members)
StudyGroup ──1:M──► Message
StudyGroup ──1:M──► MeetingSession
Profile ──M:M──► Badge  (through UserBadge)
```

---

### REST API Endpoints

All REST endpoints are prefixed with `/` and require a `Bearer` JWT token unless listed as public.

#### Authentication — `/auth` (all public)

| Method | Path | What it does |
|---|---|---|
| `POST` | `/auth/register` | Create account, send verification email |
| `GET` | `/auth/verify?token=` | Verify email address, mark account as active |
| `POST` | `/auth/login` | Validate credentials, return JWT |
| `POST` | `/auth/resend-verification` | Re-send the verification email |
| `POST` | `/auth/forgot-password` | Send password reset link to email |
| `POST` | `/auth/change-password` | Set new password via reset token |
| `POST` | `/auth/update-password` | Change password when already logged in |

#### Profiles — `/profiles` (protected)

| Method | Path | What it does |
|---|---|---|
| `GET` | `/profiles` | Load the logged-in user's profile |
| `POST` | `/profiles` | Create a profile (first-time setup) |
| `PUT` | `/profiles` | Update profile fields |

#### Study Groups — `/groups` (protected)

| Method | Path | What it does |
|---|---|---|
| `GET` | `/groups/{groupId}` | Get a single group with members |
| `GET` | `/groups/course/{courseId}` | List all groups for a course |
| `GET` | `/groups/my` | List all groups the logged-in user has joined |
| `POST` | `/groups` | Create a new study group |
| `POST` | `/groups/{groupId}/join` | Join a group |
| `DELETE` | `/groups/{groupId}/leave` | Leave a group |
| `GET` | `/groups/{groupId}/messages` | Load full chat history for a group |

#### Courses — `/courses` (protected)

| Method | Path | What it does |
|---|---|---|
| `GET` | `/courses` | Get the full course catalog |
| `GET` | `/courses/my` | Get courses the logged-in user is enrolled in |
| `POST` | `/courses/enroll` | Enroll in a course (section + semester) |
| `DELETE` | `/courses/{userCourseId}` | Drop a course |
| `GET` | `/courses/{userCourseId}/classmates` | Find classmates in same section |
| `GET` | `/courses/search?q=` | Search courses by name |
| `GET` | `/courses/department/{code}` | Filter courses by department code |

#### Meetings — `/meetings` (protected)

| Method | Path | What it does |
|---|---|---|
| `POST` | `/meetings` | Schedule a session, notify group members |
| `GET` | `/meetings/upcoming` | Get upcoming sessions for logged-in user |
| `GET` | `/meetings/group/{groupId}` | Get sessions for a specific group |
| `DELETE` | `/meetings/{sessionId}` | Cancel a session |

#### Leaderboard, AI, Notifications (stubs — Sprint 2)

These controllers exist but return stub data. They are wired up and ready to implement.

---

### WebSocket & Real-Time Chat

The real-time chat does not use normal HTTP requests. Instead it uses **WebSocket**, a persistent two-way connection between the browser and the server.

On top of the raw WebSocket connection the app uses **STOMP** (Simple Text Oriented Messaging Protocol) — a lightweight messaging layer that adds concepts like destinations, subscriptions, and topics. Spring Boot has first-class support for STOMP via `@MessageMapping` and `@SendTo`.

#### Server-Side Setup (`WebSocketConfig.java`)

```
WebSocket endpoint:  /ws           (browser connects here)
App destination:     /app          (client sends messages here)
Message broker:      /topic        (server broadcasts to here)
SockJS fallback:     enabled       (allows HTTP polling if WebSocket is blocked)
```

#### How a message travels

```
[Browser]
  User types a message and hits Send
        │
        ▼
  Publish to /app/chat/{groupId}
  with body: { studyGroupId, senderName, content }
        │
        ▼
[Spring Boot — ChatController.sendMessage()]
  1. Receives the MessageDTO
  2. Loads the StudyGroup and sender Profile from DB
  3. Saves a Message entity to the database (persistence)
  4. Returns a MessageDTO annotated with @SendTo("/topic/chat/{groupId}")
        │
        ▼
  Spring broadcasts the MessageDTO to /topic/chat/{groupId}
        │
        ▼
[All connected browsers subscribed to /topic/chat/{groupId}]
  Each client receives the broadcast and renders the new message
```

Because every message is saved before broadcasting, chat history is fully persistent. When a user opens a group chat they first load history via `GET /groups/{groupId}/messages`, then subscribe to the WebSocket topic to receive any new messages going forward.

---

### Authentication & Security

The app uses **stateless JWT authentication** — the server keeps no session. Every request proves its own identity by including a signed token.

#### JWT Flow

1. User logs in → `POST /auth/login` with email + password
2. Backend validates credentials, generates a JWT signed with a secret key
3. JWT contains: subject (email), issued-at, expiration
4. Frontend receives the token and stores it in `localStorage`
5. Every subsequent HTTP request includes `Authorization: Bearer <token>`
6. `JwtAuthFilter` intercepts every request, validates the token, and loads the user into Spring's `SecurityContext`
7. Controllers can call `SecurityContextHolder.getContext().getAuthentication()` to get the current user

#### JwtAuthFilter

Runs before every controller method. It:
- Reads the `Authorization` header
- Strips the `Bearer ` prefix to get the raw token
- Validates the token signature and expiration using `JwtUtil`
- Loads the `UserDetails` from the database by the email in the token
- Sets the authenticated user into `SecurityContext` so Spring Security knows who is making the request

#### SecurityConfig

```
CSRF:           Disabled (stateless API, no cookies)
Sessions:       STATELESS (no server-side session storage)
Public paths:   /auth/**, /swagger-ui/**, /api-docs/**, /ws/**
All other paths: require a valid JWT
CORS:           Allowed from localhost:5173
Password hash:  BCrypt
Filter order:   JwtAuthFilter runs before Spring's default UsernamePasswordAuthenticationFilter
```

`/ws/**` is public because the WebSocket upgrade request is a browser-level request that cannot include an `Authorization` header. The JWT is instead passed in the STOMP `CONNECT` frame headers after the connection is established.

#### Email Verification

New accounts are locked until the user verifies their email:
1. `AuthService.register()` sends an email with a UUID verification token
2. User clicks the link → `GET /auth/verify?token=...`
3. Backend marks `user.isVerified = true` and clears the token
4. Login is blocked (`isVerified` is checked before returning a JWT)

#### User vs Profile Separation

`User` holds only auth data (email, password, verification state). `Profile` holds student data (name, major, year, bio, points). This is intentional — it keeps authentication logic isolated from application logic. A user can exist without a profile (right after email verification, before setup), which is how the app detects first-time users and routes them to the setup page.

---

### Exception Handling

**`GlobalExceptionHandler`** (`@RestControllerAdvice`) catches any uncaught `RuntimeException` thrown anywhere in the service layer and returns a clean JSON response to the client:

```json
{ "message": "Study group not found: 42" }
```

This prevents raw stack traces from reaching the browser. Custom exceptions:

- `ResourceNotFoundException` — thrown when a group, profile, or user doesn't exist; produces a `404` response

---

## Frontend — React + Vite

### Directory Structure

```
frontend/src/
├── api/
│   ├── axios.js           Axios instance + interceptors
│   └── auth.js            Auth API wrapper functions
├── components/            Reusable UI pieces (no routing)
│   ├── AppHeader.jsx      Header for logged-in users
│   ├── Header.jsx         Header for logged-out users (public pages)
│   ├── HeroSection.jsx    Landing page hero block
│   ├── FeaturesSection.jsx Feature cards on landing page
│   ├── Footer.jsx         Site footer
│   ├── LoginForm.jsx      Email + password login form
│   ├── RegisterForm.jsx   Registration form
│   ├── ChatMessage.jsx    Single message bubble in chat
│   ├── MembersSidebar.jsx Members + My Groups tabbed sidebar
│   ├── StudyGroupCard.jsx Card for a single study group
│   └── ProtectedRoute.jsx Route guard wrapper
├── context/
│   └── AuthContext.jsx    Global auth state + login/logout methods
├── pages/                 Full route-level views
│   ├── Home.jsx
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── SetupProfile.jsx
│   ├── Profile.jsx
│   ├── StudyGroups.jsx
│   ├── GroupChatIndex.jsx
│   ├── GroupChat.jsx
│   ├── ChangePassword.jsx
│   ├── ForgotPassword.jsx
│   ├── ResetPassword.jsx
│   ├── VerifyPending.jsx
│   ├── VerifySuccess.jsx
│   └── VerifyError.jsx
├── App.jsx                Route definitions
└── main.jsx               App bootstrap (BrowserRouter + AuthProvider)
```

---

### Pages

| Page | Route | Auth Required | Purpose |
|---|---|---|---|
| `Home` | `/` | No | Landing page — shows public header when logged out, AppHeader when logged in |
| `Login` | `/login` | No | Email + password login |
| `Register` | `/register` | No | Account creation |
| `VerifyPending` | `/verify-pending` | No | "Check your email" screen after registration |
| `VerifySuccess` | `/verify-success` | No | "Email verified, you can log in" screen |
| `VerifyError` | `/verify-error` | No | "Verification failed or expired" screen |
| `ForgotPassword` | `/forgot-password` | No | Request a password reset email |
| `ResetPassword` | `/reset-password?token=` | No | Set new password via reset link |
| `SetupProfile` | `/setup-profile` | Token only (no profile yet) | First-time name/major/year/bio setup |
| `Profile` | `/profile` | Yes | Profile banner, stats, group list, edit modal |
| `StudyGroups` | `/study-groups` | Yes | Browse all groups, search, filter by course, create, join, leave |
| `GroupChatIndex` | `/group-chat` | Yes | List of the user's joined groups with links to each chat |
| `GroupChat` | `/group-chat/:groupId` | Yes | Real-time group chat with member sidebar |
| `ChangePassword` | `/change-password` | Yes | Update password when logged in |

---

### Components

**`AppHeader`** — The navigation bar shown to logged-in users. Contains the WSU StudyGroup logo (links to `/`), Study Groups and Group Chat nav tabs with active highlight, and an avatar dropdown showing the user's name, major, a link to Profile, and a Log Out button. Collapses to a hamburger menu on mobile.

**`Header`** — The stripped-down public header for logged-out pages. Logo only plus Log In and Sign Up buttons. Intentionally has no other navigation.

**`ProtectedRoute`** — Wraps any route that requires authentication. Reads `localStorage.token` — if missing, redirects to `/login`. If the token exists but `profile` is null (new user who completed email verification but hasn't set up their profile), redirects to `/setup-profile`.

**`MembersSidebar`** — A tabbed card shown alongside the group chat. The Members tab lists all group members with avatar initials and their major. The My Groups tab lists all the user's joined groups as navigation links with active state highlight. A "Browse all groups →" link sits at the bottom of the groups tab.

**`ChatMessage`** — Renders a single message. The current user's messages appear on the right with a blue bubble and a "You" label. Other users' messages appear on the left with an avatar initial, their name, and a white bubble. System messages (like "Welcome to Group Name") appear centered in a muted pill.

**`StudyGroupCard`** — A card on the browse page. Shows course code badge, joined/open status, group name, course name, member count, member avatar stack, and View Details / Join / Leave buttons.

---

### Auth Context

**`AuthContext`** is a React context that wraps the entire app. Any component can call `useAuth()` to access:

| Property | Type | Description |
|---|---|---|
| `profile` | object or null | The logged-in user's Profile, or null if not set up |
| `loading` | boolean | True while the app is checking for an existing token on first load |
| `login(token)` | function | Store token, fetch profile, return profile (or null if 404) |
| `logout()` | function | Clear token, clear profile, redirect to `/login` |
| `setProfile` | function | Update profile in-place after edits |

**Initialization:** When the app first loads, `AuthContext` checks `localStorage` for a token. If one exists it calls `GET /profiles`. A successful response sets `profile`. A 404 means the user registered and verified their email but hasn't completed setup — `profile` stays null and the app routes them to `/setup-profile`. A 401/403 means the token is invalid or expired — it is cleared and the user is treated as logged out.

---

### API Layer

**`axios.js`** creates a single shared Axios instance with base URL `http://localhost:8080`.

**Request interceptor** — Before every request, reads the token from `localStorage` and adds it as `Authorization: Bearer <token>`. No component ever touches the token directly.

**Response interceptor** — If the server returns a `401` or `403`, the interceptor clears the token from `localStorage` and redirects to `/login`, unless the current page is one of the public routes (`/login`, `/register`, `/verify-*`, etc.) where a 401 is expected and harmless.

**`auth.js`** is a thin wrapper that provides named functions for auth operations (`register`, `login`, `logout`, `resendVerification`, `forgotPassword`, `resetPassword`, `updatePassword`). This keeps auth-related API calls centralized and out of component code.

---

### Routing

Routes are defined in `App.jsx` using React Router v6. There are three categories:

**Public routes** — no guards, accessible to anyone.

**`SetupRoute`** — a custom guard for `/setup-profile`. Requires a token but actively blocks users who already have a profile (to prevent re-running setup and overwriting data). Redirects to `/login` with no token, to `/profile` if already set up.

**`ProtectedRoute`** — requires both a valid token and a completed profile. Redirects to `/login` with no token, or `/setup-profile` if profile is missing. Wraps all post-login pages.

---

### WebSocket Integration

**Library:** `@stomp/stompjs` with `sockjs-client` as the transport.

`GroupChat.jsx` creates a STOMP client inside a `useEffect` when the component mounts. The client connects to `http://localhost:8080/ws` (the SockJS endpoint) and passes the JWT in the STOMP `CONNECT` headers — this is necessary because the browser's WebSocket API cannot add custom HTTP headers to the upgrade request.

```
Client connects via SockJS to /ws
  → STOMP CONNECT sent with { Authorization: Bearer <token> }
  → Server confirms connection
  → Client subscribes to /topic/chat/{groupId}
  → User sends a message to /app/chat/{groupId}
  → Server saves it, broadcasts to /topic/chat/{groupId}
  → Client receives broadcast, adds to message list
```

The STOMP client is stored in a `useRef` so it persists across re-renders without triggering new effects. The `useEffect` cleanup function calls `client.deactivate()` when the user navigates away from the chat, cleanly closing the connection. If the connection drops, the client automatically retries every 5 seconds.

---

## How Frontend & Backend Connect

### Full Auth Flow

```
1. User visits /register
2. Frontend: POST /auth/register → { email, password, name }
3. Backend: validate @westfield.ma.edu domain, hash password, save User+Profile, send email
4. Frontend: navigate to /verify-pending

5. User clicks email link
6. Backend: GET /auth/verify?token=... → marks User.isVerified = true → redirects to /verify-success

7. User visits /login
8. Frontend: POST /auth/login → { email, password }
9. Backend: check password, check isVerified, generate JWT → return { token }
10. Frontend: localStorage.setItem('token', token)
11. AuthContext: GET /profiles → load Profile
12. Profile exists → navigate to /profile
13. Profile 404 → navigate to /setup-profile

14. User fills out /setup-profile
15. Frontend: POST /profiles → { name, major, year, bio }
16. AuthContext.setProfile(response.data)
17. Navigate to /profile
```

### Full Chat Flow

```
1. User navigates to /group-chat/5
2. Frontend loads:
   - GET /groups/5             → group name, course, members list
   - GET /groups/5/messages    → full message history
   - GET /groups/my            → user's other groups (for sidebar)
3. WebSocket connects to /ws, subscribes to /topic/chat/5
4. History messages + system welcome message are rendered

5. User types "Anyone studied Chapter 4?" and hits Send
6. STOMP publish to /app/chat/5 → { studyGroupId: 5, senderName: "Jose", content: "..." }
7. Spring ChatController receives message
8. ChatService saves Message to database
9. MessageDTO broadcast to /topic/chat/5
10. All subscribers (including the sender) receive it
11. All clients append the new message bubble to their chat view
```

---

## Stubs & Upcoming Features

The following features have controller + service scaffolding but are not yet implemented. They are Sprint 2 targets.

| Feature | Status | Files |
|---|---|---|
| AI Study Assistant | Stub | `AiController.java`, `AiService.java` |
| Gamification (points + badges) | Stub | `GamificationService.java`, `UserBadge.java` |
| Leaderboards | Stub | `LeaderboardController.java` |
| In-app Notifications | Stub | `NotificationController.java`, `NotificationService.java` |
| Meeting Scheduler | Partial | `MeetingSessionController.java`, `MeetingSessionService.java` |
| Classmate Matching | Partial | `CourseController.java` — endpoint exists, frontend not wired |
