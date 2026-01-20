## Context
The Timeline list currently uses an in-place floating menu (`Popup` + `DropdownMenuItem`) positioned near the user's tap.
This proposal replaces that UI with a fixed-height, non-draggable half-screen Bottom Sheet that first presents the entry details and then offers editing and actions.

## Goals / Non-Goals
- Goals:
  - Improve readability: show entry info in a stable panel before actions.
  - Reduce mis-taps: separate destructive actions (Delete) from primary actions (Save/Split).
  - Preserve interaction cost: a single tap still reveals the action surface (no added steps).
  - Provide a structure to expand later (quick edit, tags, note, etc.).
- Non-Goals:
  - Full visual redesign of the Timeline screen.
  - Introducing new external dependencies for pickers.
  - Changing Time Block view behavior unless needed for shared components.

## Decisions
- Decision: Use a fixed-height Modal Bottom Sheet for the Timeline list.
  - Rationale: Material3 provides a consistent modal surface with a scrim and proper accessibility.
  - Fixed height: target ~50% of the screen height.
  - Non-draggable: disable sheet gestures to prevent height changes and swipe-to-dismiss.

- Decision: Keep editing as a draft until explicit Save.
  - Rationale: the bottom sheet contains multiple editable fields; a single Save reduces accidental commits.
  - Dismiss behavior: close button or scrim tap dismisses the sheet and discards unsaved changes silently.

- Decision: Use Undo-first delete (Snackbar) and restore the same entry ID.
  - Rationale: aligns with the goal to reduce destructive friction while providing safety.
  - Implementation approach:
    - Prefer delaying the actual delete until the snackbar timeout elapses (or until Undo is dismissed).
    - This guarantees that Undo restores the same entry ID because the entry is never deleted unless Undo is not taken.

- Decision: Split and Merge remain flows, not in-place edits.
  - Rationale: both operations can have complex constraints and confirmations.
  - Split: launched from the bottom primary action (no v1.2 eligibility gating).
  - Merge:
    - Single-entry sheet: provide two icon actions (Merge Up / Merge Down) to merge with an adjacent entry.
    - Multi-select mode: provide a batch Merge action.

## UX Structure (v1.2)
- Trigger:
  - Tap time entry (not in selection mode) -> open sheet for that entry.
  - Long-press -> enter multi-select mode; no sheet.

- Sheet layout:
  - Top: icon actions (Merge, Delete, Close). Icons are visually "bare" (no capsule background) but meet minimum touch targets.
  - Main content: time range (two wheels), activity type quick chips, tags section, note input.
  - Bottom: primary actions (Save, Split).

## Risks / Trade-offs
- Time wheel pickers:
  - Compose Material3 does not provide a ready-made wheel time picker.
  - Risk: custom wheel implementation adds complexity and test surface.
  - Mitigation: start with a minimal wheel-like selector or a simpler picker while preserving the 1-minute semantics.

- Fixed half-screen on small devices:
  - Risk: content may be cramped.
  - Mitigation: internal scrolling in the main content area with a minimum note height.

- Undo correctness:
  - Risk: restoring a deleted entry incorrectly (ID/order/tags) could surprise users.
  - Mitigation: define and test undo semantics explicitly; prefer delaying delete until snackbar resolves.

## Migration Plan
- Replace the existing `TimelineContextMenuState` UI with a new sheet state.
- Keep existing delete/split/merge flows functional while the new sheet is introduced.
- After the sheet is stable, remove the floating popup menu implementation.

## Open Questions
- If Merge Up/Down is not applicable (no adjacent entry or merge rule fails), should the action be disabled or should it show an error?

## Resolved Product Decisions
- "Adjacent" for Merge Up / Merge Down is defined by time order (chronological ordering).
