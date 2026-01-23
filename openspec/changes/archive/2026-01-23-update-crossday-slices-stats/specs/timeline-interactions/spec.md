## ADDED Requirements

### Requirement: Timeline Tap Day Slice Opens Original TimeEntry
When the user taps a displayed Day Slice item in the Timeline list, the system SHALL open the time entry detail/edit flow for the original TimeEntry.
The detail/edit UI SHALL show the full start and end timestamps of the original TimeEntry.

#### Scenario: Tap slice opens edit with full range
- **GIVEN** a Day Slice is displayed for a time entry
- **WHEN** the user taps the Day Slice
- **THEN** the time entry detail/edit flow opens
- **AND** it targets the original TimeEntry
- **AND** it displays the original full start and end timestamps
