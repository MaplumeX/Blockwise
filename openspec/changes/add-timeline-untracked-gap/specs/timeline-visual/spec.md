## ADDED Requirements
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
