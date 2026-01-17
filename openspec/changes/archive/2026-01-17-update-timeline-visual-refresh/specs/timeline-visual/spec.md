## ADDED Requirements

### Requirement: Timeline Visual Axis
The system SHALL render a vertical time axis for each time entry item in the Timeline list, including a connector line and a node dot, and SHALL adapt its colors for light and dark themes.

#### Scenario: Render axis in light theme
- **WHEN** the user opens the Timeline screen in light theme
- **THEN** each time entry item shows a visible connector line and a node dot
- **AND** the axis has sufficient contrast against the list background

#### Scenario: Render axis in dark theme
- **WHEN** the user opens the Timeline screen in dark theme
- **THEN** each time entry item shows a visible connector line and a node dot
- **AND** the axis has sufficient contrast against the list background

### Requirement: Timeline Entry Card Information Structure
The system SHALL update the Timeline entry card layout as follows:
- Remove the left-side activity color strip indicator.
- Show the activity type label at the top-right of the card.
- Use the note as the primary title, and fall back to the activity type name when note is empty.
- Show the start and end time below the title.

#### Scenario: Card title uses note when present
- **WHEN** a time entry has a non-empty note
- **THEN** the card title displays the note

#### Scenario: Card title falls back to activity type
- **WHEN** a time entry has an empty note
- **THEN** the card title displays the activity type name

#### Scenario: Card shows activity type and time range
- **WHEN** a time entry is rendered in the Timeline list
- **THEN** the card shows the activity type label at the top-right
- **AND** the card shows the start and end time below the title

### Requirement: Time Range Chip Style
The system SHALL display the time range in a monospace style with a subtle background chip.

#### Scenario: Time range uses monospace
- **WHEN** a time entry time range is displayed
- **THEN** the time text is rendered using a monospace font

#### Scenario: Time range is displayed as a chip
- **WHEN** a time entry time range is displayed
- **THEN** the time text is wrapped by a background block with rounded corners
- **AND** the chip padding is visually consistent (6dp horizontal, 3dp vertical)
