# Implementation and validation register

**Classification:** Snapshot of repository/project evidence reviewed on 2026-11-23. Refresh before relying on it.  
**Rule:** Implemented, compiled, tested, installed, device-tested, and hardware-verified are separate states.

## Current workstreams

| Item | Source location | Implemented | Compiled / tested | Installed / device-verified |
|---|---|---:|---:|---:|
| Android device probe | app source tree | Prototype source present | Android Gradle build and repository tests not verified | No |
| Job protocol validation | JobProtocol.kt | Source present | Earlier isolated JVM smoke harness reportedly passed 8 assertions; not repository JUnit | No |
| Diagnostic redaction | protocol source | Source present | Earlier standalone formatter smoke test reportedly passed; not Android suite | No |
| SQLite LocalJobStore | LocalJobStore.kt | Source present; recent storage changes committed | Host SQLite smoke checks only; Android Kotlin build and SQLiteOpenHelper tests pending | No |
| Secure read-only broker | docs/local-automation-bridge.md | Design proposal only | Not applicable | No |
| Termux adapter | design only | Not implemented | Not applicable | No |
| ChatGPT-to-device transport | design investigation only | Not implemented | Not applicable | No |
| Recovery source build | PR #1 / experimental sources | Experimental workflow exists | No current validated local recovery build | No |
| Recovery image compatibility | research sources | Not established | No exact-firmware boot proof | No |

## Recent source changes

The Android-first branch includes a local job store with:
- request ID primary-key uniqueness;
- workspace and artifact identifier persistence;
- bounded summary persistence with redaction;
- compare-and-set state transitions;
- nondecreasing updated timestamps;
- expiry predicate during transitions;
- operation/state and metadata constraints on newly created databases;
- a schema v1-to-v2 migration adding artifact ID without intentionally dropping existing records.

These are source-level claims. The migration and behavior still require Android-backed tests. A host SQLite smoke test is not equivalent to Android SQLiteOpenHelper behavior.

## Evidence limitations

Previously reported isolated tests:
- A standalone JVM smoke harness for earlier job-protocol logic compiled and passed 8 assertions in an isolated Linux environment.
- A standalone formatter smoke test was reported as passing.
- A host SQLite smoke test was performed for schema creation, artifact persistence, timestamp predicates, and a simulated v1-to-v2 migration.

These do not establish:
- successful Android Gradle compilation;
- successful execution of the repository's JUnit suite;
- Android SQLiteOpenHelper migration correctness;
- concurrent database behavior on Android;
- APK generation or installation;
- runtime behavior on the SM-X200;
- recovery image bootability or hardware compatibility.

## Required next validation work

## Evidence provenance and result handling

This register combines repository inspection and previously reported smoke-test summaries. The older smoke tests are not all accompanied here by full raw logs, exact tool versions, or immutable harness source. Treat them as historical reported results until their evidence is recovered and attached. Do not cite them as independently reproducible CI or Android test results.

For each new run, retain the command, working directory, exit code, tool versions, source commit, start/end time, and a concise redacted log excerpt. For device runs, include the exact firmware/build and app version. Hash distributable APKs and recovery images. Never include signing keys or private credentials.

A failed, skipped, flaky, or inconclusive test must be recorded as such; do not convert it to "passed" by rerunning until green without retaining the failure history. Separate a test's result from the coverage it provides.

1. Inspect the current branch and exact Kotlin/Gradle configuration.
2. Add Android-backed tests for database creation, duplicate request IDs, artifact/workspace persistence, invalid states, timestamp ordering, expiry, and migration from a real v1 schema.
3. Test migration rollback and preservation of existing records.
4. Test concurrent compare-and-set transitions and database reopen/process restart.
5. Compile the app and run all repository tests using the same-device Termux path.
6. Install only the app through the normal Android package installer after build verification.
7. Capture app version, device build, commands, exit codes, test reports, APK SHA-256, and observed behavior.
8. Update this register only with results actually obtained.

## Reporting template

For every result record:

- Component and source commit.
- Status: proposed / implemented / compiled / unit-tested / integration-tested / installed / device-tested / hardware-verified.
- Exact command or UI procedure.
- Environment: device model, Android build, tool versions.
- Exit code and concise output.
- Artifact hash where applicable.
- Date and reviewer.
- Known limitations and reproduction steps.

## Release gate

No recovery artifact may be described as safe or compatible based solely on a source tree, successful CI job, compile, or static inspection. Exact-device boot, touch, storage, encryption, and recovery workflows require separately recorded tests. No flashing or destructive action without explicit authorization.
