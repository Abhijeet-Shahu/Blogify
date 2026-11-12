# Blogify
A Blog Website which shows all the Blogs free for all

## Environment variables

Required at startup:

- `JWT_SECRET`: Base64-encoded secret that decodes to at least 32 bytes. Generate one with `openssl rand -base64 32`.
- `SUPABASE_DATABASE_URL`: Supabase PostgreSQL JDBC URL.
- `SUPABASE_DATABASE_USERNAME`: PostgreSQL username.
- `SPRING_DATASOURCE_PASSWORD`: PostgreSQL password.

Optional integrations:

- `GROQ_API_KEY`: Groq API key used for blog content generation. If absent, the application uses its built-in content fallback.
- `STABILITY_API_KEY`: Stability API key used for image generation. If absent, the application uses its placeholder-image fallback.
- `GROQ_MODEL`: optional Groq model override, defaults to `llama-3.3-70b-versatile`.
- `PORT`: optional server port, defaults to `8080`.

## Local environment loading

Spring Boot does not load `.env` files automatically. Copy `.env.example` to `.env`, replace every placeholder with local values, and export the variables before starting the application:

```sh
set -a
. ./.env
set +a
./mvnw spring-boot:run
```

Keep `.env` untracked; it is excluded by `.gitignore`.

## Deployment secret injection

Configure the required variables as encrypted environment variables or secrets in the deployment platform (for example, Render or a container orchestrator). The platform injects them into the process environment before the JAR starts. Do not copy `.env` into the Docker image, commit it, or pass secrets as Dockerfile build arguments.
