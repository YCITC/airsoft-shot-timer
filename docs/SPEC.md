# Spec: Airsoft Shot Timer App

## 1. Objective

This project aims to create a dedicated shot timer application for Airsoft training, running on a Google Pixel 10 and a Pixel Watch.

The primary user is an Airsoft shooter who wants to measure and improve their reaction time from a starting signal to their first shot. The app will provide immediate feedback on the watch and store historical data on the phone for long-term tracking and analysis.

## 2. Tech Stack

- **Platform:** Android (Phone) & Wear OS (Watch)
- **Language:** Kotlin
- **UI Framework:**
  - Phone: Jetpack Compose
  - Watch: Compose for Wear OS
- **Database:** Room for local storage on the phone app.
- **Communication:** Wearable Data Layer API (for watch-to-phone data synchronization).
- **Target SDK:** Android 14 (API Level 34)

## 3. Commands

(This section will be populated as the project is set up)
- **Build:** `./gradlew build`
- **Run Phone App:** `./gradlew installDebug` (via Android Studio)
- **Run Watch App:** `./gradlew installDebug` (via Android Studio)
- **Test:** `./gradlew test`

## 4. Project Structure

The project will be a single Android Studio project containing two main modules:

```
airsoft-shot-timer/
├── phone/                  # Phone App Module
│   ├── src/
│   └── build.gradle.kts
├── watch/                  # Wear OS App Module
│   ├── src/
│   └── build.gradle.kts
├── build.gradle.kts        # Project-level build file
└── settings.gradle.kts
```

## 5. Core Features (MVP - First Shot)

### 5.1. Watch App (Wear OS)

- **Main Screen:**
  - A large, clear "START" button.
  - A settings icon to select the detection mode.
  - Display of the last recorded time.

- **Shooting Sequence:**
  1. User taps "START".
  2. The app waits for a random delay between 3.0 and 5.0 seconds.
  3. The watch emits a loud, clear "BEEP" sound and simultaneously starts an internal high-precision timer.
  4. The app actively listens for a shot using the selected detection method.
  5. Upon detecting a shot, the timer stops.
  6. The final time (e.g., "0.89s") is displayed prominently on the watch face.
  7. The recorded time and a timestamp are automatically sent to the phone app via the Wearable Data Layer.

- **Detection Modes (User Selectable):**
  - **Mode A: Microphone:** The app will listen for a sharp, loud sound (a configurable decibel threshold) that signifies a gunshot.
  - **Mode B: Accelerometer:** The app will monitor the watch's accelerometer for a sudden, high-G jerk pattern consistent with the recoil of an Airsoft pistol.

### 5.2. Phone App (Android)

- **Main Screen:**
  - A chronological list of all recorded shot times received from the watch.
  - Each list item will display the date, time of the session, and the recorded shot time (e.g., "2026-04-26 14:30:15 - 0.95s").
  - The single best (fastest) time in the entire list will be permanently highlighted with a distinct color (e.g., gold or green background).
  - A button to clear all historical data.

## 6. Boundaries

- **Always:**
  - Write code in Kotlin, following modern Android development practices.
  - Ensure UI is clean, responsive, and easy to read in outdoor conditions.
  - Add comments to explain complex logic, especially around sensor data processing.
- **Ask First:**
  - Before adding any third-party libraries not listed in the tech stack.
  - Before making significant changes to the UI/UX agreed upon in this spec.
  - Before implementing features planned for future releases (like multi-shot timing).
- **Never:**
  - Commit API keys or sensitive information to version control.
  - Collect any personal user data beyond the shot timings.

## 7. Success Criteria

- The app (phone and watch) can be successfully built and deployed to target devices.
- The watch app can reliably start a session, emit a sound, and start its timer.
- Both microphone and accelerometer detection methods can successfully stop the timer within 50ms of the actual event.
- A new record from the watch appears on the phone app within 2 seconds of being recorded.
- The phone app correctly identifies and highlights the best historical performance.

## 8. Future Milestones (Post-MVP)

- **Multi-shot "Split Time" tracking:** Record the time between consecutive shots in a single session.
- **Session Management:** Group shots into named sessions (e.g., "Drill 1", "Competition Day").
- **Data Export:** Allow users to export their data as a CSV file.
- **Calibration:** Add a calibration screen for setting microphone sensitivity and accelerometer thresholds.
