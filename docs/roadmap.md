# Engineering roadmap

This roadmap is deliberately evidence-gated. A milestone is complete only when its acceptance criteria pass; a document or design proposal is not an implementation.

## M0 — Project safety and architecture baseline

- [x] State the Android-device-first constraint.
- [x] Define architecture boundaries and local-build feasibility gate.
- [x] Define device compatibility evidence levels.
- [x] Define privacy and security defaults.
- [ ] Review and accept the baseline through a pull request.

The first probe implementation is under `app/`. It intentionally requests no permissions and performs no partition writes. Its source and unit tests have not yet been compiled or executed.

## M1 — Read-only Android environment probe

Status: initial Kotlin source prototype added; build and on-device validation are pending.

Implement a minimal Kotlin Android app that reports:
- manufacturer, model, product/device identifiers where available;
- Android API/release, build ID/fingerprint, security patch level;
- ABI list, kernel release, available storage, memory class and runtime memory observations;
- which values are unavailable or permission-restricted.

Acceptance:
- runs on the SM-X200 without root, Shizuku, a PC, or another device;
- does not write to partitions or alter boot settings;
- labels unavailable values honestly;
- local unit tests cover formatting boundaries and unknown/missing values;
- an on-device test record captures the exact app version and tablet firmware.

The current Gradle module declares only the local JUnit test dependency; it does not advertise an Android instrumentation runner because no instrumentation tests exist yet.

## M2 — Device manifest and validator

- Define a versioned schema.
- Add the SM-X200 manifest using only observed or sourced values.
- Include provenance and confidence for every nontrivial hardware/firmware claim.
- Validate malformed, incomplete, and contradictory manifests.

Acceptance:
- deterministic validation with actionable errors;
- unknown fields are handled according to schema policy;
- no inferred partition or encryption facts are silently accepted.

## B0 — Local automation protocol foundation

Status: initial pure-Kotlin protocol primitives and unit-test source added; Android/Gradle test execution is pending. This is not a running broker or ChatGPT connection.

- Typed operation registry with read-only and local-change risk classes.
- Request validation for protocol version, request ID syntax, overflow-safe expiry, bounded parameters, output fields, canonical HTTPS GitHub source URL/path, and immutable commit SHA.
- Project operations require a syntactically valid workspace ID; the future broker must resolve it against an app-owned workspace registry before access. The pure validator does not establish registration or prevent request-ID replay.
- APK inspection requires a syntactically valid artifact ID; the future broker must resolve it against an app-owned artifact registry.
- Explicit job state-transition table rejects invalid and terminal-state transitions.
- Bounded diagnostic redaction for common bearer tokens, credential assignments, and email addresses. This is defense in depth, not a guarantee that arbitrary logs are safe to export.
- Unit-test source covers expiry overflow, workspace/artifact identifier validation, repository URL edge cases, state transitions, and redaction; tests have not yet been run in the Android project.
- Repository status is classified read-only while remaining workspace-scoped; checkout, tests, and builds remain local-change operations.

This foundation deliberately contains no shell execution, transport, listener, persistence, Termux integration, Shizuku integration, or privileged operation.

## M3 — Local job engine prototype

- Follow the security gates in [the local automation bridge proposal](local-automation-bridge.md); this is a design, not an implemented remote-control feature.
- Probe execution capabilities without assuming Termux, PRoot, root, or Shizuku.
- Evaluate app-compatible native execution and optional Termux integration.
- Add persistent job state, cancellation, logs, and resource preflight.
- Benchmark storage, memory, thermal behavior, and interruption recovery.

Acceptance:
- demonstrated on the actual SM-X200;
- no mandatory remote worker;
- no claim of full recovery compilation unless a real build succeeds from pinned inputs.

## M4 — Recovery source adapter and artifact inspection

- Add one recovery adapter only after its build interface is verified.
- Pin all sources and patches.
- Inspect image metadata and calculate hashes.
- Keep compile success, image inspection, boot testing, and feature validation as separate states.

## M5 — Hardware validation

- Establish safe, reversible test procedures.
- Validate boot, touch, display, USB/ADB, storage, backup/restore, and encryption separately.
- Expand to another manufacturer only after the first device's evidence record is complete.

## Explicitly out of scope until separately reviewed

Automatic flashing, partition writes, formatting, bootloader changes, root installation, and automatic cloud uploads.
