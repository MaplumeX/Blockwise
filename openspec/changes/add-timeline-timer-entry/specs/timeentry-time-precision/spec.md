## ADDED Requirements

### Requirement: Timer Stop Creates Valid TimeEntry (Min 1 Second)
When the user stops a timer and the system creates a time entry, the end timestamp MUST be strictly later than the start timestamp.
If the captured end timestamp is less than or equal to the start timestamp, the system SHALL set end to start plus 1 second to ensure validity.

#### Scenario: End equals start is corrected to +1s
- **GIVEN** a timer start timestamp T
- **WHEN** the user stops the timer and the system captures an end timestamp that is less than or equal to T
- **THEN** the created time entry end timestamp is set to T + 1 second
- **AND** the created time entry is considered valid (end > start)

### Requirement: Timer Stop Splits Cross-Day TimeEntry
If a timer time range crosses a natural day boundary (24:00:00), the system SHALL split the result into multiple time entries such that no single created time entry crosses a day boundary.
The split boundary SHALL be at 24:00:00 / 00:00:00.
All created time entries SHALL preserve second-level precision.

#### Scenario: Timer crosses midnight and is split
- **GIVEN** a timer start timestamp at 23:59:59 on day D
- **WHEN** the user stops the timer at 00:00:10 on day D+1
- **THEN** the system creates two time entries
- **AND** the first entry is 23:59:59–24:00:00 on day D
- **AND** the second entry is 00:00:00–00:00:10 on day D+1
