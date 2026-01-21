## ADDED Requirements

### Requirement: Timeline Today Timer Entry Point
The system SHALL provide a timer entry point in the Timeline list view when the selected date is today.
The timer entry point SHALL be a button suitable for primary action (e.g., an Extended FAB) and SHALL be placed in the center, below the newest timeline item.
The timer entry point SHALL have a minimum touch target of 48dp and SHALL provide clear accessibility semantics.
The timer entry point label SHALL be "开始" when no timer is running, and SHALL be "完成" when a timer is running.
The timer entry point SHALL NOT be visible when the selected Timeline date is not today.
When the selected date is today, the Timeline quick create FAB (plus "+" FAB) SHALL NOT be visible.

#### Scenario: Show timer entry point only on today
- **GIVEN** the user is viewing the Timeline list view
- **WHEN** the selected date is today
- **THEN** the timer entry point is visible
- **AND** the label is "开始" when no timer is running

#### Scenario: Hide timer entry point when not today
- **GIVEN** the user is viewing the Timeline list view
- **WHEN** the selected date is not today
- **THEN** the timer entry point is not visible

#### Scenario: Hide quick create FAB on today
- **GIVEN** the user is viewing the Timeline list view
- **WHEN** the selected date is today
- **THEN** the Timeline quick create FAB is not visible

#### Scenario: Accessibility and touch target
- **GIVEN** the timer entry point is visible
- **WHEN** the user navigates via accessibility services
- **THEN** the entry point exposes a content description (e.g., "开始计时" or "完成计时")
- **AND** the touch target is at least 48dp

### Requirement: Start Timer Requires Activity Type Selection (Timeline)
When the user taps the timer entry point in the "开始" state, the system SHALL present an activity type selection UI.
If the user selects an activity type, the system SHALL start a timer with the selected activity type.
If the user cancels or dismisses the selection UI, the system SHALL NOT start a timer and SHALL NOT create any time entry.

#### Scenario: Cancel selection does not start timer
- **GIVEN** the timer entry point is visible and shows "开始"
- **WHEN** the user taps the entry point
- **THEN** an activity type selection UI is shown
- **WHEN** the user cancels or dismisses the selection UI
- **THEN** the timer remains not running
- **AND** no time entry is created

#### Scenario: Selecting activity type starts timer
- **GIVEN** the timer entry point is visible and shows "开始"
- **WHEN** the user taps the entry point
- **THEN** an activity type selection UI is shown
- **WHEN** the user selects an activity type
- **THEN** the timer starts with that activity type

### Requirement: Single Running Timer Constraint (Timeline)
The system SHALL allow at most one running timer at a time.
While a timer is running, the Timeline timer entry point SHALL NOT allow entering the "开始" flow again.

#### Scenario: Running timer blocks starting another
- **GIVEN** a timer is running
- **WHEN** the Timeline screen is rendered
- **THEN** the timer entry point label is "完成"
- **AND** tapping it triggers the stop flow rather than the start flow
