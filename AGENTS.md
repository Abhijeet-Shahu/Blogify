# Blogify — Agent Engineering Rules

## Project identity

Repository: Abhijeet-Shahu/Blogify

Blogify is an existing blogging application.

Technology stack:
- Java 17
- Spring Boot 3.5.4
- Spring MVC
- Thymeleaf
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- PostgreSQL / Supabase
- HTML, CSS and JavaScript
- Maven

## Non-negotiable rules

1. Preserve the existing technology stack.
2. Do not migrate to React, Next.js, Vue or another frontend framework.
3. Do not replace PostgreSQL or Supabase.
4. Inspect existing functionality before modifying it.
5. Preserve existing API contracts unless changes are necessary and documented.
6. Never delete working features to simplify implementation.
7. Never introduce fake users, articles, statistics or API responses.
8. Never hardcode credentials, JWT secrets or API keys.
9. Never expose secrets in logs, commits or error messages.
10. Never push directly to main.
11. Never perform destructive database operations without approval.
12. Never claim that a test passed unless it was executed successfully.

## Engineering standards

- Follow existing Java package conventions.
- Use dependency injection properly.
- Keep controllers thin.
- Put business logic in service classes.
- Use DTOs and validation where appropriate.
- Handle exceptions consistently.
- Use secure password handling.
- Validate and authorize every protected operation.
- Prevent stored and reflected XSS.
- Validate uploaded files.
- Use accessible semantic HTML.
- Keep CSS reusable and maintainable.
- Avoid unnecessary dependencies.
- Avoid duplicated frontend and backend logic.

## Design standards

Create a premium editorial blogging platform.

Visual direction:
- Warm off-white backgrounds.
- Near-black typography.
- Restrained lime accent.
- Premium editorial layouts.
- Strong typography hierarchy.
- Asymmetric article grids.
- Large cover photography.
- Professional writing workspace.
- Responsive author profiles.
- Clean dashboard.

Avoid generic SaaS templates, excessive gradients, excessive rounded cards, random shadows and unnecessary animations.

All existing pages must follow one consistent design system.

## Workflow

Before every implementation phase:

1. Explain what files will change.
2. Explain the implementation approach.
3. Identify potential regressions.
4. Identify any security-sensitive changes.

After implementation:

1. Run relevant tests.
2. Run the Maven build.
3. Check the browser console where applicable.
4. Verify affected routes and APIs.
5. Report changed files.
6. Report tests and their actual results.
7. Document unresolved issues.

## Git policy

- Work only on the dedicated feature branch.
- Make small, logical commits.
- Do not force-push.
- Do not amend or rewrite existing shared commits without approval.
- Do not merge into main without review.

## Completion criteria

The project is complete only when:
- Existing functionality is preserved.
- Confirmed bugs are fixed.
- Security issues are addressed.
- All pages are redesigned consistently.
- Responsive layouts are verified.
- Production build succeeds.
- Relevant automated tests pass.
- Remaining limitations are documented.