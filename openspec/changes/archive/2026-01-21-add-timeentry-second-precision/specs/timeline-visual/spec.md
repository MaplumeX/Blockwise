## MODIFIED Requirements

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
