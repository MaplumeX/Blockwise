## MODIFIED Requirements

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

## ADDED Requirements

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
