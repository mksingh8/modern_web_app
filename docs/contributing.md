# Contributing Guide

This project uses a consistent branching and pull request workflow.

## Branch naming

Create branches from `main` using the format:

`<type>/<issue-number>-<short-description>`

### Allowed types

- `feature` for new features
- `fix` for bug fixes
- `chore` for maintenance work
- `docs` for documentation updates
- `refactor` for internal code improvements

### Examples

- `feature/39-define-coding-conventions`
- `fix/142-login-timeout`
- `docs/85-update-api-guide`

## Commit conventions

Use clear, focused commits with a conventional prefix:

`<type>(optional-scope): <short summary>`

### Common types

- `feat`
- `fix`
- `docs`
- `test`
- `refactor`
- `chore`

### Examples

- `feat(auth): add token refresh endpoint`
- `fix(frontend): handle empty dashboard response`
- `docs(contributing): add PR template guidance`

## Pull request conventions

- Keep pull requests focused on a single issue or goal.
- Reference the issue in the PR description (for example: `Closes #39`).
- Fill in the PR template completely.
- Include test notes for what was run and the result.
- Add screenshots when UI behavior changes.
- Request review only after checks pass and merge conflicts are resolved.
