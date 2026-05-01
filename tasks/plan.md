# Task 9: [Phone] Implement Best Score Highlighting

## Context

Task 8 is complete. The phone app renders a `LazyColumn` list of `ShotRecord` items in the
`HistoryListScreen` composable. Each item is rendered by `ShotItem`. The goal of Task 9 is to
visually distinguish the fastest shot in the list.

## Codebase State (as of task start)

| File | Status |
|------|--------|
| `mobile/…/data/ShotDao.kt` | `getBestShot()` already exists — returns `Flow<ShotRecord?>` |
| `mobile/…/ui/HistoryViewModel.kt` | Only exposes `shots: StateFlow<List<ShotRecord>>` |
| `mobile/…/ui/HistoryListScreen.kt` | `ShotItem` has no best-shot awareness |

`getBestShot()` SQL: `SELECT * FROM shot_records ORDER BY time ASC LIMIT 1`

## Dependency Graph

```
ShotDao.getBestShot()   (already exists)
        │
        ▼
HistoryViewModel.bestShot: StateFlow<ShotRecord?>   ← Step 1
        │
        ▼
HistoryListScreen collects bestShot, derives bestId  ← Step 2
        │
        ▼
ShotItem(isBest: Boolean) applies highlight color    ← Step 3
```

## Implementation Steps

### Step 1 — HistoryViewModel: expose bestShot

**File:** `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryViewModel.kt`

Add a second `StateFlow` derived from `dao.getBestShot()`:

```kotlin
val bestShot: StateFlow<ShotRecord?> = dao.getBestShot()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
```

No other changes to the ViewModel.

### Step 2 — HistoryListScreen: collect bestShot and thread it to ShotItem

**File:** `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryListScreen.kt`

In `HistoryListScreen`:
1. Collect `viewModel.bestShot` as state.
2. Derive `bestId = bestShot?.id`.
3. Pass `isBest = (shot.id == bestId)` to each `ShotItem`.

### Step 3 — ShotItem: apply visual highlight

**File:** `mobile/src/main/java/com/example/airsoftshottimer/ui/HistoryListScreen.kt`

Update `ShotItem` signature to `ShotItem(shot: ShotRecord, isBest: Boolean)`.

When `isBest == true`, apply a distinct background color to the card/row
(e.g., `MaterialTheme.colorScheme.primaryContainer`).

## Acceptance Criteria

- The list item with the lowest `time` value has a visually distinct background.
- All other items have the normal (default) background.
- If a new record with a lower time is inserted, the highlight moves to that record automatically
  (because both `shots` and `bestShot` are reactive `Flow`s).

## Verification

1. `./gradlew :mobile:build` — must succeed.
2. Launch the app with seed data covering at least 3 different times. Confirm only the minimum
   time item is highlighted.
3. Insert a new record with a lower time. Confirm the highlight moves.

## Risk / Notes

- `bestShot` is compared by `id`, not by `time` value, so ties (two records with identical times)
  will only highlight one (whichever `ORDER BY time ASC LIMIT 1` returns first). This matches the
  spec requirement.
- No new files are needed — all changes are in two existing files.
