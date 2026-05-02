# Airsoft Shot Timer

A dedicated shot timer application for Airsoft training, running on a Google Pixel 10 (phone) and Pixel Watch (Wear OS).

The app measures reaction time from a start signal to first shot detection, provides immediate feedback on the watch, and stores historical data on the phone for long-term tracking.

## Tech Stack

- **Language:** Kotlin
- **Platform:** Android (Phone) + Wear OS (Watch)
- **UI:** Jetpack Compose / Compose for Wear OS
- **Database:** Room (local storage on phone)
- **Communication:** Wearable Data Layer API (watch-to-phone sync)
- **Target SDK:** Android 14 (API Level 34)

## Project Structure

```
airsoft-shot-timer/
├── mobile/                 # Phone app module
│   └── src/
├── wear/                   # Wear OS watch module (if separate)
│   └── src/
├── docs/                   # Specs and implementation plans
│   ├── SPEC.md
│   ├── PLAN.md
│   └── task-7.md
├── build.gradle.kts
└── settings.gradle.kts
```

## Commands

```bash
# Build all modules
./gradlew build

# Install phone app (debug)
./gradlew :mobile:installDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (device required)
./gradlew :mobile:connectedAndroidTest
```

## Features

### Watch App (Wear OS)

- **Shooting Sequence:**
  1. Tap START
  2. Random delay (3–5 seconds)
  3. Audible BEEP + high-precision timer starts
  4. Shot detected — timer stops
  5. Elapsed time displayed on watch
  6. Result synced to phone automatically

- **Detection Modes (user-selectable):**
  - **Microphone** — detects sharp loud sound above a configurable dB threshold
  - **Accelerometer** — detects high-G wrist jerk consistent with Airsoft recoil

### Phone App (Android)

- Chronological list of all recorded shot times
- Each entry shows date, time, and elapsed shot time
- Best (fastest) time highlighted in a distinct color
- Button to clear all historical data

## Implementation Progress

### Phase 1 — Foundation
- [x] Task 1: Android project setup (phone + watch modules)
- [x] Task 2: Basic watch-to-phone data sync via Wearable Data Layer

### Phase 2 — Core Features
- [x] Task 3: Watch timer UI and logic (START button, random delay, BEEP, counter)
- [x] Task 4: Microphone-based shot detection
- [x] Task 5: Accelerometer-based shot detection
- [x] Task 6: Detection mode settings UI + send result to phone
- [x] Task 7: Room database setup on phone (`ShotRecord` entity, DAO, DB class)
- [x] Task 8: History list UI on phone (Jetpack Compose, reverse chronological)
- [x] Task 9: Best score highlighting

### Phase 3 — Session-Based History
- [x] Task 10: Add `sessionId` to `ShotRecord` + Room migration v2
- [x] Task 11: `SessionManager` — creates/stores current session, wired into `MessageService`
- [x] Task 12: Session-grouped history UI with swipe-to-reveal delete (per session)
- [x] Task 13: Swipe-to-reveal hit area refinement + Compose preview

## Future Milestones

- Multi-shot split time tracking
- Data export as CSV
- Calibration screen for mic sensitivity and accelerometer thresholds
- Session custom naming
- Per-session best score / cross-session statistics
