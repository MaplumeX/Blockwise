## MODIFIED Requirements

### Requirement: Segment Capsule Rendering
The system SHALL render each time entry as one or more segment capsules covering its time range on the 5-minute grid.
For entries spanning across hours, the system SHALL slice the segment per hour row while preserving visual continuity (same color, aligned edges).
Within a given hour row, the segment capsule SHALL extend horizontally across the minute columns that fall within the entry's time range.
The system SHALL avoid per-cell rendering of time entries and SHALL prefer rendering by continuous ranges.

For entries that overlap the selected date D but span across natural days, the system SHALL render only the portion that overlaps date D in the day grid.
The non-overlapping portion SHALL be rendered when viewing the other affected dates.

#### Scenario: Render entry spanning multiple hour rows
- **GIVEN** a time entry that spans across multiple hours
- **WHEN** the day grid is rendered
- **THEN** the entry is sliced into one segment per hour row
- **AND** all slices share the same visual identity (e.g., color)
- **AND** the overall appearance remains visually continuous across rows

#### Scenario: Cross-day entry renders only the overlapped portion for selected day
- **GIVEN** the selected date is day D
- **AND** a time entry from 23:00 on day D to 01:00 on day D+1
- **WHEN** the day grid is rendered
- **THEN** the entry is rendered as 23:00–24:00 on day D

## ADDED Requirements

### Requirement: Time Block Day View Renders Overlapped Day Slices
When viewing the Time Block day view for selected date D, the system SHALL render all TimeEntries that overlap date D.
Cross-day entries SHALL be naturally shown as the portion overlapped with date D.

#### Scenario: Cross-day entry appears in both days
- **GIVEN** a time entry from 23:00 on day D to 01:00 on day D+1
- **WHEN** viewing Time Block day view for day D
- **THEN** the entry is rendered for the 23:00–24:00 portion
- **WHEN** viewing Time Block day view for day D+1
- **THEN** the entry is rendered for the 00:00–01:00 portion
