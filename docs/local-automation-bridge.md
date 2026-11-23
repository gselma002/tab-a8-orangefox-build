# Local automation bridge — security-first design proposal

## Status and promise boundary

This is a design proposal, not an implemented feature. It does not give this ChatGPT conversation a live connection to the tablet. A local agent can execute approved work on-device, but ChatGPT can submit or inspect that work only if the ChatGPT product exposes a supported connector/action that can securely reach the agent. No such connection is assumed here.

The project must never claim hands-free ChatGPT control until an end-to-end integration has been implemented and independently tested.

## Goals

- Keep the primary execution and data plane on the SM-X200.
- Let the user initiate, approve, pause, cancel, and inspect jobs on the tablet.
- Support repeatable diagnostics, source preparation, app builds, tests, and artifact inspection.
- Avoid root, Shizuku, raw block-device access, and broad storage permissions for the initial feature.
- Keep logs, source trees, device identifiers, and build artifacts local unless the user explicitly exports them.
- Fail closed when an operation is unknown, unsupported, expired, or unauthorized.

## Non-goals

- A general-purpose remote shell, remote desktop, or unattended device takeover.
- Arbitrary command strings supplied by a remote party.
- A publicly reachable HTTP server or open listening port on the tablet.
- Automatic flashing, partition writes, formatting, bootloader changes, or recovery installation.
- A claim that Termux/PRoot isolates hostile build scripts.
- A mandatory cloud relay or remote build worker.

## Recommended component model

1. **Android control app:** pairing state, job queue, approvals, progress, cancellation, audit history, and export previews. The app owns policy and approval UI.
2. **Local job broker:** accepts typed, versioned job requests; validates schema, policy, resource budget, and expiry; creates a job record; returns a job ID. It exposes no arbitrary shell endpoint.
3. **Execution adapter:** starts only predefined local task handlers. Termux integration, if used, is optional and explicitly configured. Invoke fixed scripts or argument arrays, never concatenated shell text.
4. **Job handlers:** narrowly scoped operations such as device probe, environment inventory, repository status, pinned-source checkout, unit tests, Gradle app build, and APK metadata/hash inspection.
5. **Local audit store:** append-only job events with timestamps, request IDs, handler/version, consent decision, exit status, redaction summary, and output hashes. Keep detailed logs app-private by default.
6. **Optional ChatGPT integration boundary:** a future supported connector may submit typed jobs and retrieve redacted results. Until a supported authenticated transport exists, the user starts jobs in the app and can explicitly export results. Do not fake this integration with an exposed local port or an untrusted relay.

## Execution and privilege model

- The broker accepts a registered operation ID plus a strictly validated parameter object, not a command line.
- Each handler has a static executable path, fixed argument construction, constrained working directory, environment allowlist, timeout, output-size cap, and cancellation policy.
- Never evaluate shell metacharacters, downloaded scripts, or remote-provided shell snippets.
- Do not run as root. Do not request Shizuku for the initial feature. If a later feature genuinely needs Shizuku, isolate it as a separately reviewed capability with a narrow operation list and fresh on-device approval.
- Do not expose root, raw block-device, flashing, formatting, repartitioning, or bootloader-changing operations through the broker.
- Build handlers execute untrusted repository build scripts with the local user's available privileges. The UI must warn that Termux/PRoot is not a security sandbox; recommend reviewing source and using a disposable workspace with minimal accessible data.
- Enforce storage and memory preflight, thermal checks, timeouts, cancellation, and process cleanup. Do not promise reliable background execution until tested on Android 14 / One UI 6.1.

## Approval policy

- **Class A — read-only, low sensitivity:** basic device/build-tool inventory and local job status. May run after the user explicitly enables the feature; redact serials, account identifiers, private paths, and tokens.
- **Class B — local file or build changes:** clone/update a repository, create build outputs, run tests, or clean a project-owned temporary directory. Require visible per-job approval showing exact repository, pinned revision, workspace, expected file changes/network access, storage budget, and output destination.
- **Class C — privileged, destructive, boot/security, or personal-data operations:** not supported by this bridge. No approval button should make these operations available.

A remembered approval may cover only a narrowly defined repeatable Class A task. It must not silently expand to Class B or C.

## Pairing and communication

### Initial version

- No remote pairing and no network listener.
- Pair the app to its local execution adapter using Android's explicit package/intent permissions or another documented OS-mediated mechanism.
- Verify the calling package/UID where supported; do not trust caller-supplied package names.
- Keep pairing secrets in Android Keystore-backed storage where feasible; never place them in source control, logs, clipboard, or exported diagnostics.
- Provide a visible disconnect/revoke control and rotate credentials when pairing is reset.

### Future remote connector — only if a supported ChatGPT integration exists

- Prefer an outbound-only, mutually authenticated connection or a vetted connector transport; never open an unauthenticated inbound port on the tablet.
- Use TLS, short-lived scoped credentials, request expiry, unique request IDs, nonce/replay protection, and strict rate/size limits.
- Bind each credential to permitted operation IDs and maximum data disclosure.
- Require on-device approval for every Class B job. The remote side cannot approve on the user's behalf.
- Return only redacted structured status by default. Logs and artifacts require a separate explicit export action and preview.
- Provide immediate disconnect, credential revocation, and a local kill switch.
- If the transport cannot provide confidentiality, request authentication, replay defense, and revocation, do not implement it.

## Job protocol

A request is a versioned structured object containing a unique request ID and expiry, a registered operation ID, typed parameters with strict limits, requested output fields, and (where applicable) an expected repository URL and immutable commit ID.

The broker responds with a job ID and a state from:
- RECEIVED
- VALIDATING
- AWAITING_APPROVAL
- QUEUED
- RUNNING
- CANCELLING
- SUCCEEDED
- FAILED
- CANCELLED
- EXPIRED
- REJECTED

Terminal records include exit code, start/end timestamps, handler version, output hashes, and a bounded diagnostic summary. State transitions are validated; a terminal job cannot restart under the same request ID.

## Privacy and retention

- No telemetry, automatic upload, or cloud account in the initial version.
- Store data app-private; do not scan unrelated files or collect contacts, messages, location, microphone, camera, or accessibility data.
- Redact serial numbers, Android IDs, tokens, usernames, email addresses, private absolute paths, and environment secrets from default output.
- Keep raw logs only as long as needed for local debugging; offer configurable retention and a clear-all action.
- Export previews show exact filenames, destination, and content categories before confirmation.
- Never upload source trees or APKs by default.

## Reliability requirements

- Persist job state before launching work.
- Bound stdout/stderr and disk consumption; stream output into rotating app-private log files.
- Support cancellation and process-tree cleanup where Android permits; report if cleanup cannot be confirmed.
- Recover interrupted jobs as INTERRUPTED or FAILED, never silently mark them successful.
- Do not automatically resume a build or rerun a command after process death unless the handler declares idempotency and the user has opted in.
- Capture app version, Android version, device model, tool versions, and pinned source revision in each diagnostic record, subject to privacy redaction.

## Implementation stages and gates

### B0 — threat model and protocol
Define the operation registry, schema, state machine, risk classes, redaction rules, and threat model. Add tests for malformed/expired/replayed requests, invalid transitions, output limits, and secret redaction. No command execution yet.

### B1 — local read-only broker
Implement device/tool inventory and job status only. No Termux integration, network listener, root, or Shizuku. Demonstrate on SM-X200 and inspect Android permissions/exported components.

### B2 — controlled Termux adapter
Use documented Termux integration with explicit user enablement. Run one fixed harmless diagnostic handler with a fixed argument array. Test cancellation, hostile parameters, quoting, process death, and Android 14 behavior. Do not run arbitrary repository scripts yet.

### B3 — approved project jobs
Add Git status, pinned checkout, tests, and Android app build as separate handlers. Per-job approval includes exact inputs and output paths. Test with a disposable repository and verify no out-of-workspace writes.

### B4 — optional ChatGPT connector
Proceed only if a supported ChatGPT connector/action can securely reach the agent. Complete protocol review, independent security review, revocation tests, and explicit consent UX. Until then, retain local-only operation and manual export.

### B5 — independent validation
Test on the exact SM-X200 firmware and Android build. Verify permissions, network behavior, data retention, redaction, cancellation, reboot recovery, and failure-closed behavior. Publish evidence and limitations; do not claim full remote control unless tested end to end.

## Acceptance criteria

The bridge is not ready for general use until:
1. The user can see and revoke every integration and permission.
2. No arbitrary remote shell exists.
3. All operations are registered, typed, bounded, auditable, and risk-classified.
4. Class B work requires exact per-job on-device approval.
5. No Class C operation is exposed.
6. No private data leaves the device by default.
7. Cancellation, process death, replay, expiry, and malformed-input tests pass.
8. The actual SM-X200 passes on-device tests.
9. Any ChatGPT connection is proven through a supported product integration, not assumed from Shizuku, Termux, or localhost.
