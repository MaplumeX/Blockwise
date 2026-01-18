## MODIFIED Requirements
### Requirement: Time Block Day Grid Layout (24x12)
The system SHALL render a Time Block day grid with a fixed time axis and a scrollable content area.
The day grid SHALL represent 24 hours as rows, and SHALL represent minutes within an hour as 12 fixed columns (5-minute granularity), for a total of 24 x 12 = 288 time cells.
The system SHALL support only vertical scrolling to browse all 24 hours and SHALL NOT support horizontal scrolling.

#### Scenario: Render horizontal grid with fixed time axis
- **WHEN** the user views a day in the Time Block view
- **THEN** the UI shows a fixed vertical time axis for hours 00–23
- **AND** each hour is rendered as one row
- **AND** each row renders 12 columns representing 5-minute increments
- **AND** the user can scroll vertically to view all 24 hours
- **AND** the UI does not provide horizontal scrolling for the grid

## MODIFIED Requirements
### Requirement: Segment Capsule Rendering
The system SHALL render each time entry as one or more segment capsules covering its time range on the 5-minute grid.
For entries spanning across hours, the system SHALL slice the segment per hour row while preserving visual continuity (same color, aligned edges).
Within a given hour row, the segment capsule SHALL extend horizontally across the minute columns that fall within the entry's time range.
The system SHALL avoid per-cell rendering of time entries and SHALL prefer rendering by continuous ranges.

#### Scenario: Render entry as horizontal segment within an hour row
- **GIVEN** a time entry that starts and ends within the same hour
- **WHEN** the day grid is rendered
- **THEN** the entry appears as a horizontal segment capsule within that hour row
- **AND** the capsule spans the corresponding 5-minute columns

#### Scenario: Render entry spanning multiple hour rows
- **GIVEN** a time entry that spans across multiple hours
- **WHEN** the day grid is rendered
- **THEN** the entry is sliced into one segment per hour row
- **AND** all slices share the same visual identity (e.g., color)
- **AND** the overall appearance remains visually continuous across rows

#### Scenario: Range-based rendering
- **WHEN** multiple time entries are rendered on the grid
- **THEN** the UI renders entries as range segments rather than rendering individual 5-minute cells for each entry

## ADDED Requirements
### Requirement: Fixed 5-Minute Granularity
The Time Block day view SHALL use a fixed 5-minute granularity for its grid and interactions.
The system SHALL NOT provide a user-configurable granularity setting in v1.2.

#### Scenario: Fixed granularity
- **WHEN** the user views the Time Block day view
- **THEN** the grid uses 5-minute increments
- **AND** the user cannot change the grid granularity
