# Session-Based History — Todo

## Task 10: Data Model Migration ✅
- [x] Add `sessionId: Long = 0L` and `location: String? = null` to `ShotRecord`
- [x] Bump `AppDatabase` to version 2, add `MIGRATION_1_2`
- [x] Update `ShotDao.getAll()` order to `sessionId DESC, timestamp DESC`
- [x] Add `ShotDao.deleteBySession(sessionId: Long)`
- [x] Add test: `deleteBySession` removes only target session records
- [x] `./gradlew :mobile:build` passes

## Task 11: SessionManager + MessageService ✅
- [x] Create `SessionManager` (SharedPreferences, auto-init on first launch)
- [x] Add instrumented tests: currentSessionId persists, startNewSession() returns new value
- [x] Update `MessageService` to pass `sessionId` when inserting `ShotRecord`
- [x] `./gradlew :mobile:build` passes

## Task 12: HistoryViewModel ✅
- [x] Add `SessionManager` dependency to `HistoryViewModel`
- [x] Replace `shots` with `groupedShots: StateFlow<List<Pair<Long, List<ShotRecord>>>>`
- [x] Add `startNewSession()` delegating to `SessionManager`
- [x] Add `deleteSession(sessionId: Long)` calling `dao.deleteBySession()`
- [x] `./gradlew :mobile:build` passes

## Task 13: UI ✅
- [x] Add "開始新訓練" icon to TopAppBar, calls `viewModel.startNewSession()`
- [x] Render session headers with formatted date from `sessionId`
- [x] Render `ShotItem`s under each session header
- [x] Implement swipe-to-reveal with `Animatable` + `draggable`
  - [x] Left swipe reveals red "刪除" button
  - [x] Partial swipe snaps back on release
  - [x] Tap "刪除" calls `viewModel.deleteSession()`
- [x] Keep global best score `★` highlight
- [x] Empty state: "No shots recorded yet."
- [x] `./gradlew :mobile:build` passes
- [ ] Manual test: grouping / swipe-reveal / delete / new session all work
