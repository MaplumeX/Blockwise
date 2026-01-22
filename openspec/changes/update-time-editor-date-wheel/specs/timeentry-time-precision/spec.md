## ADDED Requirements

### Requirement: No Maximum Duration Limit For TimeEntry
The system SHALL NOT enforce a maximum duration limit for a single TimeEntry.
The only fundamental validity constraint for a time range SHALL be that the end timestamp is strictly later than the start timestamp.

#### Scenario: Creating an entry longer than 24 hours is allowed
- **GIVEN** a draft time entry time range with a duration longer than 24 hours
- **WHEN** the user attempts to create the entry
- **THEN** the operation is accepted as valid as long as end timestamp is strictly later than start timestamp

#### Scenario: Updating an entry to be longer than 24 hours is allowed
- **GIVEN** an existing time entry
- **WHEN** the user updates the entry to a time range with a duration longer than 24 hours
- **THEN** the operation is accepted as valid as long as end timestamp is strictly later than start timestamp