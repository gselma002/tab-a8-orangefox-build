# Android-first architecture

## Status

Proposed foundation. This document describes intended boundaries and experiments; it does not claim that an Android application or local recovery build engine already exists.

## Product constraint

The primary development and user workflow must run on the target Android device itself. For the current reference target, that is the Samsung Galaxy Tab A8 Wi-Fi (SM-X200, codename `gta8wifi`). A PC, second phone, or remote build service must not be a required dependency.

A remote runner may be added later as an explicitly optional capability. It must never be presented as an on-device build.

## Architecture boundaries

1. **Android client (Kotlin):** device and environment inspection, permission-aware file access, job lifecycle UI, progress, cancellation, logs, and clear safety gates.
2. **Local execution adapter:** evaluates which supported execution environment is available on this Android installation. Candidate paths include app-compatible native tools and an explicitly configured Termux integration. No path is considered supported until tested on real target hardware.
3. **Build orchestration:** declarative build plans, pinned source revisions, dependency checks, resumable stages, resource preflight, and reproducible logs. Existing upstream recovery build systems remain authoritative; do not rewrite them without evidence.
4. **Device definitions:** versioned manifests describing exact model/variant, SoC, firmware constraints, partition metadata, boot image format, AVB facts, panel/touch variants, and evidence provenance.
5. **Recovery adapters:** isolated integrations for OrangeFox, TWRP, and future recovery projects. A device definition must not assume that all recovery projects share the same build or packaging rules.
6. **Artifact verification:** record SHA-256, size, source revisions, build configuration, image format, and inspection results. Verification must distinguish static inspection from boot and hardware testing.

## Android execution constraints

- Android app sandboxing, scoped storage, process execution restrictions, memory limits, thermal throttling, and background execution limits are first-class design constraints.
- Termux/PRoot is not a virtual machine and must not be described as a strong security boundary.
- Shizuku provides a shell-level bridge for supported operations; it is not root and does not grant arbitrary block-device access.
- Root is not assumed. A feature requiring root must be optional, separately permissioned, and unavailable by default.
- Long-running work must be lifecycle-safe, cancellable, observable, and recoverable after process death or device restart. Select Android foreground-service and WorkManager mechanisms only after matching the actual job type and current Android API requirements.
- Resource preflight must measure actual free storage, available memory, thermal state, and execution capabilities. Do not infer hardware capacity from a model name or a single SKU.

## Technology decisions

- Kotlin is the default for the Android application and Android platform integration.
- Keep the initial core small. Introduce Rust or another portable language only for a demonstrated need such as safe binary parsing or reusable deterministic validation.
- Use existing upstream shell/Python build tooling where required, with explicit environment checks and controlled invocation.
- Use versioned JSON/YAML device manifests with a machine-validated schema. The schema and validation rules must be tested.
- Do not introduce a server, database, Go service, web dashboard, or mandatory cloud account until a measured requirement justifies it.

## Local-build feasibility gate

A conventional Android recovery source build is large and may not fit or run reliably on the SM-X200. ARM64 Android userspace is not interchangeable with a conventional x86-64 Linux/glibc workstation. PRoot does not remove those differences.

Before claiming local compilation support, a prototype must demonstrate on the actual SM-X200:
- the exact supported Android version and execution environment;
- required toolchain binaries and host compatibility;
- peak and sustained storage use, including temporary files;
- peak memory use, process limits, thermal behavior, and elapsed time;
- interruption, cancellation, and resume behavior;
- successful compilation from pinned source inputs;
- artifact inspection and reproducibility checks.

If full compilation is infeasible, the app must still provide useful local device inspection, manifest validation, source preparation, build diagnostics, and artifact inspection without implying that compilation occurred.

## Non-goals for the first milestone

- No automatic flashing, partition writes, formatting, bootloader changes, or recovery installation.
- No claim of encryption support without successful tests against the exact firmware and data-encryption configuration.
- No universal manufacturer support before a second device family has passed the same validation gates.
- No mandatory telemetry or automatic upload of source trees, logs, device identifiers, or artifacts.

## Architecture acceptance criteria

The foundation is acceptable when:
1. The app can inspect the current device without root and clearly report unknown values.
2. A device manifest can be parsed and schema-validated with actionable errors.
3. A build plan resolves only pinned, reviewed inputs.
4. Jobs can report states, persist logs locally, cancel safely, and recover from interruption.
5. Unsupported operations fail closed with an explanation.
6. No operation writes to device partitions.
7. Documentation accurately labels every capability as proposed, implemented, tested, or hardware-verified.
