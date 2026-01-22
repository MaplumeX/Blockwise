# timeentry-time-precision Specification

## Purpose
TBD - created by archiving change add-timeentry-second-precision. Update Purpose after archive.
## Requirements
### Requirement: TimeEntry Timestamp Precision (Seconds)
The system SHALL store time entry start and end timestamps with at least second-level precision.
The stored timestamps SHALL preserve the seconds component of the captured time (i.e., not rounded to minute precision).

#### Scenario: Timer creates a TimeEntry with seconds preserved
- **GIVEN** the user starts a timer for an activity
- **WHEN** the user stops the timer and chooses to save the result as a time entry
- **THEN** the created time entry start and end timestamps include the seconds component as captured by the system clock

#### Scenario: Repository persists Instant timestamps without minute rounding
- **GIVEN** a time entry is created or updated with `Instant` timestamps
- **WHEN** the entry is persisted via the data layer
- **THEN** the persisted timestamps retain epoch-millisecond values (and therefore at least second-level precision)

### Requirement: TimeEntry Validity Uses Second-Level Comparison
A time entry time range SHALL be considered valid only when the end timestamp is strictly later than the start timestamp.
The comparison SHALL use second-level precision or finer.
A time entry SHALL be considered valid even when its duration is less than 1 minute (as long as end > start).

#### Scenario: End equals start is invalid
- **GIVEN** a draft time entry time range where end timestamp equals start timestamp
- **WHEN** the user attempts to create or save the entry
- **THEN** the operation is rejected as invalid

#### Scenario: End is 1 second after start is valid
- **GIVEN** a draft time entry time range where end timestamp is exactly 1 second later than the start timestamp
- **WHEN** the user attempts to create or save the entry
- **THEN** the operation is accepted as valid
- **AND** the entry is not rejected solely due to being under 1 minute long

### Requirement: Minute-Precision Editing Preserves Seconds
When the user edits a time entry using a minute-precision time picker, the system SHALL preserve the existing seconds component of that timestamp.

#### Scenario: Editing start time preserves seconds
- **GIVEN** an existing time entry start time of 09:00:45
- **WHEN** the user edits the start time to 10:00 using a minute-precision control
- **THEN** the saved start time becomes 10:00:45

#### Scenario: Editing end time preserves seconds
- **GIVEN** an existing time entry end time of 11:30:05
- **WHEN** the user edits the end time to 12:00 using a minute-precision control
- **THEN** the saved end time becomes 12:00:05

### Requirement: Timer Writes System Current Timestamps
When creating a time entry from the timer feature, the system SHALL write the start timestamp at timer start.
When stopping the timer and creating a time entry, the system SHALL write the end timestamp using the system current time.
The timer-to-entry save flow SHALL allow saving entries with a duration greater than 0 seconds.

#### Scenario: Start time is captured at timer start
- **WHEN** the user starts a timer
- **THEN** the timer start timestamp is captured from the system clock

#### Scenario: End time is captured at timer stop
- **GIVEN** the timer is running
- **WHEN** the user stops the timer and creates a time entry
- **THEN** the time entry end timestamp is captured from the system clock

#### Scenario: Timer save allows entries under 1 minute
- **GIVEN** the user runs a timer for 10 seconds
- **WHEN** the user stops the timer and chooses to save
- **THEN** a time entry is created successfully

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

