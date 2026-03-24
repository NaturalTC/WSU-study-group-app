# Contributing to WSU Study Group App

## Branch Naming

Branch off `dev` for all work ( create a new branch following one of the criteria / naming convention listed below) :

| Type    | Pattern                        | Example                        |
|---------|--------------------------------|--------------------------------|
| Feature | `feature/<short-description>`  | `feature/user-login`           |
| Bug fix | `bugfix/<short-description>`   | `bugfix/session-expiry`        |
| Chore   | `chore/<short-description>`    | `chore/update-dependencies`    |

Never push directly to `main` or `dev`.

## Commit Messages

Use the following format:

```
<type>: <short description>

Optional longer explanation if needed.
```

Types: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`

Examples:
```
feat: add study group search endpoint
fix: correct null check in UserService
chore: bump spring-boot to 4.0.5
```

## Pull Request Process

1. Branch off `dev` ( the new branch you created )
2. Open a PR targeting `dev` ( Submit a pull request to merge your branch with the dev branch so someone can review your code )
3. Request at least **1 reviewer** ( someone reviews code and makes comments )
4. Address all review comments before merging ( make changes neccessary based of team member comments on your pull request )
5. Squash and merge when approved ( code is merged after changes are made and approved )

## Local Setup

1. Copy `src/main/resources/application-local.properties.example` to `application-local.properties` in the same folder and fill in your MySQL credentials (this file is gitignored)
2. Run with: `./mvnw spring-boot:run`
3. Tests: `./mvnw test`
