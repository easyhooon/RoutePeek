# Project Guidelines

## Commit & PR Rules

- Do NOT include `Co-Authored-By` lines referencing AI tools in commit messages.
- Do NOT include "Generated with Claude Code" or similar AI attribution in PR descriptions.
- Keep commit messages compact. Subject <= 72 chars. Add a body only when the why is not obvious from the diff.
- For commit-push requests, verify the relevant Gradle task first, inspect `git diff --stat`, commit with an English subject, and push the current branch.
- Do not include unrelated generated files or local wrapper churn in commit-push changes.

## Code Style

- Do NOT use fully-qualified names inline in Kotlin code. Add imports and use simple type names.
- Keep comments compact. Explain non-obvious intent in one or two lines, and skip comments that restate the code.
- Keep public APIs consistent between `:routepeek` and `:routepeek-noop` so debug and release dependency swaps remain source-compatible.

## Project Structure

- `:app` is the sample app Gradle path and maps to the `sample/` directory.
- `:routepeek` is the debug implementation artifact.
- `:routepeek-noop` is the no-op implementation artifact for non-debug builds.
- The shared library version is `libs.versions.routepeek` in `gradle/libs.versions.toml`; use it for all RoutePeek publish coordinates and generated version constants.

## Verification

- Run `./gradlew :app:assembleDebug build` before committing build-related changes.
- Run `./gradlew publishToMavenLocal` before Maven Central release changes.
- Repo-local Codex skills live under `.codex/skills/`; use them for commit-push, release, and CodeRabbit review workflows when relevant.

## Release

- Always run `./gradlew clean` before publishing to Maven Central to avoid stale build cache artifacts.
- Use the local Maven Central and signing credentials from Gradle properties; do not commit credential values.
- Confirm the target version does not already exist on Maven Central before publishing because released coordinates are immutable.
