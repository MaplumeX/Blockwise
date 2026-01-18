# timeline-date-navigation Specification

## Purpose
TBD - created by archiving change add-timeline-date-navigation. Update Purpose after archive.
## Requirements
### Requirement: Timeline Week Range Title
The system SHALL render a week range title on the Timeline screen that reflects the week containing the currently selected date.

#### Scenario: Display current week range
- **GIVEN** the currently selected date is within a week
- **WHEN** the Timeline screen is rendered
- **THEN** the title displays the start and end dates of that week

#### Scenario: Week range title includes year only when ambiguous
- **GIVEN** the week range display would be ambiguous without a year (e.g., spans across different years)
- **WHEN** the Timeline screen is rendered
- **THEN** the title includes the year in its formatted text

### Requirement: Timeline Week Strip Day Selection
The system SHALL provide a week strip of 7 selectable day buttons for the week containing the currently selected date, and SHALL update the selected date and timeline data when a day is selected.

#### Scenario: Select a day from the week strip
- **GIVEN** the week strip is visible
- **WHEN** the user taps a day button
- **THEN** the selected date becomes that day
- **AND** the Timeline screen renders data for the selected date

### Requirement: Preserve Selected Weekday When Paging Weeks
The system SHALL allow paging to the previous/next week and SHALL preserve the selected weekday semantics when changing weeks.

#### Scenario: Page to previous week preserves weekday
- **GIVEN** the selected date is a Friday
- **WHEN** the user navigates to the previous week
- **THEN** the selected date becomes the Friday of the previous week

#### Scenario: Page to next week preserves weekday
- **GIVEN** the selected date is a Friday
- **WHEN** the user navigates to the next week
- **THEN** the selected date becomes the Friday of the next week

### Requirement: Calendar Jump From Week Range Title
The system SHALL open a calendar date picker when the user taps the week range title, and SHALL update the selected date when the user chooses a date.

#### Scenario: Open date picker from title
- **WHEN** the user taps the week range title
- **THEN** a calendar date picker is shown

#### Scenario: Select date from date picker
- **GIVEN** the calendar date picker is shown
- **WHEN** the user selects a date
- **THEN** the selected date becomes the chosen date
- **AND** the Timeline screen renders data for the selected date

### Requirement: Today Shortcut
The system SHALL provide a "today" shortcut located next to the week range title that sets the selected date to the current system date.

#### Scenario: Jump to today
- **WHEN** the user triggers the today shortcut
- **THEN** the selected date becomes the current system date
- **AND** the week range title and week strip update accordingly

### Requirement: Smooth Transition Without Wrong-Date Data
The system SHALL provide a smooth transition when the selected date changes and SHALL avoid rendering timeline data for an incorrect date during the transition.

#### Scenario: Date switch does not show wrong-date content
- **GIVEN** the Timeline screen is showing data for date A
- **WHEN** the selected date changes to date B
- **THEN** the UI does not temporarily render time entries that belong to a date other than date A or date B
- **AND** the final rendered state corresponds to date B

