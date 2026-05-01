# Task 9 — Best Score Highlighting

## Checklist

- [x] **Step 1** — Add `bestShot: StateFlow<ShotRecord?>` to `HistoryViewModel`
  - File: `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryViewModel.kt`
  - Use `dao.getBestShot().stateIn(viewModelScope, WhileSubscribed(5000), null)`

- [x] **Step 2** — Update `HistoryListScreen` to collect `bestShot`
  - File: `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryListScreen.kt`
  - Derive `bestId = bestShot?.id`
  - Pass `isBest = (shot.id == bestId)` into each `ShotItem` call

- [x] **Step 3** — Update `ShotItem` to accept and apply `isBest`
  - File: `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryListScreen.kt`
  - Add `isBest: Boolean` parameter
  - Apply `MaterialTheme.colorScheme.primaryContainer` background when `isBest == true`

- [x] **Verify** — Build passes (`./gradlew :mobile:build` — BUILD SUCCESSFUL)
