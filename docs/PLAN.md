# Implementation Plan: Airsoft Shot Timer

## Overview

This plan breaks down the development of the Airsoft Shot Timer app into sequential, verifiable tasks. We will follow a phased approach, starting with the project foundation, then implementing core features, and finally polishing the app.

## Task List

### Phase 1: Foundation (Project Setup & Basic Communication)

The goal of this phase is to create a compilable and runnable skeleton project where the watch and phone modules can communicate.

-   **[x] Task 1: Setup Android Project Structure**
    -   **Description:** Initialize a new Android Studio project with two modules: `phone` for the handheld app and `watch` for the Wear OS app. Configure Gradle files and necessary dependencies.
    -   **Acceptance Criteria:** The project opens and builds successfully in Android Studio without errors. Both `phone` and `watch` modules compile.
    -   **Verification:** Run `./gradlew build` from the project root. The build must succeed.
    -   **Dependencies:** None.

-   **[x] Task 2: Implement Basic Watch-to-Phone Data Sync**
    -   **Description:** Integrate the Wearable Data Layer API. Create a basic UI on the watch with a button and a service on the phone to listen for messages.
    -   **Acceptance Criteria:** Tapping the button on the watch sends a simple message (e.g., a "ping" string) to the phone. The phone app receives the message.
    -   **Verification:** Check the phone's Logcat for a message printed by the receiving service when the watch button is pressed.
    -   **Dependencies:** Task 1.

### Checkpoint: Foundation
-   [x] All tests pass (if any).
-   [x] Application builds without errors.
-   [x] Basic data transfer from watch to phone is functional.

### Phase 2: Core Feature Implementation

This phase focuses on building the main functionalities as defined in the spec.

-   **[x] Task 3: [Watch] Implement Timer UI and Logic**
    -   **Description:** Create the main UI for the watch app featuring a large "START" button. Implement the timer sequence: random delay (1-3s), start-signal "BEEP", and a high-precision on-screen timer.
    -   **Acceptance Criteria:** Pressing "START" triggers the audio signal after a random delay and the timer on the screen begins counting up.
    -   **Verification:** Manually test the watch app. The BEEP sound should be audible, and the timer should visually update.
    -   **Dependencies:** Task 1.

-   **[x] Task 4: [Watch] Implement Microphone-based Shot Detection**
    -   **Description:** Develop a service that accesses the watch's microphone to listen for sound levels. When a sound exceeds a predefined decibel threshold, it should stop the timer.
    -   **Acceptance Criteria:** The running timer stops when a sufficiently loud noise (like a clap) is made near the watch.
    -   **Verification:** Run the timer and clap. The timer should stop and display the elapsed time.
    -   **Dependencies:** Task 3.

-   [x] Task 5: [Watch] Implement Accelerometer-based Shot Detection
    -   **Description:** Develop a service that monitors the watch's accelerometer data. When it detects a high-G, sharp motion pattern, it should stop the timer.
    -   **Acceptance Criteria:** The running timer stops when the watch experiences a motion pattern consistent with an Airsoft recoil (simulated by a sharp wrist flick).
    -   **Verification:** Run the timer and flick your wrist. The timer should stop and display the elapsed time.
    -   **Dependencies:** Task 3.

-   [x] **Task 6: [Watch] Integrate Detection Modes & Send Data**
    -   **Description:** Add a simple settings UI on the watch to allow the user to switch between "Microphone" and "Accelerometer" modes. After the timer is stopped by either method, the final time result is sent to the phone via the Data Layer.
    -   **Acceptance Criteria:** The detection mode can be changed. The final time is successfully transmitted to the phone.
    -   **Verification:** Check the phone's Logcat for the correctly formatted time data (e.g., "0.89") after a successful shot detection on the watch.
    -   **Dependencies:** Task 2, 4, 5.

-   **[x] Task 7: [Phone] Setup Database with Room**
    -   **Description:** Define the `ShotRecord` entity and create the Room database structure to store shot timings and timestamps.
    -   **Acceptance Criteria:** A Room database is configured. A `ShotRecord` can be successfully inserted, queried, and deleted.
    -   **Verification:** Write a simple unit test that inserts a record, reads it back, and confirms the data is correct.
    -   **Dependencies:** Task 1.

-   **[x] Task 8: [Phone] Implement History List UI**
    -   **Description:** Create the main screen of the phone app. It will fetch all records from the Room database and display them in a reverse chronological list using Jetpack Compose.
    -   **Acceptance Criteria:** The phone app displays a list of all shot records stored in the database.
    -   **Verification:** Manually add a few records to the database (via test or code) and verify they appear correctly on the screen.
    -   **Dependencies:** Task 7.

-   **[ ] Task 9: [Phone] Implement Best Score Highlighting**
    -   **Description:** Add logic to the data query or the UI to find the `ShotRecord` with the minimum time. In the Compose UI, apply a visually distinct style to this list item.
    -   **Acceptance Criteria:** The list item corresponding to the fastest shot time has a different background color.
    -   **Verification:** Ensure that among a list of various times, only the fastest one is highlighted. If a new, faster time is added, the highlight moves to the new record.
    -   **Dependencies:** Task 8.

### Checkpoint: Complete
-   [ ] All acceptance criteria for all tasks are met.
-   [ ] The app is ready for user testing and review.
