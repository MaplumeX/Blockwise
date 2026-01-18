## MODIFIED Requirements
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
