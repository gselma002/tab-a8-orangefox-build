# Galaxy Tab A8 recovery development workspace

An experimental, Android-device-first project for researching, validating, and eventually building custom recovery software. The initial reference device is the Samsung Galaxy Tab A8 Wi-Fi (SM-X200 / `gta8wifi`).

## Core requirement

The primary workflow is intended to run on the Android device itself. A PC, second phone, or remote build service must not be a required dependency. Local full-source recovery compilation is **not yet demonstrated** on the SM-X200; the project will measure real toolchain, storage, memory, and thermal constraints before claiming support.

## Current status

This repository contains engineering documentation, experimental build material, and an initial read-only Android probe source prototype. The app has **not** yet been compiled, installed, or tested on the SM-X200. There is no validated local recovery build engine or recovery image verified on the target tablet.

- [Project source index](docs/project-sources.md) — source authority, provenance, current implementation facts, and validation vocabulary.
- [Device baseline](docs/source-device-baseline.md) — recorded SM-X200 facts with provenance and revalidation requirements.
- [Engineering decisions and history](docs/source-engineering-decisions.md) — consolidated project decisions and unresolved questions.
- [Implementation and validation register](docs/source-validation-register.md) — evidence-based implementation and test status.
- [Technical research and compatibility](docs/source-technical-research.md) — Android, Termux, Shizuku, and recovery research leads.
- [Architecture](docs/architecture.md) — component boundaries and Android execution constraints.
- [Device validation](docs/device-validation.md) — evidence levels and compatibility requirements.
- [Security model](docs/security-model.md) — least privilege, untrusted build inputs, and privacy defaults.
- [Local automation bridge](docs/local-automation-bridge.md) — security-first design proposal; not yet implemented and not a live ChatGPT connection.
- [Termux build path](docs/termux-build-path.md) — candidate same-tablet app-build setup, not yet validated on the SM-X200.
- [Roadmap](docs/roadmap.md) — acceptance-gated implementation milestones.

## Safety

A successful compile does not prove that a recovery image is bootable or compatible with a particular firmware build. Static inspection does not prove hardware operation. Encryption/decryption support must be tested against the exact firmware and data configuration.

The initial implementation is read-only with respect to device partitions. It must not flash, format, erase, repartition, change bootloader settings, or write raw block devices.

## Experimental CI

An experimental GitHub Actions build proposal is tracked separately in [PR #1](https://github.com/gselma002/tab-a8-orangefox-build/pull/1). It is not the Android-first product implementation and must not be treated as proof that builds can run locally on the tablet. Do not flash an artifact based only on CI success.
