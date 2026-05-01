# Feature Plan: Session-Based History

## Overview

Replace the flat shot history list with a session-grouped view. Each training session
groups its records. Users start a new session via a TopAppBar button. Sessions can be
deleted by swiping the session header left to reveal an inline delete button (swipe-to-reveal
via `AnchoredDraggableState`), then tapping it.

Reference: `docs/ideas/session-based-history.md`

## Current State

| Component | Current |
|-----------|---------|
| `ShotRecord` | `id`, `timestamp`, `time` — no `sessionId` |
| `AppDatabase` | version 1, no migration |
| `ShotDao` | `getAll()`, `insert()`, `delete()`, `getBestShot()` |
| `HistoryViewModel` | `shots`, `bestShot` |
| `HistoryListScreen` | flat `LazyColumn` of `ShotItem` |
| `MessageService` | inserts records with no session awareness |

## Dependency Graph

```
Task 10: ShotRecord + DB migration v2 + DAO changes
         ↓
Task 11: SessionManager + MessageService update
         ↓
Task 12: HistoryViewModel — grouped shots + deleteSession + startNewSession
         ↓
Task 13: UI — session headers + swipe-to-reveal delete + TopAppBar action
```

---

## Task 10: Data Model Migration

**Files:**
- `mobile/…/data/ShotRecord.kt`
- `mobile/…/data/AppDatabase.kt`
- `mobile/…/data/ShotDao.kt`

**Changes:**

`ShotRecord` — add two fields:
```kotlin
val sessionId: Long = 0L          // groups records into a training session
val location: String? = null      // reserved, not used in UI yet
```

`AppDatabase` — bump to version 2, add migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE shot_records ADD COLUMN sessionId INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shot_records ADD COLUMN location TEXT")
    }
}
```

`ShotDao` — add:
```kotlin
// order by sessionId DESC so newest session comes first, then timestamp DESC within session
@Query("SELECT * FROM shot_records ORDER BY sessionId DESC, timestamp DESC")
fun getAll(): Flow<List<ShotRecord>>   // replaces existing

@Query("DELETE FROM shot_records WHERE sessionId = :sessionId")
suspend fun deleteBySession(sessionId: Long)
```

**Acceptance Criteria:**
- `ShotRecord` compiles with new fields, defaults don't break existing insert calls.
- DB migration runs without crash; existing rows get `sessionId = 0`.
- `deleteBySession()` removes only records matching the given sessionId.

**Verification:**
- Extend `AppDatabaseTest`: insert records with different sessionIds, call `deleteBySession`, confirm only target session records are gone.
- `./gradlew :mobile:build` must pass.

---

## Task 11: SessionManager + MessageService

**Files:**
- `mobile/…/data/SessionManager.kt` (new)
- `mobile/…/MessageService.kt`

**SessionManager** — SharedPreferences-backed singleton:
```kotlin
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val currentSessionId: Long
        get() = prefs.getLong("current_session_id", 0L).let {
            if (it == 0L) startNewSession() else it
        }

    fun startNewSession(): Long {
        val id = System.currentTimeMillis()
        prefs.edit().putLong("current_session_id", id).apply()
        return id
    }
}
```

**MessageService** — pass `currentSessionId` when inserting:
```kotlin
val record = ShotRecord(
    timestamp = System.currentTimeMillis(),
    time = timeFloat,
    sessionId = SessionManager(applicationContext).currentSessionId
)
```

**Acceptance Criteria:**
- First launch: `currentSessionId` is initialized to a non-zero timestamp.
- After `startNewSession()`, subsequent calls to `currentSessionId` return the new value.
- Records inserted via `MessageService` carry the active session ID.

**Verification:**
- Unit test `SessionManager`: verify `currentSessionId` persists; verify `startNewSession()` returns a new value greater than the previous one.
- `./gradlew :mobile:build` must pass.

---

## Task 12: HistoryViewModel — Grouped Shots

**File:** `mobile/…/ui/HistoryViewModel.kt`

**Changes:**

```kotlin
// Group at application level — no new Room entity needed
val groupedShots: StateFlow<List<Pair<Long, List<ShotRecord>>>> =
    dao.getAll()
        .map { records -> records.groupBy { it.sessionId }.entries
            .sortedByDescending { it.key }
            .map { it.key to it.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

fun startNewSession() {
    sessionManager.startNewSession()
}

fun deleteSession(sessionId: Long) {
    viewModelScope.launch { dao.deleteBySession(sessionId) }
}
```

Remove `shots` StateFlow (replaced by `groupedShots`). Keep `bestShot`.

**Acceptance Criteria:**
- `groupedShots` emits records correctly grouped by `sessionId`, newest session first.
- `deleteSession()` removes all records of that session from the DB, and `groupedShots` updates reactively.
- `startNewSession()` delegates to `SessionManager`.

**Verification:**
- `./gradlew :mobile:build` must pass (compile check is sufficient; logic is tested end-to-end in Task 13 manual test).

---

## Task 13: UI — Session List with Swipe-to-Reveal Delete

**File:** `mobile/…/ui/HistoryListScreen.kt`

**Layout:**

```
TopAppBar: "Shot History"  [ 開始新訓練 icon ]
─────────────────────────────────────
▸  2024-05-01  12:30        ← session header (swipe-left to reveal)
   ★ 0.38 s  12:34:01
     0.45 s  12:35:20
─────────────────────────────────────
▸  2024-04-28  09:00
     0.61 s  09:05:10
─────────────────────────────────────
```

**Swipe-to-reveal implementation:**

Use `AnchoredDraggableState` (Compose Foundation). Session header has two anchors:
- `Resting` (offset = 0): normal view
- `Revealed` (offset = -deleteButtonWidthPx): delete button visible

When `Revealed`, a red "刪除" button appears on the right of the header row.
Tapping it calls `viewModel.deleteSession(sessionId)` and snaps back to `Resting`.

**New composables:**
- `SessionHeader(sessionId, isSwiping, onDeleteClick)` — the header row with drag state
- `SwipeToRevealSession(sessionId, onDelete, content)` — wraps header with drag logic
- `ShotItem` — unchanged

**Acceptance Criteria:**
- Sessions are displayed grouped, newest first.
- "開始新訓練" button in TopAppBar creates a new session (verified by inserting a new record afterwards and seeing it appear in a new group).
- Swiping a session header left reveals a red "刪除" button; releasing before full swipe snaps back.
- Tapping "刪除" removes the session and its records from the list.
- If no sessions exist, shows "No shots recorded yet."
- Global best score (`★`) is still highlighted across all sessions.

**Verification:**
- `./gradlew :mobile:build` must pass.
- Manual test with 2+ sessions: confirm grouping, swipe-reveal, delete, new session flow.

---

## Checkpoint

- [ ] Task 10 — DB migration and DAO compile + test pass
- [ ] Task 11 — SessionManager unit tests pass, MessageService updated
- [ ] Task 12 — ViewModel compiles, groupedShots logic correct
- [ ] Task 13 — UI builds, manual golden-path test passes
