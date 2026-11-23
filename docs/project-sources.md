# Project source index

This document is the navigation and provenance index for the SM-X200 Android Recovery, Security & Automation work. It is not a substitute for inspecting the current source tree, device, or test results.

## Project identity

- Target: Samsung Galaxy Tab A8 Wi-Fi, SM-X200 (gta8wifi).
- Primary repository: https://github.com/gselma002/tab-a8-orangefox-build
- Product constraint: the primary workflow must operate on the tablet itself; a PC, second phone, or remote build worker is not a required dependency.

## Source authority

Use evidence in this order, while considering both authority and freshness:

1. Current repository source, immutable commits, tests, and workflow results.
2. Reproducible observations from the actual SM-X200, with date, command/method, firmware, and result.
3. Official Android/AOSP, Linux, Samsung, Termux, Shizuku, and upstream recovery documentation/source.
4. Dated project decisions and validation records.
5. Community device trees, unofficial builds, forum posts, and experimental workflows.
6. Unverified assumptions and recollections.

A newer source does not automatically outrank a more authoritative source. Mark conflicts explicitly and resolve them using direct evidence.


## Curated Project Sources

These five maintained documents are intended to be added to ChatGPT Project Sources (or uploaded as files if the project UI does not support linking repository files):

1. [Project Sources Master Index](project-sources.md) — navigation, source authority, project boundaries, and maintenance rules.
2. [Device Baseline](source-device-baseline.md) — historical device facts, provenance warnings, and a safe revalidation template.
3. [Engineering Decisions and History](source-engineering-decisions.md) — consolidated decisions, rationale, prior recovery research, and unresolved questions.
4. [Implementation and Validation Register](source-validation-register.md) — evidence-based status of code, tests, builds, installation, and hardware verification.
5. [Technical Research and Compatibility](source-technical-research.md) — Android/Termux/Shizuku constraints, upstream references, and recovery compatibility gates.

The five documents are maintained in the repository under `docs/`. If the ChatGPT project accepts only file uploads rather than repository links, download these five Markdown files and upload them. Keep the repository versions authoritative and refresh uploaded copies after material changes. A URL in this index does not imply automatic synchronization.

## Repository navigation

## Source governance and synchronization

Each source document must carry its classification, scope, last-reviewed date, and evidence limitations. Record the exact commit SHA when a claim depends on repository code. Project Sources uploads are snapshots unless the ChatGPT product explicitly provides a supported live-link feature; do not assume GitHub changes synchronize into uploaded files.

Review all five documents when any of these occur: firmware/Android update; repository architecture or PR change; new test/build/device evidence; security incident or newly identified threat; change in recovery source/branch; or a milestone transition. Prefer correcting the authoritative source first, then refresh the Project upload.

Keep the collection small and non-duplicative. The master index should point to detailed sources rather than copying entire sections. Do not put secrets, raw personal device identifiers, access tokens, signing keys, or unredacted diagnostic output in Project Sources.

## Conversation-history coverage and limits

The engineering history document is a curated synthesis of project conversations available in the working context, not a complete export or verbatim archive of every account chat. Conversation titles alone do not establish relevance. Include a conversation only when its technical decisions, device evidence, commands, or constraints materially affect this project. Preserve uncertainty where the original command output or full transcript is unavailable. The user remains the authority for whether a conversation should be moved into the ChatGPT Project.

- [README](../README.md) — current project scope and validation status.
- [Architecture](architecture.md) — Android-first component boundaries and local-build feasibility.
- [Security model](security-model.md) — threat model, least privilege, supply-chain, and privacy defaults.
- [Device validation](device-validation.md) — evidence levels and exact-device compatibility gates.
- [Local automation bridge](local-automation-bridge.md) — proposed local job broker; not a live ChatGPT-to-device connection.
- [Engineering roadmap](roadmap.md) — milestones and acceptance criteria.
- [Termux build path](termux-build-path.md) — candidate on-device app build setup; not yet proven on the SM-X200.

## Pull-request boundaries

- [PR #1 — experimental recovery build workflow](https://github.com/gselma002/tab-a8-orangefox-build/pull/1). Keep experimental and separate from the Android-first application/security foundation.
- [PR #2 — Android-first architecture and security foundation](https://github.com/gselma002/tab-a8-orangefox-build/pull/2). Contains the app/protocol/storage prototypes and engineering documentation.

Do not merge either pull request without explicit owner authorization. Refresh the live PR state before making claims or changes.

## Current implementation facts

The Android-first branch contains:
- a minimal read-only Android device-probe source prototype;
- pure Kotlin job request validation and state-machine primitives;
- an app-private SQLite job metadata-store prototype;
- architecture, security, device-validation, local-bridge, Termux, and roadmap documentation.

The job store has been updated to persist both workspace and artifact IDs, reject duplicate request IDs, use compare-and-set state transitions with nondecreasing update timestamps, and support a v1-to-v2 schema migration. These changes are source-level only until Android compilation, database tests, migration tests, and actual tablet validation have run.

There is no implemented command runner, complete broker, Termux/Shizuku integration, network listener, or ChatGPT-to-device transport. The local bridge document is a design proposal, not a functioning feature.

## Device baseline and provenance

Previously reported reference values:
- Model: SM-X200.
- Codename: gta8wifi.
- Android 14 / One UI 6.1.
- Firmware: X200XXS6DYG6.
- Kernel: 4.14.199.
- SoC/platform: Unisoc Tiger T618 / ums512.
- Shizuku: shell-level authorization; not root.
- Termux: installed.

Treat these as historical observations, not guaranteed current state. Recheck the actual tablet before a device-dependent operation. Do not infer root, partition layout, encryption configuration, AVB behavior, or recovery compatibility from these values.

## Validation vocabulary

Report each status separately:
- **Proposed:** design only.
- **Implemented:** source/configuration changed.
- **Compiled:** relevant build completed.
- **Unit-tested:** relevant automated unit tests passed.
- **Integration-tested:** connected components passed integration tests.
- **Installed:** installed on the SM-X200.
- **Device-tested:** behavior observed on the actual tablet.
- **Hardware-verified:** the specifically named hardware behavior passed.

Do not infer one status from another. Attach commit IDs, commands, logs, test reports, artifact hashes, device firmware, and dates when available.

## Safety and privacy rules

- No flashing, partition writes, formatting, bootloader changes, or destructive operations without explicit authorization.
- Never assume Shizuku is root.
- Treat repositories, build scripts, device trees, patches, archives, and generated artifacts as untrusted until reviewed.
- Keep credentials, signing keys, personal identifiers, and unnecessary device data out of Git and logs.
- Do not upload source trees, logs, APKs, or device information automatically.
- Keep experimental recovery work separate from validated Android app/security work.

## Maintenance

Update this index when architecture, milestone status, PR boundaries, or validation facts materially change. Include the source and date for new device observations. Preserve historical evidence, but label superseded facts rather than silently presenting them as current.
