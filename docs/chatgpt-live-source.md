# SM-X200 — Live Project Source Pointer

**Purpose:** This is the small, uploadable entry point for the ChatGPT Project. It points to the maintained engineering sources in GitHub so future work can retrieve their current contents instead of trusting an old uploaded copy.

## Canonical repository

- Repository: https://github.com/gselma002/tab-a8-orangefox-build
- Documentation branch: `architecture/android-first-foundation`
- Pull request: https://github.com/gselma002/tab-a8-orangefox-build/pull/2
- Master index: https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/project-sources.md

## Required source-refresh behavior

At the start of substantive project work:

1. Retrieve the current repository/branch state from GitHub, using the connected GitHub app when available. GitHub access is on-demand; it is not a continuously synchronized Project index.
2. Read the current master index and the relevant source documents from the branch above. Do not rely on remembered contents or assume this uploaded pointer contains the latest engineering facts.
3. Record the branch name, latest commit SHA, retrieval date, and exact files consulted in the work notes when freshness matters.
4. Treat repository source and dated evidence as authoritative over this pointer. If GitHub cannot be accessed, say so and clearly label any answer based on this uploaded pointer or prior context as potentially stale.
5. Do not silently switch to `main` or another branch. PR #2 is not automatically approved or merged; do not merge it or either project PR without explicit user authorization.
6. When proposing source improvements, update the canonical repository documents first (with authorization), validate links and consistency, then tell the user whether the Project upload needs replacing.

## Maintained source documents

- [Master index](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/project-sources.md)
- [Device baseline](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-device-baseline.md)
- [Engineering decisions and history](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-engineering-decisions.md)
- [Implementation and validation register](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-validation-register.md)
- [Technical research and compatibility](https://github.com/gselma002/tab-a8-orangefox-build/blob/architecture/android-first-foundation/docs/source-technical-research.md)

## What “live” means—and does not mean

This file does **not** update itself, and uploading it does **not** create automatic GitHub-to-Project synchronization. It is a stable pointer and operating rule. When ChatGPT has authorized GitHub access, it can retrieve permitted repository content on demand and see later commits. The user may need to connect GitHub and authorize this repository in ChatGPT settings. If live retrieval is unavailable in a particular chat or product surface, use the links to retrieve the current files manually or replace the uploaded source snapshots.

ChatGPT Project Sources currently document connected app links for Google Drive and Slack; ordinary uploaded files are snapshots. Do not claim that a GitHub URL added as a normal Project source will stay synchronized. See the official guidance:
- https://help.openai.com/en/articles/11145903-connecting-github-to-chatgpt
- https://help.openai.com/en/articles/10169521-using-projects-in-chatgpt

## Project safety invariants

- Target device: Samsung Galaxy Tab A8 Wi-Fi SM-X200 (gta8wifi).
- Primary workflow must run on the tablet; a computer or second phone is not a required dependency.
- Shizuku is not root.
- No flashing, partition writes, formatting, bootloader changes, or destructive actions without explicit authorization.
- Separate proposed, implemented, compiled, tested, installed, device-tested, and hardware-verified status.
- Treat historical device values and old test reports as historical until revalidated.
- Keep experimental recovery build work separate from the Android-first architecture/security foundation.
