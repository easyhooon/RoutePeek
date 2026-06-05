---
name: commit-push
description: Use when the user asks to commit-push, commit and push, or says 커밋/푸시 in the RoutePeek repository.
---

# RoutePeek Commit Push

## Workflow

1. Inspect `git status --short` and `git diff --stat`.
2. Verify relevant changes before committing:
   - Build or sample behavior change: run `./gradlew :app:assembleDebug build`.
   - Maven publishing change: also run `./gradlew publishToMavenLocal`.
3. Exclude unrelated generated files, Gradle wrapper churn, local screenshots, and build artifacts.
4. Use a compact English commit subject, 72 chars or less.
5. Do not add AI attribution or `Co-Authored-By` lines.
6. Run `git commit -m "<subject>"`.
7. Push the current branch with `git push origin <branch>`.
8. Report the commit hash, branch, and verification commands.
