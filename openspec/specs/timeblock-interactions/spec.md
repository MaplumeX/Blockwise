# timeblock-interactions Specification

## Purpose
TBD - created by archiving change add-timeblock-core-interactions. Update Purpose after archive.
## Requirements
### Requirement: Select Time Entry Segment
The system SHALL allow selecting a time entry by tapping on any of its rendered segments in the Time Block day view.
When a time entry spans multiple hour rows, all segments belonging to that entry SHALL reflect the selected state together.

#### Scenario: Tap segment selects the whole entry
- **GIVEN** a time entry is rendered as one or more segments
- **WHEN** the user taps any segment of the entry
- **THEN** the entry becomes selected
- **AND** all segments of the entry show the selected state

### Requirement: Time Entry Detail Panel
When a time entry is selected in the Time Block day view, the system SHALL show a detail panel anchored at the bottom.
The detail panel SHALL display the activity, time range, duration, note, and tags.

#### Scenario: Show detail panel for selected entry
- **GIVEN** a time entry is selected
- **WHEN** the Time Block day view is visible
- **THEN** a bottom detail panel is shown
- **AND** the panel shows the selected entry details

### Requirement: Range Create With 5-Minute Snap
The system SHALL allow creating a new time entry by long-pressing on an empty area and dragging to select a time range.
The selected range boundaries SHALL snap to the nearest 5-minute grid.
On release, the system SHALL navigate to the create time entry flow with the selected start and end times prefilled.
Creating a time entry from an empty area SHALL be available only via the long-press and drag gesture.

#### Scenario: Drag to select range on empty area
- **GIVEN** the user long-presses on an empty area in the day grid
- **WHEN** the user drags to select a range and releases
- **THEN** the system computes a start and end aligned to 5-minute increments
- **AND** the create flow opens with the aligned start and end prefilled

#### Scenario: Tap on empty area does not create
- **GIVEN** the Time Block day view is visible
- **WHEN** the user taps on an empty area of the day grid
- **THEN** the system does not start a create flow

### Requirement: Clear Selection Feedback
The system SHALL provide clear visual feedback for selection state.
Non-selected segments MAY be visually de-emphasized while maintaining readability.

#### Scenario: Selected segment is clearly distinguished
- **GIVEN** one entry is selected
- **WHEN** the day grid is rendered
- **THEN** the selected entry is clearly distinguished from others

### Requirement: Cross-Day Marker Is Shown At Most Once Per Entry In Time Block
When a cross-day TimeEntry is rendered as multiple hour-row segments in the Time Block day view, the system SHALL show the cross-day marker at most once for that entry within the selected day.

#### Scenario: Marker shown only once across hour segments
- **GIVEN** a cross-day time entry rendered as multiple hour segments within the selected day
- **WHEN** the Time Block day view is rendered
- **THEN** the cross-day marker is shown on at most one segment

