# Candidate same-device Android app build path

## Status and scope

This is a research-backed candidate for compiling **this small Android probe app** on the SM-X200 itself. It is not yet tested on the user's tablet, and it does not establish that a full OrangeFox/TWRP recovery source build can run there.

A public third-party Termux guide reports a working ARM64 Android app toolchain using OpenJDK 21, Gradle 8.11.1, Android Gradle Plugin 8.7.3, Android SDK API 35, and Termux-native Android build tools. Its documented aapt2 workaround is important: the Linux/glibc aapt2 artifact downloaded by Android Gradle Plugin may not execute in Android's Bionic userspace. The guide overrides it with Termux's native aapt2 binary.

Reference implementation (external, not a project dependency): https://github.com/GeoFFerDev/Termux-Based-Native-Builder

Gradle's official documentation says the wrapper should be generated with the Gradle `wrapper` task and checked into version control. Do not copy an unverified wrapper JAR from an arbitrary mirror. See https://docs.gradle.org/current/userguide/gradle_wrapper.html.

## Candidate toolchain

- Termux on the target tablet, ARM64 userspace.
- OpenJDK 21.
- Gradle Wrapper 8.11.1 (compatible with the AGP 8.7.3 family used here).
- Android Gradle Plugin 8.7.3.
- Android SDK platform 35 and Termux-compatible build tools.
- Termux-native `aapt2` selected through `android.aapt2FromMavenOverride`.
- No root, second device, PC, or remote build service.

The current app uses Kotlin Android plugin 2.0.21. The external guide reports a newer Kotlin plugin; that difference must be tested rather than silently copied.

## Why the aapt2 override matters

Android runs Bionic, not glibc. A host executable downloaded from Maven for conventional Linux may fail to execute in Termux. The external guide uses the native Termux SDK package's aapt2 binary and sets:

```properties
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/opt/android-sdk/build-tools/36.1.0/aapt2
```

That path is specific to the standard Termux installation and package version. Confirm the actual path on the tablet before using it. Do not hard-code this machine-specific path into the shared project configuration.

## First validation sequence

1. Inspect the installed Termux/JDK/SDK environment before installing or upgrading packages.
2. Confirm the tablet reports `aarch64`, Java 21 is available, and the Android SDK platform 35 is installed.
3. Confirm the Termux-native `aapt2` exists and can execute.
4. Generate the Gradle Wrapper on the tablet with Gradle 8.11.1, then commit the generated scripts, properties, and wrapper JAR. Verify the wrapper JAR against Gradle's published checksum before executing it.
5. Set the local SDK path in ignored `local.properties`; keep machine-specific paths and credentials out of Git.
6. Configure the local Gradle environment to use the Termux-native aapt2. Do not commit a personal absolute path.
7. Run the local unit tests and assemble the debug APK on the tablet.
8. Install the APK on the same tablet through Android's package installer and record the exact result.

Suggested first build commands after the environment is configured:

```bash
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

Expected APK path:

```
app/build/outputs/apk/debug/app-debug.apk
```

A successful local app build is only evidence that the probe app can be compiled on this Android installation. It is not evidence that a recovery image can be built, booted, or used to decrypt data.

## Resource and safety limits

- The user's earlier reported free internal storage was approximately 17 GB; remeasure before installing SDK components or attempting a build.
- Keep parallel Gradle execution disabled and use `--no-daemon` for the initial Termux experiment.
- Monitor memory, available storage, battery temperature, and process termination during the build.
- Do not run third-party one-shot setup scripts blindly. Review package names, commands, download sources, and file changes first.
- Do not grant root or Shizuku access for this app-build test.
- Do not flash, format, erase, repartition, or write to any block device.

## Evidence required to mark this path supported

Record the Termux app source/version, Android version, architecture, JDK and Gradle versions, SDK platform/build-tools versions, resolved aapt2 path, command output, elapsed time, peak storage use, observed memory/thermal behavior, APK SHA-256, install result, and app-reported device facts.

Until that record exists for the actual SM-X200, this remains a candidate path—not a supported feature.
