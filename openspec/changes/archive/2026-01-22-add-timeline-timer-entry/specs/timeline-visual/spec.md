## ADDED Requirements

### Requirement: Timeline Shows Running Timer Entry
When a timer is running for the selected date (today), the Timeline list SHALL show a visible "running timer" entry to provide immediate feedback.
The running timer entry SHALL display the selected activity type.
The running timer entry start time SHALL be the time captured when the activity type selection is confirmed.
The running timer entry end time SHALL be presented with an "ongoing" semantics (e.g., "现在" / "进行中") and SHALL update as the current time advances.
The running timer entry SHALL include a clear status indicator (e.g., "计时中" / "进行中").
When the timer is completed, the running timer entry SHALL transition into a normal time entry within the same day group.

#### Scenario: Running timer entry is shown after starting
- **GIVEN** the user starts a timer from Timeline Today by selecting an activity type
- **WHEN** the Timeline list is rendered
- **THEN** a running timer entry is shown
- **AND** it shows the selected activity type
- **AND** it shows a "计时中" status indicator

#### Scenario: Running timer entry end updates over time
- **GIVEN** a running timer entry is shown
- **WHEN** time advances
- **THEN** the running timer entry end time representation updates to reflect the current time with ongoing semantics

#### Scenario: Running timer entry becomes normal entry after completion
- **GIVEN** a running timer is running and the running timer entry is shown
- **WHEN** the user stops the timer
- **THEN** the running timer entry is replaced by a normal time entry within the same day group
