# Contributing

Thanks for your interest in contributing to MinecraftOTEL.

## Quick Start
1. Open or find an issue that describes the change.
2. Fork the repo and create a branch.
3. Make your changes with tests and docs as needed.
4. Open a pull request with a clear description.

## Branching Model
We use a lean trunk-based model. See: `docs/development/branching-model.md`.

## Development Setup
- Java 21+
- Gradle Wrapper (included)

Build locally:
```
./gradlew build
```

## Commit and PR Guidelines
- Use clear, descriptive commit messages.
- Keep PRs small and focused.
- Explain user impact and any behavior changes.
- Update docs for new features or config changes.

## Code Style
- Follow existing formatting and conventions in the codebase.
- Keep methods small and focused.
- Avoid heavy work on hot paths.

## Tests
- Add or update tests when behavior changes.
- If you cannot add tests, explain why in the PR.

## Documentation
- Admin documentation lives in `docs/admin/`.
- Developer documentation lives in `docs/development/`.

## Community Standards
Please follow the Code of Conduct in `CODE_OF_CONDUCT.md`.
