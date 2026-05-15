# Existing Database Runtime Notes

## Runtime Layout
- `backend/`: Spring Boot 3 backend.
- `apps/admin-web/`: Vue 3 admin app.
- `apps/portal-web/`: Vue 3 student portal app.
- `university(1).sql`: source-of-truth schema for the running database.

## Database Rule
The backend must not reshape the production schema. It now reads and writes the
existing tables from `university(1).sql` directly. Flyway is disabled and the
new normalized migration scripts were removed from the backend module.

## Table Usage
- `users`: administrator login.
- `student`: student login and profile.
- `province`: province data.
- `universityinformation`: universities.
- `professionalinformation`: majors.
- `collegeapplication`: university applications.
- `professionalregistration`: major applications.
- `admissionresults`: admission results.
- `resultsinformation`: academic results.
- `news`: news articles.
- `aboutus`: about-us site page.
- `systemintro`: system-intro site page.
- `config`: application settings.
- `chat`: consultations.
- `storeup`: favorites.
- `token`: ignored by the new JWT authentication flow.

## API Compatibility Layer
The external API still uses `/api/v1/**` and clean response models. Internally,
services map those models onto the existing legacy table and column names through
`com.university.backend.legacy`.

## Startup Requirements
Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to the running database. If the
database is named `university`, the default URL is:

```text
jdbc:mysql://127.0.0.1:3306/university?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
```

