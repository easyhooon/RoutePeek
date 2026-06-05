---
name: resolve-coderabbit-review
description: Use when addressing CodeRabbit review comments on a RoutePeek GitHub PR.
---

# Resolve CodeRabbit Review

## Scope

Handle unresolved CodeRabbit review threads on the current PR. Keep responses and PR comments in English because RoutePeek is a public library.

## Workflow

1. Identify the PR:
   - If the user provides a PR number, use it.
   - Otherwise run `gh pr view --json number,headRefName,baseRefName,url,isDraft`.
   - Stop on draft PRs unless the user explicitly asks to continue.
2. Fetch unresolved review threads with GitHub GraphQL.
3. Keep only threads where the first comment author is `coderabbitai[bot]` or `coderabbitai`.
4. Skip threads already handled with comments containing:
   - `✅ Addressed in`
   - `Thanks for the suggestion. After review, we've decided not to apply this change.`
5. Sort by risk: security/crash, bug, maintainability, style.
6. For each actionable comment:
   - Read the target file and nearby code.
   - Apply only focused changes that match existing project patterns.
   - Prefer imports over inline fully-qualified Kotlin names.
   - Keep public APIs compatible between `:routepeek` and `:routepeek-noop`.
7. Verify relevant changes:
   - Normal code changes: `./gradlew :app:assembleDebug build`.
   - Publishing changes: also `./gradlew publishToMavenLocal`.
8. Commit accepted fixes with compact English messages and push.
9. Reply to the CodeRabbit thread:
   - Accepted: `✅ Addressed in https://github.com/easyhooon/RoutePeek/commit/<hash>`
   - Declined: ask the user before posting the decline reason.
10. Resolve the handled review thread through GitHub GraphQL.
11. Post or update a short PR summary comment if multiple threads were handled.

## Declines

Do not decline automatically. Present the comment, file, severity, and concrete reason to the user first. After approval, reply in English with a concise reason.

## Final Report

Report PR number, applied count, declined count, commits, verification commands, and any remaining unresolved threads.
