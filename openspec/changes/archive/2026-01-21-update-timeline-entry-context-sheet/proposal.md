# Change: Update timeline time entry context menu to a fixed half-screen bottom sheet

## Why
The current in-place floating context menu makes it easy to trigger destructive actions without first reviewing the time entry details.
A fixed half-screen bottom sheet improves readability, reduces accidental taps by separating dangerous actions, and creates a stable surface to extend with inline edit affordances (time range, tags, note).

## What Changes
- Replace the Timeline list time entry tap behavior:
  - BEFORE: tap shows a floating context menu near the tap location.
  - AFTER: tap opens a fixed-height, non-draggable half-screen Bottom Sheet containing entry details and inline edit controls.
- Add a top icon action area (in the sheet) for infrequent actions:
  - Merge Up
  - Merge Down
  - Delete (dangerous action)
  - Close (top-right)
- Add a main content editor area inside the sheet:
  - Start/end time wheels (1-minute step) with live duration display
  - Frequent activity type chips (two fixed rows) + "More" entry
  - Tags section with "Add tag" and selected tag chips (collapsible)
  - Multi-line note input
- Add bottom primary actions:
  - Save changes (applies updates to time range / activity / tags / note)
  - Split
- Update delete safety strategy:
  - Prefer Snackbar Undo after deletion.
  - If Undo cannot be supported, fall back to explicit confirmation.
- Multi-select behavior remains long-press driven, but adds a bottom batch action bar:
  - Delete
  - Merge (enabled only when selection rules are satisfied)

## Impact
- Affected specs:
  - `openspec/specs/timeline-interactions/spec.md`
- Likely affected code:
  - `feature/timeentry/.../presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/.../presentation/timeline/TimelineViewModel.kt`
  - `feature/timeentry/.../presentation/timeline/TimeEntryItem.kt`
- UX/behavioral changes:
  - Tap no longer shows a floating menu; it opens an editing-capable bottom sheet.
  - Delete moves from a confirmation-first flow to an Undo-first flow.

## Non-Goals
- Redesigning the Timeline entry card visuals (title/time/tags) beyond what is required for the new interaction.
- Changing Time Block day view selection/detail panel behavior (unless explicitly requested).
- Implementing advanced tag/note editors beyond the described entry points (v1.2 scope).

## Resolved Product Decisions
- Merge in the single-entry sheet uses two icon actions:
  - Merge Up
  - Merge Down
- Split eligibility is not gated in v1.2 (the Split entry point is always available).
- Dismissing the Bottom Sheet discards unsaved changes silently.
- Undo MUST restore the same time entry (including the same ID).
