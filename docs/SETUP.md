# Nestmate — Setup

*Living document. Last updated: 2026-06-15.*

## Prerequisites

- **Android Studio** — a recent version that supports **AGP 9.2.x** (this project pins AGP `9.2.1`, Kotlin `2.2.10`).
- **JDK 17+** — bundled with Android Studio; no separate install needed.
- **Android SDK 36** (compileSdk/targetSdk = 36; minSdk = 24).
- A device or emulator on **API 24+**.

## Project location

The project lives on the secondary drive at:

```
/media/elavala-srinivas-reddy/HDD/2026/Nestmate
```

Open this folder directly in Android Studio.

## Firebase configuration

The app is wired to a Firebase project named **Nestmate** (`applicationId = com.nestmate.app`).

1. **`google-services.json`** must sit at **`app/google-services.json`**. It is **git-ignored** (kept out of version control), so it must be present locally to build. If it's missing, re-download it from the Firebase console → Project settings → Your apps → Nestmate (Android) → `google-services.json`.
2. **Authentication** → enable **Email/Password** and **Phone**.
3. **Cloud Firestore** → create database. Currently in **test mode**; production rules ship in Phase 9 (see `DATA_MODEL.md`).
4. **Storage** → *deferred* (billing). Photos are off until this is enabled.

### Dev phone testing

A test phone number is registered so you never burn real SMS while building:

```
Phone:  +91 82979 39238
Code:   123456
```

Real SMS is capped at **10/day** until a billing account is added to the project.

### SHA-1 (needed for phone auth on a real device)

From the project root:

```bash
./gradlew signingReport
```

Copy the **debug** `SHA-1`, then add it in Firebase console → Project settings → Your apps → Nestmate (Android) → **Add fingerprint**. (Claude's sandbox can't run Gradle on this drive, so run this on your machine.)

## Build & run

1. Open the project in Android Studio; let Gradle sync.
2. When Claude adds dependencies (Phase 1+), Android Studio will prompt to **Sync Now** — click it (Claude can't press IDE buttons).
3. Pick a device/emulator (API 24+) and **Run**.

## Git

```bash
bash scripts/init-git.sh      # one-time: init repo + first commit
```

If Android Studio doesn't show the repo afterward, set it manually: **Settings → Version Control → Directory Mappings → +**, map the project root as **Git**. (The IDE's generated mapping pointed at the parent folder; this corrects it.)

## Troubleshooting

- **"File google-services.json is missing"** → ensure it's at `app/google-services.json` (re-download from Firebase).
- **Gradle sync fails after dependency changes** → File → Invalidate Caches / Restart, then re-sync.
- **Phone auth fails on device** → confirm the SHA-1 is added in Firebase and you're using the registered test number in dev.
- **Min SDK errors** → device/emulator must be API 24+.
