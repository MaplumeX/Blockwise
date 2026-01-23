# timeline-visual Specification

## Purpose
TBD - created by archiving change update-timeline-visual-refresh. Update Purpose after archive.
## Requirements
### Requirement: Timeline Visual Axis
The system SHALL render a vertical time axis for each time entry item in the Timeline list, including a connector line and a node dot, and SHALL adapt its colors for light and dark themes.

#### Scenario: Render axis in light theme
- **WHEN** the user opens the Timeline screen in light theme
- **THEN** each time entry item shows a visible connector line and a node dot
- **AND** the axis has sufficient contrast against the list background

#### Scenario: Render axis in dark theme
- **WHEN** the user opens the Timeline screen in dark theme
- **THEN** each time entry item shows a visible connector line and a node dot
- **AND** the axis has sufficient contrast against the list background

### Requirement: Timeline Entry Card Information Structure
The system SHALL update the Timeline entry card layout as follows:
- Remove the left-side activity color strip indicator.
- Show the activity type label at the top-right of the card.
- Use the note as the primary title, and fall back to the activity type name when note is empty.
- Show the start and end time below the title.

#### Scenario: Card title uses note when present
- **WHEN** a time entry has a non-empty note
- **THEN** the card title displays the note

#### Scenario: Card title falls back to activity type
- **WHEN** a time entry has an empty note
- **THEN** the card title displays the activity type name

#### Scenario: Card shows activity type and time range
- **WHEN** a time entry is rendered in the Timeline list
- **THEN** the card shows the activity type label at the top-right
- **AND** the card shows the start and end time below the title

### Requirement: Time Range Chip Style
The system SHALL display the time range in a monospace style with a subtle background chip.
The time range text SHALL be displayed at minute precision in `HH:mm` format.
The display logic SHALL truncate (hide) seconds without rounding.
When the displayed end time equals a natural day boundary (i.e., the next day at 00:00:00 for the current day group), the system MAY display the end time as `24:00` for that day group.
The `24:00` representation SHALL be display-only and SHALL NOT affect the stored data.

#### Scenario: Seconds are truncated in time range display
- **GIVEN** a time entry start time of 09:00:59
- **AND** a time entry end time of 09:01:01
- **WHEN** the time range is displayed in the Timeline list
- **THEN** the start time is shown as 09:00
- **AND** the end time is shown as 09:01
- **AND** no seconds are displayed
- **AND** the displayed minutes are not rounded based on seconds

#### Scenario: Display end time as 24:00 at day boundary
- **GIVEN** a displayed day group for date D
- **AND** a Day Slice that ends exactly at D+1 00:00:00
- **WHEN** the time range is displayed for that Day Slice
- **THEN** the end time MAY be shown as 24:00

### Requirement: Timeline Untracked Gap Cards
The system SHALL detect untracked time gaps in the Timeline list and SHALL render an "untracked gap" card when the gap duration is at least 1 minute.
The gap detection window for a given day group SHALL include:
- Gaps between adjacent time entries within the same day group.
- The gap from 00:00 to the first entry start time (when the first entry does not start at 00:00).
- The gap from the last entry end time to 24:00 (when the last entry does not end at 24:00).

#### Scenario: Insert untracked gap card between entries
- **GIVEN** two adjacent time entries within the same day group
- **AND** the later entry ends at 10:00
- **AND** the earlier entry starts at 10:05
- **WHEN** the Timeline list is rendered
- **THEN** an untracked gap card is shown between them
- **AND** the card displays the time range 10:00–10:05

#### Scenario: Do not insert gap card under threshold
- **GIVEN** two adjacent time entries within the same day group
- **AND** the gap between them is 0 minutes
- **WHEN** the Timeline list is rendered
- **THEN** no untracked gap card is shown

#### Scenario: Do not insert gap card for overlapping entries
- **GIVEN** two adjacent time entries within the same day group
- **AND** the later entry overlaps the earlier entry
- **WHEN** the Timeline list is rendered
- **THEN** no untracked gap card is shown

#### Scenario: Insert gap card at start of day
- **GIVEN** a day group where the first time entry starts at 09:00
- **WHEN** the Timeline list is rendered
- **THEN** an untracked gap card is shown before the first entry
- **AND** the card displays the time range 00:00–09:00

#### Scenario: Insert gap card at end of day
- **GIVEN** a day group where the last time entry ends at 18:30
- **WHEN** the Timeline list is rendered
- **THEN** an untracked gap card is shown after the last entry
- **AND** the card displays the time range 18:30–24:00

#### Scenario: Do not insert start-of-day gap card when first entry starts at midnight
- **GIVEN** a day group where the first time entry starts at 00:00
- **WHEN** the Timeline list is rendered
- **THEN** no start-of-day untracked gap card is shown

#### Scenario: Do not insert end-of-day gap card when last entry ends at midnight
- **GIVEN** a day group where the last time entry ends at 24:00
- **WHEN** the Timeline list is rendered
- **THEN** no end-of-day untracked gap card is shown

### Requirement: Create Time Entry From Untracked Gap
The system SHALL allow users to create a new time entry from an untracked gap card by navigating to the create time entry flow with the gap start and end times prefilled.

#### Scenario: Tap gap card to create entry with prefilled times
- **GIVEN** an untracked gap card is rendered with a start time and end time
- **WHEN** the user taps the untracked gap card
- **THEN** the app navigates to the create time entry screen
- **AND** the create screen pre-fills the gap start time and end time

### Requirement: Timeline Shows Running Timer Entry
When a timer is running for the selected date (today), the Timeline list SHALL show a visible "running timer" entry to provide immediate feedback.
The running timer entry SHALL display the selected activity type.
The running timer entry start time SHALL be the time captured when the activity type selection is confirmed.
The running timer entry end time SHALL be presented with an "ongoing" semantics (e.g., "现在" / "进行中") and SHALL update as the current time advances.
The running timer entry SHALL include a clear status indicator (e.g., "计时中" / "进行中").
When the timer is completed, the running timer entry SHALL transition into a normal time entry within the same day group.

#### Scenario: Running timer entry is shown after starting
- **GIVEN** the user starts a timer from Timeline Today by selecting an activity type
- **WHEN** the Timeline list is rendered
- **THEN** a running timer entry is shown
- **AND** it shows the selected activity type
- **AND** it shows a "计时中" status indicator

#### Scenario: Running timer entry end updates over time
- **GIVEN** a running timer entry is shown
- **WHEN** time advances
- **THEN** the running timer entry end time representation updates to reflect the current time with ongoing semantics

#### Scenario: Running timer entry becomes normal entry after completion
- **GIVEN** a running timer is running and the running timer entry is shown
- **WHEN** the user stops the timer
- **THEN** the running timer entry is replaced by a normal time entry within the same day group

### Requirement: Timeline Displays Cross-Day Entries As Day Slices
When viewing the Timeline list for a selected date D, the system SHALL display all TimeEntries that overlap date D.
For a TimeEntry that spans multiple natural days, the system SHALL display one Day Slice in each covered day group.
Within a day group, items SHALL be ordered by `sliceStart` ascending.
The day group's untracked gap detection SHALL remain consistent with the day group's ordered Day Slices.

#### Scenario: Cross-day entry appears on both days
- **GIVEN** a time entry from 23:00 on day D to 01:00 on day D+1
- **WHEN** viewing the Timeline list for day D
- **THEN** a Day Slice for the entry is shown in day D group as 23:00–24:00
- **WHEN** viewing the Timeline list for day D+1
- **THEN** a Day Slice for the entry is shown in day D+1 group as 00:00–01:00

#### Scenario: Day group ordering uses sliceStart
- **GIVEN** multiple Day Slices in the same day group
- **WHEN** the Timeline list is rendered
- **THEN** the items are ordered by `sliceStart` ascending

### Requirement: Timeline Shows Cross-Day Marker On Day Slice
For a TimeEntry whose start and end timestamps are not on the same natural date, the system SHALL show a cross-day marker on the rendered Day Slice.
The marker SHALL expose the original TimeEntry's full start and end time points including dates.
The marker SHALL be lightweight and SHALL NOT significantly increase list reading burden.

#### Scenario: Marker shows original time range with dates
- **GIVEN** a time entry from 23:00 on day D to 01:00 on day D+1
- **WHEN** a Day Slice for that entry is displayed
- **THEN** a cross-day marker is visible
- **AND** the marker indicates the original range with date and time (e.g., "D 23:00 – D+1 01:00")

### Requirement: Day Slice Edits Target Original TimeEntry
The system SHALL treat Day Slice as a display-only concept.
When the user opens detail or edit from a Day Slice, the system SHALL target the original TimeEntry for all create/update/delete actions.
The edit UI SHALL display the original TimeEntry's full start and end range including cross-day information.

#### Scenario: Tap Day Slice opens edit for original entry
- **GIVEN** a Day Slice rendered for a cross-day time entry
- **WHEN** the user taps the Day Slice
- **THEN** the app opens the time entry detail/edit flow
- **AND** the edited object is the original TimeEntry (not the Day Slice)
- **AND** the edit UI displays the full original start and end timestamps

