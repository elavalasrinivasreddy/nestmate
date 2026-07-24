<div align="center">
  <img src="docs/images/logo.webp" width="120" alt="Nestmate Logo">
  
  <h1>Nestmate</h1>
  <p><b>Find a room. Find a roommate. Without the chaos of scattered WhatsApp groups and sketchy listings.</b></p>
  
  [![Android CI](https://github.com/elavalasrinivasreddy/nestmate/actions/workflows/android_ci.yml/badge.svg)](https://github.com/elavalasrinivasreddy/nestmate/actions/workflows/android_ci.yml)
</div>

## 📱 About the App

**Nestmate** is a trust-first, two-sided housing-discovery app designed specifically for students and relocating professionals. 
- **Room Holders:** Post *"I have a room"* to find verified, reliable roommates.
- **Room Seekers:** Post *"I'm looking for a room"* and connect with great places to live.

By bringing both sides together in one secure platform, Nestmate eliminates the noise of social media housing groups and provides a safe, filtered environment to find your next home.

## ✨ Key Features

*   🔒 **Verified Profiles:** Email and phone authentication built-in to ensure a trusted community.
*   🏠 **Room Vacancies:** Easily post, edit, and manage available rooms with detailed descriptions.
*   🙋 **Accommodation Requirements:** Room seekers can post exactly what they are looking for.
*   🔍 **Smart Discovery:** Search and filter by location, budget, and room type to find the perfect match.
*   💬 **Real-Time Chat:** In-app messaging allows both sides to communicate safely without sharing personal numbers upfront.
*   🔖 **Bookmarks:** Save your favorite listings or roommate profiles for quick access.

## 🛠️ For Developers

Nestmate is built with a modern Android tech stack, focusing on architecture, code quality, and maintainability.

| Layer | Choice |
|---|---|
| **Language** | Kotlin 2.2.x |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Repository, unidirectional state (`StateFlow`) |
| **Dependency Injection** | Hilt |
| **Navigation** | Navigation Compose (type-safe routes) |
| **Async** | Coroutines + Flow |
| **Backend** | Firebase Auth, Cloud Firestore, Storage, FCM |

### Getting Started (Local Development)

Full setup instructions—including Firebase configuration, SDKs, signing, and running the app—are available in our [Setup Guide](docs/SETUP.md). 

**Quick Start:**
1. Open the project in Android Studio (targets Firebase project *Nestmate*).
2. Ensure `app/google-services.json` is present (see SETUP).
3. Sync Gradle and Run on a device/emulator (API 24+).

*First time only: Run `bash scripts/init-git.sh` to initialize git hooks.*

### CI/CD Pipeline

This project utilizes GitHub Actions for Continuous Integration. Every push and pull request to the `main` branch triggers a workflow that automatically sets up the JDK environment, builds the app using Gradle, and runs unit tests to ensure stability.

## 📚 Documentation

| Document | Description |
|---|---|
| [Product Spec](docs/PRODUCT_SPEC.md) | What we're building and for whom |
| [Architecture](docs/ARCHITECTURE.md) | How the app is structured |
| [Data Model](docs/DATA_MODEL.md) | Firestore collections + security rules |
| [Roadmap](docs/ROADMAP.md) | Phase-by-phase build plan |
| [Status Tracker](docs/STATUS_TRACKER.md) | Live progress, blockers, dependencies |
| [Decisions](docs/DECISIONS.md) | Architecture decision log (ADRs) |

---
<div align="center">
  <sub>Built with care for the craft of software engineering. Quality and completeness over growth.</sub>
</div>
