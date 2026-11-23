# Security and privacy model

## Security goals

- Preserve the device's bootability and the user's data.
- Minimize permissions and avoid root requirements.
- Keep source trees, logs, device information, and artifacts local by default.
- Make all privileged or destructive capabilities absent from the initial implementation.
- Treat third-party recovery sources, device trees, patches, build scripts, and downloaded artifacts as untrusted until reviewed.

## Threats and limits

Build scripts execute code. A malicious or compromised device tree can read files accessible to the process, alter outputs, or attempt network access. Running a build in Termux or PRoot does not make hostile scripts safe; PRoot is not a strong isolation boundary. The Android app sandbox limits app access relative to other apps, but it does not make arbitrary build scripts harmless when they run with the app's own UID and permissions.

The project must state these limits plainly and must not market process separation as a security sandbox without a demonstrated isolation boundary.

## Least privilege

- No root by default.
- No Shizuku dependency for read-only core functionality.
- Request Android permissions only when a specific user-visible feature needs them.
- Use Storage Access Framework or app-private storage where appropriate; do not request broad storage access as a shortcut.
- Never request credentials, signing keys, screen locks, or account tokens for ordinary local inspection.
- Never store secrets in manifests, source control, build logs, or artifacts.
- Do not automatically execute downloaded scripts or newly cloned repositories.

## Network and supply-chain controls

- Pin source repositories to immutable commit IDs for release builds.
- Verify downloaded archives and artifacts with recorded cryptographic hashes.
- Do not suppress failed dependency or Git LFS downloads.
- Record source URL, commit, patch set, toolchain identity, and build configuration.
- Minimize network access and explain every external source.
- Do not use floating tags or branches as the sole provenance for a released build.
- Treat upstream release checksums as claims until independently verified against downloaded bytes.

## Safe operation policy

The initial implementation is read-only with respect to device partitions. It must not:
- flash, sideload, format, erase, repartition, or write raw block devices;
- change bootloader or verified-boot settings;
- patch boot images or alter vbmeta;
- run commands through Shizuku or root without a separately reviewed feature and explicit user action.

Any future operation that can affect bootability or user data requires a dedicated threat model, exact-device preconditions, clear consequences, independent validation, explicit confirmation, and a recovery/rollback plan. A generic confirmation dialog is not sufficient.

## Privacy defaults

- No telemetry by default.
- No automatic uploads of logs, source trees, device properties, crash reports, or build artifacts.
- No mandatory cloud account.
- Any future export or upload must preview exactly what will be sent and require explicit user action.
- Redact sensitive paths, serial numbers, account identifiers, and tokens from diagnostics.

## Reporting

For each operation, report requested permissions, data accessed, network activity, files created, and whether the operation changed device state. Distinguish an operation that was attempted from one that completed and from one that was independently verified.
