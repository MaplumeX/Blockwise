## 1. Specification
- [x] 1.1 Update `openspec/changes/update-timeline-entry-context-sheet/specs/timeline-interactions/spec.md` with renamed/modified requirements for bottom sheet, delete undo, and selection mode batch actions.
- [x] 1.2 Run `openspec validate update-timeline-entry-context-sheet --strict --no-interactive` and fix all validation issues.

## 2. UI & Interaction (Timeline list)
- [x] 2.1 Replace floating popup context menu with a fixed-height, non-draggable modal bottom sheet.
- [x] 2.2 Implement sheet dismiss behaviors: close button and scrim tap; verify no accidental dismiss via drag.
- [x] 2.3 Implement top icon action area: Merge Up, Merge Down, Delete, Close.
- [x] 2.4 Implement main content editor sections:
  - [x] 2.4.1 Start/end time pickers (1-minute step) + live duration display
  - [x] 2.4.2 Activity type quick chips (two fixed rows) + "More" entry
  - [x] 2.4.3 Tags section: "Add tag" + selected tag chips (collapsible)
  - [x] 2.4.4 Note multi-line input with placeholder and minimum height
- [x] 2.5 Implement bottom primary actions:
  - [x] 2.5.1 Save changes (apply edits and refresh the Timeline list)
  - [x] 2.5.2 Split

## 3. Data / Domain Wiring
- [x] 3.1 Reuse or introduce an update time entry use case to persist edits from the sheet (time range, activity type, tags, note).
- [x] 3.2 Implement delete Undo semantics (choose: delayed delete or restore-after-delete) and ensure correctness.

## 4. Multi-Select Mode
- [x] 4.1 Add a bottom batch action bar in selection mode with Delete and Merge.
- [x] 4.2 Ensure Merge is enabled only when selection rules are satisfied; provide user feedback otherwise.
- [x] 4.3 Ensure selection mode never shows the single-entry sheet.

## 5. Tests & Validation
- [x] 5.1 Add/adjust UI tests for:
  - [x] 5.1.1 Tap opens bottom sheet
  - [x] 5.1.2 Long-press enters selection mode and does not open sheet
  - [x] 5.1.3 Delete shows Undo snackbar; Undo restores
  - [x] 5.1.4 Merge Up/Down enabled/disabled conditions (adjacent entry present)
- [x] 5.2 Run `./gradlew test`.
- [x] 5.3 Run targeted instrumentation tests (e.g. `:feature:timeentry:connectedDebugAndroidTest`) if available.
