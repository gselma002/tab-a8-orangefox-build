# Device compatibility and validation

## Purpose

A device codename or successful build is not sufficient evidence of recovery compatibility. Every supported model and firmware combination must have traceable evidence.

## Identity record

A device definition should capture, where available:

- manufacturer, marketing name, exact model number, codename, regional/SKU variant;
- SoC/platform and CPU architecture;
- Android release, build fingerprint, build ID, security patch level, bootloader revision;
- bootloader lock state only when reliably observable and explicitly authorized;
- kernel version and boot image header/version details;
- physical and dynamic partition layout, sizes, slot scheme, and filesystem types;
- AVB/vbmeta configuration and rollback behavior;
- display resolution, panel and touchscreen controller variants;
- encryption mode, metadata-encryption behavior, keymaster/gatekeeper dependencies, and recovery decryption status;
- source URLs, immutable commit IDs, file hashes, observation date, and confidence level.

Unknown values must remain unknown. Do not fill gaps with guesses from another regional variant or firmware release.

## Evidence levels

| Level | Meaning | Permitted claim |
|---|---|---|
| E0 | Unverified report or upstream claim | Reported, not independently verified |
| E1 | Source/configuration inspected | Source-reviewed only |
| E2 | Build completed from pinned inputs | Compiles for the declared target |
| E3 | Image statically inspected | Image structure inspected; not boot proof |
| E4 | Boot-tested on the exact device variant | Boots on the tested variant |
| E5 | Hardware functions tested | Only the individually tested functions may be claimed |
| E6 | Data/decryption and recovery workflows tested | Only the exact tested firmware and data configuration may be claimed |

A higher level does not automatically establish every lower-level feature. Record test method, tester, date, firmware, result, logs, and artifact SHA-256.

## SM-X200 current known facts

The current reference device is a Samsung Galaxy Tab A8 Wi-Fi, model SM-X200, codename `gta8wifi`, with Unisoc Tiger T618 / `ums512` platform. The user's reported installed firmware is `X200XXS6DYG6`, Android 14 / One UI 6.1.

The upstream device tree inspected for the `fox-12.1` branch identifies an Android 11 fingerprint and disables both `TW_INCLUDE_CRYPTO` and `TW_INCLUDE_CRYPTO_FBE`. It also contains AVB/test-key and rollback-related configuration that requires review. These are source observations, not proof that a particular published image will fail or succeed.

The actual touchscreen controller revision, complete partition map, exact boot chain behavior, and successful Android 14 data decryption have not been independently established in this project.

## Required validation before any recovery can be called compatible

1. Confirm exact model and regional variant.
2. Capture read-only device facts and compare them with the manifest.
3. Verify partition names, sizes, image format, and boot/recovery installation model against authoritative device evidence.
4. Review AVB keys, vbmeta flags, rollback indices, and bootloader behavior. No test-key or rollback workaround is accepted without a documented, device-specific explanation.
5. Confirm panel/touch driver selection on the physical device.
6. Build from immutable source revisions and record all inputs.
7. Inspect the resulting image and verify its hash and structure.
8. Boot-test only through a separately reviewed, reversible procedure appropriate to the device. Never make an unverified image the only boot path.
9. Test USB, ADB, input, display, storage, backup/restore, and other claimed functions individually.
10. Test encryption/decryption against the exact firmware and encryption state before claiming data support.

## Stop conditions

Stop and report the blocker if partition data conflicts, firmware identity is ambiguous, AVB behavior is unexplained, required source files are missing, build inputs are mutable, image structure is unexpected, or a test could make the device unbootable or destroy user data.

The initial project milestone is read-only inspection and evidence collection. It does not flash or modify partitions.
