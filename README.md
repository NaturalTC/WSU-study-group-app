# WSU Study Group Finder

A full-stack web application designed exclusively for Westfield State University students. The platform connects students based on shared courses and sections, enabling them to form study groups, communicate in real time, and enhance their learning through AI-assisted tools.

**Repository:** https://github.com/NaturalTC/WSU-study-group-app

---

## Team

| Name | GitHub                                      |
|------|---------------------------------------------|
| Jose Jimenez | [@NaturalTC](https://github.com/NaturalTC)  |
| Hayden Parker | [@Aalexlee22](https://github.com/Aalexlee22) |
| Eric Melnik | [@ericmelnik](https://github.com/ericmelnik) |
| Michael Mayberry | [@MichaelMayberry](https://github.com/MichaelMayberry) |
| Brian Torres | [@btorres561](https://github.com/btorres561) |
| Maicheal Shenouda | [@ymazir](https://github.com/ymazir)        |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java + Spring Boot |
| Security | Spring Security + JWT |
| Database | MySQL 8.0 (Docker locally, AWS RDS in production) |
| ORM | Hibernate (JPA) |
| Real-Time | WebSockets (STOMP) |
| AI | ChatGPT API (proxied through backend) |
| Email | JavaMailSender + Gmail SMTP |
| Deployment | AWS EC2 + RDS |

---

## Core Features

- **Authentication** — `@westfield.ma.edu` school email required, email verification, JWT login
- **Student Profiles** — name, major, academic year
- **Course Enrollment** — pre-seeded WSU catalog, students pick section + semester
- **Classmate Matching** — automatically find students in the same course section
- **Study Groups** — create, join, and leave groups tied to specific courses
- **Real-Time Chat** — WebSocket group chat with message persistence
- **AI Study Assistant** — ChatGPT side panel for context-aware academic help
- **Meeting Scheduler** — schedule sessions, notify all group members
- **Gamification** — points, badges, and filterable leaderboards

---

## Sprint Plan

### Sprint 1 — Proof of Concept
> Core functionality working end-to-end

| Member | Task |
|--------|------|
| Jose Jimenez | Authentication — register, email verify, login, JWT |
| Maicheal Shenouda | Profile + course enrollment endpoints |
| Hayden Parker | Study group management (create, join, leave) |
| Brian Torres | WebSocket real-time chat |
| Eric Melnik | Frontend — auth, profile, courses, classmate view |
| Michael Mayberry | Frontend — study groups, chat UI |

### Sprint 2 — Polish & Enhancement
> Usability improvements and advanced features

| Member | Task |
|--------|------|
| Jose Jimenez | ChatGPT AI side panel |
| Maicheal Shenouda | Meeting scheduler |
| Hayden Parker | Gamification + leaderboard backend |
| Brian Torres | Notifications system |
| Eric Melnik | Frontend — leaderboard, badges, polish, AWS deploy |
| Michael Mayberry | Frontend — AI panel, scheduler UI | 

---

## Local Setup

See [CONTRIBUTING.md](CONTRIBUTING.md) for full setup instructions including Docker, Gmail SMTP config, and running the app locally.

---

## Stretch Goals

- Campus study location map
- Multi-university support
- Google Calendar integration
