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

#### Scenario: Time range uses monospace
- **WHEN** a time entry time range is displayed
- **THEN** the time text is rendered using a monospace font

#### Scenario: Time range is displayed as a chip
- **WHEN** a time entry time range is displayed
- **THEN** the time text is wrapped by a background block with rounded corners
- **AND** the chip padding is visually consistent (6dp horizontal, 3dp vertical)

#### Scenario: Seconds are truncated in time range display
- **GIVEN** a time entry start time of 09:00:59
- **AND** a time entry end time of 09:01:01
- **WHEN** the time range is displayed in the Timeline list
- **THEN** the start time is shown as 09:00
- **AND** the end time is shown as 09:01
- **AND** no seconds are displayed
- **AND** the displayed minutes are not rounded based on seconds

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

