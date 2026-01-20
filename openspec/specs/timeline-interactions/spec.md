# timeline-interactions Specification

## Purpose
TBD - created by archiving change add-timeline-context-menu-multiselect. Update Purpose after archive.
## Requirements
### Requirement: Timeline Multi-Select Mode
The system SHALL enter multi-select mode when the user long-presses a time entry card in the Timeline list.
While in multi-select mode, the system SHALL allow the user to tap additional time entry cards to toggle selection.
While in multi-select mode, tapping a time entry card SHALL NOT open the Bottom Sheet.
The system SHALL provide a bottom batch action bar in multi-select mode containing Delete and Merge actions.
The system SHALL provide a clear exit path for multi-select mode.

#### Scenario: Enter multi-select mode on long press
- **GIVEN** the Timeline list renders a time entry card
- **WHEN** the user long-presses the time entry card
- **THEN** the system enters multi-select mode
- **AND** the long-pressed entry becomes selected

#### Scenario: Toggle selection in multi-select mode
- **GIVEN** the system is in multi-select mode
- **WHEN** the user taps a time entry card
- **THEN** the tapped entry toggles between selected and unselected
- **AND** no Bottom Sheet is shown

#### Scenario: Batch actions are available in multi-select mode
- **GIVEN** the system is in multi-select mode
- **WHEN** the batch action bar is shown
- **THEN** Delete and Merge actions are available

#### Scenario: Exit multi-select mode
- **GIVEN** the system is in multi-select mode
- **WHEN** the user triggers the exit control
- **THEN** the system exits multi-select mode
- **AND** all selections are cleared


#### Scenario: FAB is visible in Timeline list view
- **GIVEN** the user is viewing the Timeline list view
- **WHEN** the Timeline list view is rendered
- **THEN** a plus (+) FAB is visible at the bottom-right above the bottom navigation

#### Scenario: FAB is hidden in Time Block day view
- **GIVEN** the user is viewing the Time Block day view
- **WHEN** the Time Block day view is rendered
- **THEN** the quick create FAB is not visible

#### Scenario: FAB is hidden outside Timeline
- **GIVEN** the user navigates to a non-Timeline screen/module
- **WHEN** that screen is rendered
- **THEN** the quick create FAB is not visible

### Requirement: Open Create Mode Bottom Sheet From FAB
When the user taps the Timeline quick create FAB, the system SHALL open a modal Bottom Sheet in create mode.
The Bottom Sheet SHALL appear within 300ms (subjective, no noticeable lag) after the FAB tap.
The Bottom Sheet SHALL be dismissible via a close control and via tapping the scrim area.
Dismissing the Bottom Sheet SHALL NOT create or persist a time entry.

#### Scenario: Tap FAB opens create mode sheet
- **GIVEN** the Timeline quick create FAB is visible
- **WHEN** the user taps the FAB
- **THEN** a create mode Bottom Sheet is shown

#### Scenario: Close sheet without creating
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the user dismisses it via close or scrim tap
- **THEN** the Bottom Sheet is dismissed
- **AND** no time entry is created

### Requirement: Create Mode Bottom Sheet Presentation
The create mode Bottom Sheet SHALL reuse the v1.2 Bottom Sheet interaction container, but SHALL present as "create mode".
The primary bottom action label SHALL be "创建".
In create mode, the Bottom Sheet SHALL NOT display destructive or structural operation entry points (including Delete, Merge, Split).

#### Scenario: Create mode hides dangerous actions
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the user views available actions
- **THEN** Delete/Merge/Split entry points are not shown

### Requirement: Default Time Prefill Rules (Create Mode)
When the create mode Bottom Sheet opens, the start time and end time SHALL be prefilled as "current time".
The prefilled times SHALL use minute precision (consistent with the v1.2 time wheel step) and SHALL align to a whole minute by flooring seconds (i.e., align to :00 seconds).
The date component SHALL be determined by the selected Timeline date:
- If the selected date is today, the system SHALL use the system current date and time.
- If the selected date is not today, the system SHALL use the selected date combined with the system current hour/minute.

#### Scenario: Selected date is today
- **GIVEN** the selected Timeline date is today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start time and end time show the system current time aligned to minute precision

#### Scenario: Selected date is not today
- **GIVEN** the selected Timeline date is not today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start time and end time use the selected date
- **AND** start/end hour and minute match the system current hour and minute

### Requirement: Create Time Constraint And Inline Validation
In create mode, the system SHALL require that end time is strictly later than start time.
If end time is less than or equal to start time, the primary "创建" action SHALL be disabled.
If end time is less than or equal to start time, the time area SHALL display a validation message: "结束时间需晚于起始时间".

#### Scenario: Invalid time disables create and shows message
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time is less than or equal to the start time
- **THEN** the "创建" button is disabled
- **AND** the validation message "结束时间需晚于起始时间" is shown in the time area

#### Scenario: Valid time enables create
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time becomes later than the start time
- **THEN** the "创建" button becomes enabled

### Requirement: Timeline Entry Bottom Sheet
The system SHALL display a fixed-height, non-draggable half-screen Bottom Sheet when the user taps a time entry card in the Timeline list while not in multi-select mode.
The Bottom Sheet SHALL be dismissible via a close button in the top-right of the sheet and via tapping the scrim area above the sheet.
The Bottom Sheet SHALL contain:
- A top icon action area (infrequent actions), including Merge Up, Merge Down, Delete, and Close.
- A main content editor area for the time entry.
- A bottom primary action area for Save and Split.

#### Scenario: Tap opens bottom sheet
- **GIVEN** the Timeline list renders a time entry card
- **AND** the system is not in multi-select mode
- **WHEN** the user taps the time entry card
- **THEN** a fixed-height Bottom Sheet is shown for that time entry

#### Scenario: Dismiss bottom sheet via close button
- **GIVEN** the Bottom Sheet is visible
- **WHEN** the user taps the close button
- **THEN** the Bottom Sheet is dismissed

#### Scenario: Dismiss bottom sheet via scrim tap
- **GIVEN** the Bottom Sheet is visible
- **WHEN** the user taps outside the sheet (the scrim area)
- **THEN** the Bottom Sheet is dismissed

### Requirement: Bottom Sheet Actions - Save Changes
The Bottom Sheet SHALL provide a primary Save action.
When the user selects Save, the system SHALL persist the edited time range, activity type, tags, and note for the time entry.
After saving, the Timeline list SHALL reflect the updated time entry.

#### Scenario: Save persists edits
- **GIVEN** the Bottom Sheet is visible for a time entry
- **AND** the user has changed one or more editable fields
- **WHEN** the user selects Save
- **THEN** the edits are persisted
- **AND** the Timeline list reflects the updated entry

### Requirement: Bottom Sheet Actions - Delete (Undo)
The Bottom Sheet top icon action area SHALL include a Delete action.
When the user selects Delete, the system SHALL remove the time entry and SHALL provide an Undo affordance (e.g., Snackbar).
If the user selects Undo, the system SHALL restore the removed time entry using the same time entry ID.

#### Scenario: Delete provides Undo
- **GIVEN** the Bottom Sheet is visible for a time entry
- **WHEN** the user selects Delete
- **THEN** the time entry is removed from the Timeline list
- **AND** the UI shows an Undo affordance

#### Scenario: Undo restores the deleted entry with same ID
- **GIVEN** the user deleted a time entry
- **AND** the Undo affordance is visible
- **WHEN** the user selects Undo
- **THEN** the time entry is restored with the same ID
- **AND** the Timeline list reflects the restored entry

### Requirement: Bottom Sheet Actions - Split
The Bottom Sheet SHALL provide a primary Split action.

#### Scenario: Split triggers split flow
- **GIVEN** the Bottom Sheet is visible for a time entry
- **WHEN** the user selects Split
- **THEN** the app opens the split flow for that time entry

### Requirement: Timeline Batch Actions - Merge
The system SHALL support merging time entries via a batch Merge action while in multi-select mode.
The Merge action SHALL be enabled only when the user has selected at least 2 time entries.

#### Scenario: Merge is disabled under threshold
- **GIVEN** the system is in multi-select mode
- **AND** the user has selected fewer than 2 time entries
- **WHEN** the batch actions are shown
- **THEN** the Merge action is disabled

#### Scenario: Merge applies and reflects in timeline
- **GIVEN** the system is in multi-select mode
- **AND** the user has selected at least 2 time entries
- **WHEN** the user confirms the merge
- **THEN** the selected time entries are merged into a single time entry
- **AND** the Timeline list updates to reflect the merge

### Requirement: Bottom Sheet Actions - Merge Up / Merge Down
The Bottom Sheet top icon action area SHALL include Merge Up and Merge Down actions.
The system SHALL define adjacency by chronological ordering of time entries within the same day group (sorted by start time ascending, ties broken by time entry ID).
Merge Up SHALL merge the current time entry with the immediately preceding time entry in that order.
Merge Down SHALL merge the current time entry with the immediately following time entry in that order.

#### Scenario: Merge Up merges with adjacent earlier entry
- **GIVEN** the Bottom Sheet is visible for a time entry
- **AND** there is an adjacent earlier time entry
- **WHEN** the user selects Merge Up
- **THEN** the app merges the current entry with the adjacent earlier entry
- **AND** the Timeline list reflects the merge

#### Scenario: Merge Down merges with adjacent later entry
- **GIVEN** the Bottom Sheet is visible for a time entry
- **AND** there is an adjacent later time entry
- **WHEN** the user selects Merge Down
- **THEN** the app merges the current entry with the adjacent later entry
- **AND** the Timeline list reflects the merge

### Requirement: Timeline Batch Actions - Delete (Undo)
The system SHALL provide a Delete action in the bottom batch action bar while in multi-select mode.
When the user selects Delete, the system SHALL remove the selected time entries and SHALL provide an Undo affordance (e.g., Snackbar).
If the user selects Undo, the system SHALL restore all removed time entries using the same time entry IDs.

#### Scenario: Batch delete provides Undo
- **GIVEN** the system is in multi-select mode
- **AND** the user has selected one or more time entries
- **WHEN** the user selects the batch Delete action
- **THEN** the selected time entries are removed from the Timeline list
- **AND** the UI shows an Undo affordance

#### Scenario: Undo restores the deleted entries with same IDs
- **GIVEN** the user batch-deleted one or more time entries
- **AND** the Undo affordance is visible
- **WHEN** the user selects Undo
- **THEN** the time entries are restored with the same IDs
- **AND** the Timeline list reflects the restored entries

### Requirement: Bottom Sheet Content - Time Range Editing
The Bottom Sheet SHALL provide start and end time controls in a single row using two side-by-side wheel pickers.
The time wheel pickers SHALL use a 1-minute step.
When the user changes either time value, the system SHALL update the displayed duration immediately.

#### Scenario: Duration updates immediately when time changes
- **GIVEN** the Bottom Sheet is visible for a time entry with a duration
- **WHEN** the user changes the start time using the wheel picker
- **THEN** the displayed duration updates immediately

### Requirement: Bottom Sheet Content - Activity Type Quick Selection
The Bottom Sheet SHALL display a frequent activity type list as chip-style controls.
The frequent activity type list SHALL be presented in two fixed rows.
The Bottom Sheet SHALL include a "More" entry at the end of the second row to open the full activity type selector.

#### Scenario: Selecting an activity type chip updates the draft
- **GIVEN** the Bottom Sheet is visible for a time entry
- **WHEN** the user taps an activity type chip
- **THEN** the entry's draft activity type is updated

#### Scenario: More opens the full activity type selector
- **GIVEN** the Bottom Sheet is visible for a time entry
- **WHEN** the user taps the "More" activity type entry
- **THEN** the app opens the full activity type selector

### Requirement: Bottom Sheet Content - Tags
The Bottom Sheet SHALL provide an "Add tag" entry point.
When one or more tags are selected, the Bottom Sheet SHALL display the selected tags as chips.
When the selected tag count exceeds the display limit, the Bottom Sheet MAY collapse the remaining tags into a "+X" indicator.

#### Scenario: Add tag results in tag chip display
- **GIVEN** the Bottom Sheet is visible for a time entry
- **WHEN** the user adds a tag
- **THEN** the added tag is displayed as a chip in the Bottom Sheet

### Requirement: Bottom Sheet Content - Note
The Bottom Sheet SHALL provide a multi-line note input area with placeholder text when empty.
The note input area SHALL have a minimum height.

#### Scenario: Note input shows placeholder when empty
- **GIVEN** the Bottom Sheet is visible for a time entry with an empty note
- **WHEN** the note input area is shown
- **THEN** placeholder text is displayed

