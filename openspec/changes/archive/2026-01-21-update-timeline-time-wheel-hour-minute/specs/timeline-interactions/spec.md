## MODIFIED Requirements

### Requirement: Bottom Sheet Content - Time Range Editing
The Bottom Sheet SHALL provide start and end time controls in a single row.
Each time control (start and end) SHALL use wheel-based pickers composed of two aligned wheel columns:
- Hour wheel: values 00–23 with a 1-hour step.
- Minute wheel: values 00–59 with a 1-minute step.
The selected time for each control SHALL be displayed in `HH:mm` format using a monospace font and SHALL update immediately as the wheels change.
When the user changes either time value, the system SHALL update the displayed duration immediately.

#### Scenario: Start and end time controls each have hour and minute wheels
- **GIVEN** the Bottom Sheet is visible
- **WHEN** the user views the start time control
- **THEN** the start time control shows two wheel columns: hour (00–23) and minute (00–59)
- **AND** the hour wheel steps by 1 hour and the minute wheel steps by 1 minute
- **WHEN** the user views the end time control
- **THEN** the end time control shows two wheel columns: hour (00–23) and minute (00–59)

#### Scenario: Selected time updates immediately in HH:mm as wheels change
- **GIVEN** the Bottom Sheet is visible
- **WHEN** the user scrolls the hour wheel for a time control
- **THEN** the displayed selected time updates immediately in `HH:mm` format
- **WHEN** the user scrolls the minute wheel for a time control
- **THEN** the displayed selected time updates immediately in `HH:mm` format

#### Scenario: Duration updates immediately when time changes
- **GIVEN** the Bottom Sheet is visible for a time entry with a duration
- **WHEN** the user changes the start time using the wheel pickers
- **THEN** the displayed duration updates immediately
