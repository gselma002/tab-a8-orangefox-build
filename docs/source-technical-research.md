# Technical research and compatibility register

**Classification:** Research leads and engineering constraints; re-check upstream sources before consequential use.  
**Target:** Samsung Galaxy Tab A8 Wi-Fi SM-X200 (gta8wifi).

## Android and same-device development

The project separates two distinct feasibility questions:

1. Building and testing the Kotlin Android application on the tablet.
2. Building full OrangeFox/TWRP recovery sources on the tablet.

The first is a smaller experiment; the second has substantially different compiler, host-tool, libc, memory, storage, and thermal requirements. Never infer recovery-build feasibility from Android app-build success.

Candidate Termux app-build guide:
https://github.com/GeoFFerDev/Termux-Based-Native-Builder

The repository's termux-build-path.md records the candidate OpenJDK/Gradle/AGP/SDK/aapt2 approach. Treat versions and paths as candidate configuration until independently reproduced on the current SM-X200. Do not blindly execute setup scripts or hard-code machine-specific SDK paths into shared project files.

Official Gradle wrapper documentation:
https://docs.gradle.org/current/userguide/gradle_wrapper.html

Generate the wrapper from a trusted Gradle distribution and verify the wrapper JAR/distribution checksums. Do not download wrapper binaries from arbitrary mirrors.

## Android platform and privilege model

- Android uses the Bionic C library; conventional Linux host binaries may not execute unchanged in Termux.
- Termux and PRoot are not strong isolation boundaries for untrusted build scripts.
- Shizuku provides an IPC-mediated shell-level capability for supported operations; it is not root.
- Android app sandboxing does not make arbitrary scripts harmless when they run with the app's own UID and granted permissions.
- Use app-private storage and Storage Access Framework where appropriate; avoid broad storage permissions as a shortcut.
- Android background execution, foreground service, process lifecycle, memory, and thermal constraints must be tested against the target API level.

Official documentation to consult at implementation time:
- Android application sandbox: https://source.android.com/docs/security/app-sandbox
- Android storage: https://developer.android.com/training/data-storage
- Android background work: https://developer.android.com/develop/background-work
- Termux project: https://github.com/termux/termux-app
- Shizuku project/documentation: https://github.com/RikkaApps/Shizuku

## Recovery source leads

## Research status and version pinning

The links and project summaries in this document are research entry points, not a claim that every upstream page or repository revision was freshly audited on the document's creation date. Before adopting a dependency, command, device tree, kernel, patch, or recovery image:
1. Open the upstream source directly and record the check date.
2. Pin a tag or immutable commit SHA; branches such as `fox-12.1` are mutable.
3. Inspect licenses, provenance, release assets, build scripts, and all privileged/destructive operations.
4. Compare Android version, kernel ABI/configuration, firmware, partition model, encryption implementation, and hardware variants.
5. Record the exact files inspected and distinguish source review from compilation, static image inspection, boot testing, and functional hardware testing.
6. Recheck advisories and dependency compatibility before release.

A URL being present here does not imply endorsement, safety, freshness, or compatibility. Do not treat a GitHub release's description as independent test evidence.

## Compatibility matrix

| Dimension | Required evidence | Current status |
|---|---|---|
| Exact model/variant | Settings/device identifiers and regional variant | Historical SM-X200 target; recheck |
| Firmware/build | Current build ID and security patch | Historical X200XXS6DYG6; recheck |
| Kernel | Exact kernel source/config and runtime release | Runtime release historically reported; source/config not established |
| Boot image format | Header/version and partition/slot evidence | Unknown |
| AVB/vbmeta | Current verified-boot configuration and signing chain | Unknown |
| Encryption | FBE/metadata encryption and keymaster dependencies | Unknown |
| Display/touch | Panel and touch-controller identification plus driver support | Unknown |
| USB/ADB | Actual recovery-mode enumeration and input/output tests | Unknown |
| Recovery functions | Boot, mount, backup, restore, decryption tests | Not verified |

Unknown cells are blocking questions, not permission to assume a default.

These repositories are research inputs, not validated builds:

| Source | Known context | Required caution |
|---|---|---|
| https://github.com/Magendanz/android_device_samsung_gta8wifi | Historical SM-X200 tree associated with Android 11 | Old tree; inspect branch, kernel, encryption, and current firmware compatibility |
| https://github.com/SavedByLight/android_device_samsung_gta8wifi_nokernel | Experimental no-kernel device tree | Previously observed potentially destructive data-format/encryption procedures; do not follow without independent review |
| https://github.com/encipherdev/android_recovery_samsung_gta8wifi | OrangeFox device source, branch fox-12.1 | Community source; inspect exact commit, kernel, partition assumptions, and build instructions |
| https://github.com/encipherdev/android_recovery_samsung_gta8wifi/releases/tag/R12.0_1 | Community release previously identified | Published claims are not proof of compatibility with X200XXS6DYG6 |

A device tree that targets the same marketing model may still differ in firmware, panel/touch controller, partition layout, boot image header, kernel, AVB configuration, and encryption support.

## Recovery compatibility checklist

Before any image is considered for a test:
- Confirm exact model and regional variant.
- Pin device tree, recovery source, kernel, proprietary blobs, and patches to immutable revisions.
- Inspect partition layout and boot/recovery installation model using authoritative, read-only evidence.
- Inspect boot image header, AVB/vbmeta configuration, rollback behavior, and signing assumptions.
- Review all scripts for formatting, erasing, raw block writes, data decryption changes, and bootloader modifications.
- Verify display and touchscreen drivers for the actual panel/controller variant.
- Review FBE/metadata encryption, keymaster/gatekeeper dependencies, and Android 14 compatibility.
- Build from pinned inputs and record artifact SHA-256.
- Perform static image inspection separately from boot and hardware tests.
- Require an explicitly authorized, reversible test plan before any boot or flash operation.

No published recovery should be called compatible until the required exact-device tests have passed.

## Source verification policy

For each upstream reference, record:
- URL and repository owner.
- Branch/tag and immutable commit SHA.
- Date checked.
- License and provenance where relevant.
- Relevant files and configuration findings.
- Whether the finding is source-inspected, build-tested, statically inspected, boot-tested, or hardware-tested.
- Known differences from the user's current firmware.

Do not assume current upstream state from old conversation summaries. Refresh sources before relying on them.

## Privacy and supply chain

Do not publish device serials, Android IDs, account identifiers, Wi-Fi details, credentials, signing keys, or unredacted logs. Pin dependencies and verify hashes. Treat third-party scripts, archives, device trees, patches, and build artifacts as untrusted until reviewed.
