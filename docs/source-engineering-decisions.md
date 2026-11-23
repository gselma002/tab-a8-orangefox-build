# Engineering decisions and project history

**Classification:** Consolidated record from available project conversation summaries and repository state. Not a verbatim transcript.  
**Purpose:** Preserve decisions and their rationale so future work does not repeat unsafe or superseded recommendations.

## Product constraints

1. The primary workflow must operate directly on the Samsung Galaxy Tab A8 Wi-Fi SM-X200.
2. A computer, second phone, or remote build worker must not be a required dependency.
3. Security, privacy, reliability, efficiency, and maintainability take precedence over convenience.
4. No flashing, partition writes, formatting, bootloader changes, or other destructive actions without explicit authorization.
5. Shizuku must never be described as root.
6. Do not claim a build, installation, test, or hardware behavior succeeded without evidence.
7. Keep experimental recovery work separate from the Android-first application/security foundation.

## Repository workstreams

Repository: https://github.com/gselma002/tab-a8-orangefox-build

- PR #1: experimental recovery build workflow. Keep separate; do not merge without explicit authorization.
- PR #2: Android-first architecture and security foundation. Keep draft/unmerged unless explicitly authorized.

Always refresh repository and PR state before relying on historical descriptions.

## Decisions and rationale

### Android-first, same-device architecture

The tablet is the primary execution target. A cloud or hosted workflow may be investigated only as optional support; it must not be represented as local execution or become a required dependency.

### Distinguish Android app build from recovery source build

A small Kotlin Android app and a full OrangeFox/TWRP source build have different toolchains, resource demands, and host assumptions. Evidence that the app compiles in Termux does not prove recovery sources can compile there.

### Least privilege and read-only first

Begin with device inventory and typed, allowlisted operations. Do not assume root or require Shizuku for core read-only functionality. No arbitrary shell, raw block-device access, network listener, or automatic flashing is part of the initial foundation.

### Shizuku is shell-level access, not root

Prior device interaction showed a shell identity (UID 2000) and permission-denied access to a raw block device. This is consistent with the rule that Shizuku does not grant root. Do not retry restricted block access through alternate paths or treat a symlink listing as permission to read the target.

### Local job storage is not a broker

The SQLite store persists bounded metadata and state. It does not by itself authorize operations, resolve workspace/artifact identifiers, execute commands, prevent all replay across a distributed protocol, or provide a ChatGPT connection. User approval must be enforced by a real user-facing broker flow; a boolean method parameter is not a security boundary.

### Recovery compatibility is evidence-gated

A device tree, community release, successful compile, or static image inspection cannot prove exact-firmware bootability, touchscreen support, encryption/decryption, or backup/restore functionality. Require exact model/firmware evidence and safe, separately authorized test procedures.

### Preserve existing work

Inspect repository state and existing code before changes. Prefer targeted changes and tests over rewrites. Keep PR #1 and PR #2 separate. Never merge either without explicit authorization.

## Historical recovery research

Previously considered sources include:

- Magendanz SM-X200 device tree: https://github.com/Magendanz/android_device_samsung_gta8wifi — historical Android 11-era source; not a validated Android 14 recovery.
- SavedByLight no-kernel tree: https://github.com/SavedByLight/android_device_samsung_gta8wifi_nokernel — experimental and includes potentially destructive data/encryption behavior; do not follow its procedures without independent review.
- Encipherdev OrangeFox tree: https://github.com/encipherdev/android_recovery_samsung_gta8wifi — community source, branch fox-12.1; requires exact source and firmware review.
- Community release previously identified: https://github.com/encipherdev/android_recovery_samsung_gta8wifi/releases/tag/R12.0_1 — existence/claims previously reported, not independently validated for the current tablet state.

These are leads for review, not endorsements or safe-to-flash recommendations.

## Unresolved decisions and questions

- What exact Android app build path works on the SM-X200 with its current Termux environment?
- Does the current Android project compile and pass its actual Gradle/JUnit tests?
- Does the SQLite v1-to-v2 migration work using Android SQLiteOpenHelper on-device?
- What are the current partition layout, boot chain, AVB configuration, encryption behavior, and touchscreen controller details?
- Can any complete recovery build system run locally within the tablet's measured resource limits?
- What supported product integration, if any, could securely connect ChatGPT to a local agent? No such connection is currently implemented.

## Change control

When a decision changes, append a dated entry with: previous decision, new decision, reason, evidence, affected files/PRs, and required follow-up. Do not silently rewrite history.
