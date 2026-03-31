# Contributing to WSU Study Group App

Welcome to the team! This guide will walk you through everything you need to know to get the project running locally and start contributing — even if you've never used Spring Boot or Docker before.

## Team & Sprint 1 Ownership

| Member | Sprint 1 Task |
|--------|--------------|
| Jose Jimenez | Authentication — register, email verify, login, JWT |
| Maicheal Shenouda | Profile + course enrollment endpoints |
| Hayden Parker | Study group management (create, join, leave) |
| Brian Torres | WebSocket real-time chat |
| Eric Melnik | Frontend — auth, profile, courses, classmate view |
| Michael Mayberry | Frontend — study groups, chat UI |

If you're unsure where to start, find your name above and jump to the relevant section in [ARCHITECTURE.md](src/main/java/com/github/wsustudygroupapp/ARCHITECTURE.md).

---

## Table of Contents

1. [What Is This Stack?](#what-is-this-stack)
2. [Prerequisites](#prerequisites)
3. [Local Setup (Step by Step)](#local-setup-step-by-step)
4. [Project Structure](#project-structure)
5. [How the App Works](#how-the-app-works)
6. [Git Workflow](#git-workflow)
7. [Commit Messages](#commit-messages)
8. [Pull Request Process](#pull-request-process)
9. [Daily Standup Format](#daily-standup-format)
10. [Common Problems & Fixes](#common-problems--fixes)

---

## What Is This Stack?

You don't need to be an expert in any of these before you start — just understand what each piece does:

| Tool | What it does |
|------|--------------|
| **Spring Boot** | Java framework that runs our backend server. It handles HTTP requests, talks to the database, and contains all our business logic. |
| **MySQL** | The database where all app data is stored (users, profiles, courses, etc.). |
| **Docker** | Runs MySQL in an isolated container on your machine so everyone has the same database setup without manually installing MySQL. Think of it as a mini virtual machine just for the database. |
| **Maven** (`mvnw`) | Java build tool. Downloads dependencies and compiles/runs the project. The `mvnw` file is a wrapper so you don't need to install Maven separately. |
| **JWT** | JSON Web Tokens — how users stay "logged in". After logging in, the server gives you a token. You send that token with every request to prove who you are. |

---

## Prerequisites

Install the following before anything else.

### 1. Java 17+

Check if you already have it:
```bash
java -version
```
If you see `openjdk 17` or higher, you're good. If not, download it from:
https://adoptium.net/ (pick **Java 17**, the LTS version)

### 2. Docker Desktop

Docker runs our MySQL database. Download and install from:
https://www.docker.com/products/docker-desktop/

After installing, open Docker Desktop and make sure it's running (you should see the whale icon in your taskbar/menu bar). You need Docker running anytime you want to run the app.

### 3. Git

Check if you have it:
```bash
git --version
```
If not, download from: https://git-scm.com/

### 4. IntelliJ IDEA (recommended)

The best IDE for Spring Boot. Download the free Community Edition from:
https://www.jetbrains.com/idea/download/

---

## Local Setup (Step by Step)

Follow these steps in order. Do this once when you first join the project.

---

### Step 1 — Clone the repo

This downloads the project to your machine.

```bash
git clone https://github.com/NaturalTC/WSU-study-group-app.git
cd WSU-study-group-app
```

---

### Step 2 — Set up your local config file

The app needs a config file with your database password, email credentials, and a secret key. This file is **not** stored in git (so secrets stay private). You create it from a template:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Now open `src/main/resources/application-local.properties` in any text editor. It looks like this:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wsu_study_group
spring.datasource.username=studyuser
spring.datasource.password=studypassword
...
spring.mail.username=yourgmail@gmail.com
spring.mail.password=your-app-password
...
app.jwt.secret=your-secret-key-here-make-it-long-and-random
```

Things to fill in:
- **`spring.mail.username`** — a Gmail address used by the **app** to send verification emails (this is not how users log in — users always log in with their `@westfield.ma.edu` school email). Use a shared team Gmail or your own.
- **`spring.mail.password`** — a Gmail App Password for that account (not your real Gmail password). To get one: Google Account → Security → 2-Step Verification → App Passwords → create one for "Mail"
- **`app.jwt.secret`** — make up any long random string (e.g. `wsuStudyGroupSuperSecretKey2026AbcXyz!`)

Everything else (database URL, username, password) is already filled in to match Docker — don't change those.

> **Note:** `application-local.properties` is in `.gitignore`. Never commit it. Never share it. Your credentials stay on your machine only.

---

### Step 3 — Start the database

Make sure Docker Desktop is running first, then:

```bash
docker compose up -d
```

What this does:
- Downloads a MySQL 8.0 image (first time only — takes ~1 minute)
- Starts a MySQL container named `wsu-studygroup-db`
- Creates the `wsu_study_group` database automatically
- Runs in the background (`-d` = detached)

You only need to do this once per work session. The database keeps running until you stop it.

To check it started correctly:
```bash
docker ps
```
You should see `wsu-studygroup-db` in the list with status `Up`.

---

### Step 4 — Run the app

```bash
./mvnw spring-boot:run
```

On Windows:
```bash
mvnw.cmd spring-boot:run
```

What happens on first run:
1. Maven downloads all dependencies (takes 1-2 minutes first time)
2. Hibernate reads the entity classes and creates all database tables automatically
3. Spring runs `data.sql` which inserts all ~500 WSU courses into the database
4. The server starts on `http://localhost:8080`

You'll know it's ready when you see something like:
```
Started WsuStudyGroupAppApplication in 4.3 seconds
```

---

### Step 5 — Verify the database (optional but recommended)

This checks that the course data was seeded correctly:

```bash
docker exec -it wsu-studygroup-db mysql -u studyuser -pstudypassword wsu_study_group -e "SELECT COUNT(*) FROM course_table;"
```

You should see a count around 500. If you get 0, the seed didn't run — ask Jose.

---

### Step 6 — Run tests

```bash
./mvnw test
```

Run this before opening any pull request to make sure you didn't break anything.

---

### Step 7 — Stop the database when you're done

```bash
docker compose down        # stops the container, keeps your data
docker compose down -v     # stops the container AND wipes all data (use this to reset)
```

You don't have to stop it every time — it's fine to leave it running. Just stop it when you're done for the day or your computer is running slow.

---

## Project Structure

```
src/main/java/com/github/wsustudygroupapp/
├── config/          # Security and app configuration
├── controller/      # HTTP endpoints (what the frontend calls)
├── dto/             # Data Transfer Objects (request/response shapes)
├── filter/          # JWT auth filter (runs on every request)
├── model/           # Database entities (User, Profile, Course, UserCourse)
├── repository/      # Database queries (Spring Data JPA)
├── service/         # Business logic (where the real work happens)
└── util/            # Helpers (e.g. JwtUtil)

src/main/resources/
├── application.properties               # Shared config (safe to commit)
├── application-local.properties         # Your secrets (DO NOT COMMIT)
├── application-local.properties.example # Template for teammates
└── data.sql                             # Seeds ~500 WSU courses on startup
```

**The flow of a request:**
```
HTTP Request → Controller → Service → Repository → Database
                                ↑
                           Business logic lives here
```

- **Controller** — receives the HTTP request, calls the service, returns a response. Keep it thin.
- **Service** — where the actual logic goes (validate input, call repos, build responses).
- **Repository** — talks to the database. Spring generates most queries automatically from method names.
- **Model** — Java classes that map to database tables (annotated with `@Entity`).

---

## How the App Works

### Authentication

- Users register with a `@westfield.ma.edu` school email and password
- A verification email is sent with a one-time link
- After verifying, users can log in and receive a **JWT token**
- That token goes in the `Authorization: Bearer <token>` header of every request
- The server validates the token on each request — no sessions, no cookies

### Course Enrollment

- All WSU courses are pre-loaded in the database (students can't add their own)
- When a student enrolls in a course, they provide: `courseCode`, `section`, `semester`
- This gets stored as a `UserCourse` record linking their profile to the course

### Study Group Matching

- When a student views classmates for a course, the app finds all other `UserCourse` records with the same `courseId`, `section`, and `semester`
- This is the core feature — students find each other through shared class sections

---

## Git Workflow

We use **Git Flow** — all feature work branches off `dev` and gets reviewed before merging.

```
main       ← stable, production-ready
  └── dev  ← integration branch, always working
        └── feature/your-feature  ← your work
```

**Day-to-day workflow:**

```bash
# 1. Make sure you're on dev and up to date
git checkout dev
git pull origin dev

# 2. Create your feature branch
git checkout -b feature/your-feature-name

# 3. Do your work, commit often
git add .
git commit -m "feat: add user profile endpoint"

# 4. Push your branch to GitHub
git push origin feature/your-feature-name

# 5. Open a Pull Request on GitHub targeting dev
```

### Branch Naming

| Type    | Pattern                       | Example                      |
|---------|-------------------------------|------------------------------|
| Feature | `feature/<short-description>` | `feature/user-login`         |
| Bug fix | `bugfix/<short-description>`  | `bugfix/session-expiry`      |
| Chore   | `chore/<short-description>`   | `chore/update-dependencies`  |

**Never push directly to `main` or `dev`.** Always go through a PR.

### Handling Merge Conflicts

If your branch has conflicts with `dev`:

```bash
git checkout dev
git pull origin dev
git checkout feature/your-feature-name
git merge dev
# fix the conflicts in your editor, then:
git add .
git commit -m "chore: resolve merge conflicts with dev"
git push origin feature/your-feature-name
```

---

## Commit Messages

Use this format for every commit:

```
<type>: <short description>

Optional longer explanation if needed.
```

| Type | When to use |
|------|-------------|
| `feat` | Adding a new feature |
| `fix` | Fixing a bug |
| `chore` | Maintenance (dependencies, config) |
| `refactor` | Restructuring code without changing behavior |
| `test` | Adding or updating tests |
| `docs` | Documentation only |

Examples:
```
feat: add study group search endpoint
fix: correct null check in UserService
chore: bump spring-boot to 4.0.5
refactor: extract email validation to helper method
test: add unit tests for AuthService
```

Keep the first line under 72 characters. Use the body to explain *why*, not *what*.

---

## Pull Request Process

1. Push your branch to GitHub
2. Go to the repo on GitHub → click **"Compare & pull request"**
3. Set the base branch to `dev` (not `main`)
4. Write a short description of what you did and why
5. Request at least **1 reviewer** from the team
6. Address all review comments — push new commits to the same branch
7. Once approved, use **Squash and merge** to keep the history clean

---

## Daily Standup Format

Post in `#standups` on Discord every day by 10am:

```
Yesterday: what you worked on
Today: what you're planning to work on
Blockers: anything stopping you (or "none")
```

Example:
```
Yesterday: set up local environment, got the app running
Today: working on the profile update endpoint
Blockers: none
```

---

## Common Problems & Fixes

**`./mvnw: Permission denied`**
```bash
chmod +x mvnw
```

**`Communications link failure` or can't connect to database**
- Docker isn't running — open Docker Desktop and wait for it to start
- Then run `docker compose up -d` again

**`Table 'wsu_study_group.xxx' doesn't exist`**
- Hibernate hasn't created the tables yet. Make sure `spring.jpa.hibernate.ddl-auto=update` is in your `application-local.properties`. Stop the app and run it again.

**`Could not connect to SMTP host: smtp.gmail.com`**
- The app uses Gmail only to **send** verification emails — this is not user login. Users log in with their `@westfield.ma.edu` school email.
- Check that `spring.mail.username` and `spring.mail.password` are filled in correctly in `application-local.properties`
- Make sure 2-Step Verification is enabled on that Google account before generating an app password

**Port 3306 already in use**
- Another process is using MySQL's port. Run `docker ps` to see if a container is already running. If so, that's fine — the database is already up.

**`application-local.properties` not found**
- You forgot Step 2. Run:
  ```bash
  cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
  ```
  Then fill in your credentials.

**IntelliJ doesn't recognize Spring annotations / red errors everywhere**
- Right-click `pom.xml` → Maven → Reload Project. Wait for it to finish downloading dependencies.

---

> Questions? Ask in `#dev-help` on Discord before getting stuck for more than 30 minutes.
