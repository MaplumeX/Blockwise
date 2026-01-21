## ADDED Requirements

### Requirement: Bottom Sheet Time Picker - Hour/Minute Split Wheels
The Timeline entry Bottom Sheet time range editor SHALL use wheel-based pickers for both start time and end time.
Each time picker (start and end) SHALL be composed of two aligned wheel columns:
- Hour wheel: values 00–23 with a 1-hour step.
- Minute wheel: values 00–59 with a 1-minute step.
The start and end time pickers SHALL be laid out side-by-side in a single row.
The selected time for each picker SHALL be displayed in `HH:mm` format using a monospace font and SHALL update immediately as the wheels change.

#### Scenario: Start and end time pickers each have hour and minute wheels
- **GIVEN** the Timeline entry Bottom Sheet is visible
- **WHEN** the user views the start time picker
- **THEN** the start time picker shows two wheel columns: hour (00–23) and minute (00–59)
- **AND** the hour wheel steps by 1 hour and the minute wheel steps by 1 minute
- **WHEN** the user views the end time picker
- **THEN** the end time picker shows two wheel columns: hour (00–23) and minute (00–59)

#### Scenario: Selected time updates in HH:mm as wheels change
- **GIVEN** the Timeline entry Bottom Sheet is visible
- **WHEN** the user scrolls the hour wheel for a time picker
- **THEN** the displayed selected time updates immediately in `HH:mm` format
- **WHEN** the user scrolls the minute wheel for a time picker
- **THEN** the displayed selected time updates immediately in `HH:mm` format
