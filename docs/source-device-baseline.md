# SM-X200 device baseline

**Classification:** Historical user-reported baseline; revalidation required before device-dependent operations.  
**Last reviewed:** 2026-11-23  
**Target:** Samsung Galaxy Tab A8 Wi-Fi, model SM-X200, codename gta8wifi.

## Recorded observations

| Property | Recorded value | Evidence status |
|---|---|---|
| Model | Samsung SM-X200 | User-reported; recheck before operation |
| Codename | gta8wifi | Project target designation |
| Android | 14 | Previously reported; recheck |
| One UI | 6.1 | Previously reported; recheck |
| Firmware/build | X200XXS6DYG6 | Previously reported; recheck |
| Kernel | 4.14.199 | Previously reported; recheck |
| SoC | Unisoc Tiger T618 | Reported platform identification |
| Platform | ums512 | Reported platform identifier |
| ABI | Not yet recorded in a dated baseline capture | Unknown until queried |
| Bootloader | Previously observed unlocked | Historical observation only; recheck safely |
| Shizuku | Paired; shell-level execution observed | Not root |
| Termux | Installed | Installed status previously reported |
| Free storage | Approximately 17 GB at one point | Stale measurement; remeasure |

## Interpretation rules

## Evidence provenance and confidence

The values above are historical project-context observations. They were not all captured in one contemporaneous diagnostic session, and this document does not currently attach raw command output or screenshots to each row. Consequently:
- Model and target codename define the intended device, but should still be confirmed in Settings before device-specific work.
- Android/One UI/firmware/kernel and SoC/platform values are historical reports, not live reads.
- The unlocked bootloader observation must be reconfirmed from a trustworthy current device screen/property before any boot-chain analysis; do not change bootloader state to perform verification.
- Shizuku's shell-level behavior is supported by prior command context, including UID 2000 and a permission-denied raw block-device attempt. That evidence supports the non-root warning only; it does not authorize further block access.
- The approximate free-storage figure is explicitly stale and must never be used for capacity planning.

Do not assign numeric confidence scores without a defined rubric. If evidence is missing, use "unknown" rather than inference.

- Do not assume these values remain current after firmware updates, resets, app updates, or configuration changes.
- Do not infer partition names, sizes, slot scheme, AVB/vbmeta configuration, rollback behavior, encryption implementation, touchscreen controller, or recovery compatibility from the model alone.
- Shizuku authorization does not imply root. Shell UID and root UID are distinct security identities.
- A successful read of a sysfs/property value does not establish that a recovery image can boot or decrypt user data.
- Do not collect or publish serial numbers, Android IDs, account identifiers, Wi-Fi identifiers, or other unnecessary device identifiers.

## Revalidation record template

For each new capture, record:

- Capture date/time and timezone.
- Exact model and build fingerprint (redact unique identifiers).
- Android release, API level, One UI version, build ID, security patch level.
- Kernel release and ABI list.
- Termux version/source and package versions when relevant.
- Shizuku version and effective UID for a specific command, if relevant.
- Available storage and memory at the time of a build experiment.
- Read-only command or UI method used and its exit status.
- Raw evidence location, with sensitive values redacted.
- Reviewer and confidence.

## Safe read-only checks

Prefer Android Settings for model, software, and build information. For Termux diagnostics, use read-only commands such as `getprop ro.product.model`, `getprop ro.build.version.release`, `getprop ro.build.display.id`, `uname -a`, `uname -m`, `df -h`, and `id`. Availability and output can differ by Android build and execution context; record failures rather than substituting guesses.

Do not use these checks to access raw block devices or modify partitions.

## Status

This file is a baseline template populated with previously reported values. It is not a fresh diagnostic capture from the tablet and does not certify bootloader, partition, encryption, or recovery state.
