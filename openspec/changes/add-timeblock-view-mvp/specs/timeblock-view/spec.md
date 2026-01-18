## ADDED Requirements

### Requirement: Timeline View Toggle (List vs Time Block)
The system SHALL provide a view toggle on the Timeline entry point to switch between the Timeline list view and the Time Block day view.
The toggle SHALL change only the content area while preserving the Timeline date navigation context.

#### Scenario: Switch from list to time block
- **GIVEN** the Timeline screen is showing the list view for a selected date
- **WHEN** the user triggers the view toggle
- **THEN** the Timeline content switches to the Time Block day view for the same selected date

#### Scenario: Switch from time block to list
- **GIVEN** the Timeline screen is showing the Time Block day view for a selected date
- **WHEN** the user triggers the view toggle
- **THEN** the Timeline content switches to the list view for the same selected date

### Requirement: Persist Timeline View Preference
The system SHALL persist the user's last selected Timeline view mode (list vs time block) and SHALL restore it on next entry to the Timeline screen.

#### Scenario: Restore last view mode
- **GIVEN** the user previously selected the Time Block view
- **WHEN** the user enters the Timeline screen again
- **THEN** the Timeline screen opens in the Time Block view

### Requirement: Time Block Day Grid Layout (24x12)
The system SHALL render a Time Block day grid with a fixed time axis and a scrollable content area.
The day grid SHALL represent 24 hours with 12 cells per hour (5-minute granularity), for a total of 288 time cells.

#### Scenario: Render day grid with fixed time axis
- **WHEN** the user views a day in the Time Block view
- **THEN** the UI shows a fixed vertical time axis for hours 00–23
- **AND** the UI shows a 5-minute grid aligned to the time axis
- **AND** the user can scroll vertically to view all 24 hours

### Requirement: Segment Capsule Rendering
The system SHALL render each time entry as one or more segment capsules covering its time range on the 5-minute grid.
For entries spanning across hours, the system SHALL slice the segment per hour row while preserving visual continuity (same color, aligned edges).
The system SHALL avoid per-cell rendering of time entries and SHALL prefer rendering by continuous ranges.

#### Scenario: Render entry spanning multiple hours
- **GIVEN** a time entry that spans across multiple hours
- **WHEN** the day grid is rendered
- **THEN** the entry appears as a visually continuous segment across the hour rows

#### Scenario: Range-based rendering
- **WHEN** multiple time entries are rendered on the grid
- **THEN** the UI renders entries as range segments rather than rendering individual 5-minute cells for each entry

### Requirement: Shared Data Source and Selected Date
The Timeline list view and the Time Block view SHALL share the same data source and the same selected date semantics.
Changes made in one view (create, edit, delete) SHALL be reflected in the other view without manual refresh.

#### Scenario: Edit reflects across views
- **GIVEN** the user edits a time entry in one view
- **WHEN** the user switches to the other view
- **THEN** the updated time entry is visible in the other view
