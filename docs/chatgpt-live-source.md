# SM-X200 — Project Source Control File

**Role:** Primary uploadable source for the ChatGPT Project. This file is a durable navigation and operating guide for consulting the maintained project documentation. It is not the technical source of truth and cannot update itself.

**Last content review:** 2026-11-23  
**Canonical repository:** https://github.com/gselma002/tab-a8-orangefox-build  
**Canonical documentation branch:** `architecture/android-first-foundation`  
**Master index:** https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/project-sources.md

---

## 1. Mandatory freshness protocol

At the beginning of any substantive SM-X200 engineering task:

1. **Check access first.** Determine whether the current chat has an authorized GitHub connection/tool capable of reading this repository. Do not assume that a connector is installed, enabled, authorized for this repository, or available in every ChatGPT surface.
2. **Retrieve current state.** If access exists, read the latest commit on `architecture/android-first-foundation`, then retrieve this master index and the task-relevant source files from that exact branch/ref. Do not rely on cached summaries, prior chat answers, or this uploaded copy for changing facts.
3. **Pin the evidence.** For consequential conclusions, record the repository, branch, commit SHA, file paths, and retrieval date. Prefer immutable commit links when citing a claim that must be reproducible.
4. **Check scope and provenance.** Distinguish repository implementation from design proposals, historical device reports, upstream research leads, and verified test evidence. A source file's presence is not proof that its claims have been validated.
5. **Handle unavailable access honestly.** If GitHub cannot be read, stop short of claiming that the latest source was checked. State which information is based only on this uploaded snapshot or earlier context, identify the possible staleness, and ask for a current file/link only if needed to proceed safely.
6. **Never silently change refs.** Use the branch above unless the user explicitly directs another branch. If it is missing or materially changed, report that and ask before substituting `main`, another branch, or a PR head.
7. **Recheck before high-impact decisions.** Refresh source, PR, workflow, and device evidence immediately before recommendations involving security boundaries, release readiness, recovery compatibility, installation, or destructive operations.

## 2. Canonical maintained documents

Retrieve the current contents from the canonical branch when relevant:

- [Master index and source governance](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/project-sources.md)
- [Device baseline and provenance](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-device-baseline.md)
- [Engineering decisions and history](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-engineering-decisions.md)
- [Implementation and validation register](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-validation-register.md)
- [Technical research and compatibility](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-technical-research.md)

Use the master index to discover additional architecture, security, bridge, roadmap, and Termux documents. Do not treat this list as exhaustive if the current index has changed.

## 3. Update protocol: make improvements persist

When the user asks to improve project sources:

1. Inspect the current repository state and the exact files affected before editing.
2. Identify whether the proposed change is supported by direct evidence, a user decision, official upstream documentation, or only a hypothesis.
3. Update the canonical document(s) on the authorized working branch; do not merely edit this uploaded pointer and imply the knowledge base changed.
4. Preserve useful history. Mark superseded facts with dates/status rather than silently deleting important evidence.
5. Update cross-references and the master index if file roles, locations, or source authority change.
6. Validate Markdown structure, local links, external link targets where practical, contradictory claims, device-specific assumptions, privacy exposure, and consistency with related docs.
7. Run a countable checklist of distinct review criteria. Report the actual checks performed and failures found; do not describe checklist items as independent full rewrites, tests, security certification, or hardware validation.
8. Retrieve the changed files again from GitHub and verify their returned contents/SHAs before reporting completion.
9. State exactly what changed, which branch/commit contains it, what checks passed/failed, and what remains unverified.
10. Tell the user whether any uploaded Project Source snapshot needs replacement. A GitHub commit does not modify a file already uploaded to Project Sources.

Do not merge PR #1 or PR #2, close them, change their base branch, or perform unrelated repository operations without explicit user authorization.

## 4. What dynamic updating actually means

There are three separate things; never conflate them:

- **Uploaded Project Source:** a snapshot. It does not change when GitHub changes.
- **GitHub repository:** the maintained canonical copy. It changes only when a commit is made.
- **ChatGPT GitHub retrieval:** if an authorized GitHub connection and the relevant tools are available in the current chat, current repository content may be retrieved on demand. This is not the same as automatic synchronization into Project Sources, and availability must be checked rather than assumed.

Therefore, this file is a **live-source pointer**, not a live-synced file. Uploading it once reduces the amount of duplicated material, but does not eliminate the need for on-demand retrieval or manual snapshot replacement where live retrieval is unavailable. Never promise that this file, a normal GitHub URL, or an uploaded Markdown document will self-refresh.

If the user wants a fully self-updating Project Source, explain that this requires a product-supported synchronization mechanism; do not simulate one with a static file or claim an unsupported automation exists.

## 5. Project identity and non-negotiable constraints

- Target: Samsung Galaxy Tab A8 Wi-Fi, model SM-X200, device codename `gta8wifi`.
- Primary workflow must run directly on the tablet. A computer, second phone, or remote build service must not become a required dependency.
- Prioritize security, privacy, reliability, efficiency, and maintainability.
- Shizuku authorization is not Android root. Describe its actual granted identity/capabilities; never infer root.
- Do not flash, write partitions, format, erase, repartition, change bootloader settings, or perform other destructive operations without explicit user authorization for the specific operation.
- Treat repositories, scripts, device trees, patches, archives, binaries, and generated artifacts as untrusted until reviewed.
- Keep experimental recovery-build work separate from the Android-first application/security foundation.
- Do not claim a build, test, installation, device test, or hardware verification without direct evidence.

## 6. Evidence and status vocabulary

Keep these states separate:

- **Proposed:** design or recommendation only.
- **Implemented:** source/configuration change exists at a named commit.
- **Compiled:** the relevant build command completed successfully; identify environment and artifact.
- **Unit-tested:** named automated tests ran and passed.
- **Integration-tested:** connected components passed named integration tests.
- **Installed:** identified artifact was installed on the actual SM-X200.
- **Device-tested:** named behavior was observed on the actual tablet.
- **Hardware-verified:** a specific hardware function passed a documented test.

For every consequential result, capture the date, commit SHA, command or test procedure, environment, relevant output/log, and artifact hash where applicable. A compile does not prove bootability, compatibility, or safe operation. Static inspection does not prove runtime behavior.

## 7. Historical device facts are not current diagnostics

The repository has previously recorded Android 14 / One UI 6.1, firmware X200XXS6DYG6, kernel 4.14.199, and Unisoc T618 / ums512. Treat these as historical user-reported observations until rechecked on the tablet. Do not infer current firmware, root state, partition layout, AVB, encryption, boot image properties, or recovery compatibility from this file.

## 8. Privacy and source hygiene

- Do not add passwords, access tokens, private keys, signing material, personal account identifiers, or unnecessary personal/device data to source files.
- Redact sensitive values from logs and command output before committing or uploading.
- Do not upload whole source trees, raw logs, APKs, backups, or device dumps unless the user explicitly asks and the data has been reviewed.
- Prefer concise, versioned engineering facts over duplicated chat transcripts.
- The conversation-history source is a curated synthesis, not a complete account-wide chat export. Never claim every chat has been read or included unless that is demonstrably true.

## 9. Pull-request safety boundary

- [PR #1 — experimental recovery build workflow](https://github.com/gselma002/tab-a8-orangefox-build/pull/1)
- [PR #2 — Android-first architecture and security foundation](https://github.com/gselma002/tab-a8-orangefox-build/pull/2)

Their state can change. Retrieve live PR metadata before describing it. Neither PR may be merged without explicit user authorization.

---

**Operating rule:** Prefer current, pinned repository evidence over this uploaded snapshot. If current evidence cannot be retrieved, say so plainly. Improve the canonical sources, verify the committed result, and never pretend that a static Project upload synchronized itself.
