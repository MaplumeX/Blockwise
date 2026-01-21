## ADDED Requirements

### Requirement: Timeline Quick Create FAB
The system SHALL display a Floating Action Button (FAB) in the bottom-right of the Timeline list view to provide a stable, predictable entry point for creating a new time entry.
The FAB SHALL be positioned above the bottom navigation bar (and system gesture area) to avoid occlusion.
The FAB icon SHALL be a plus (+).
The FAB touch target SHALL be at least 48dp.
The FAB SHALL provide clear accessibility semantics (e.g., content description: "添加时间记录").
The FAB SHALL be visible only in the Timeline list view.
The FAB SHALL NOT be visible in the Time Block day view.
The FAB SHALL NOT be visible in other screens/modules.

#### Scenario: FAB is visible in Timeline list view
- **GIVEN** the user is viewing the Timeline list view
- **WHEN** the Timeline list view is rendered
- **THEN** a plus (+) FAB is visible at the bottom-right above the bottom navigation

#### Scenario: FAB is hidden in Time Block day view
- **GIVEN** the user is viewing the Time Block day view
- **WHEN** the Time Block day view is rendered
- **THEN** the quick create FAB is not visible

#### Scenario: FAB is hidden outside Timeline
- **GIVEN** the user navigates to a non-Timeline screen/module
- **WHEN** that screen is rendered
- **THEN** the quick create FAB is not visible

### Requirement: Open Create Mode Bottom Sheet From FAB
When the user taps the Timeline quick create FAB, the system SHALL open a modal Bottom Sheet in create mode.
The Bottom Sheet SHALL appear within 300ms (subjective, no noticeable lag) after the FAB tap.
The Bottom Sheet SHALL be dismissible via a close control and via tapping the scrim area.
Dismissing the Bottom Sheet SHALL NOT create or persist a time entry.

#### Scenario: Tap FAB opens create mode sheet
- **GIVEN** the Timeline quick create FAB is visible
- **WHEN** the user taps the FAB
- **THEN** a create mode Bottom Sheet is shown

#### Scenario: Close sheet without creating
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the user dismisses it via close or scrim tap
- **THEN** the Bottom Sheet is dismissed
- **AND** no time entry is created

### Requirement: Create Mode Bottom Sheet Presentation
The create mode Bottom Sheet SHALL reuse the v1.2 Bottom Sheet interaction container, but SHALL present as "create mode".
The primary bottom action label SHALL be "创建".
In create mode, the Bottom Sheet SHALL NOT display destructive or structural operation entry points (including Delete, Merge, Split).

#### Scenario: Create mode hides dangerous actions
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the user views available actions
- **THEN** Delete/Merge/Split entry points are not shown

### Requirement: Default Time Prefill Rules (Create Mode)
When the create mode Bottom Sheet opens, the start time and end time SHALL be prefilled as "current time".
The prefilled times SHALL use minute precision (consistent with the v1.2 time wheel step) and SHALL align to a whole minute by flooring seconds (i.e., align to :00 seconds).
The date component SHALL be determined by the selected Timeline date:
- If the selected date is today, the system SHALL use the system current date and time.
- If the selected date is not today, the system SHALL use the selected date combined with the system current hour/minute.

#### Scenario: Selected date is today
- **GIVEN** the selected Timeline date is today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start time and end time show the system current time aligned to minute precision

#### Scenario: Selected date is not today
- **GIVEN** the selected Timeline date is not today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start time and end time use the selected date
- **AND** start/end hour and minute match the system current hour and minute

### Requirement: Create Time Constraint And Inline Validation
In create mode, the system SHALL require that end time is strictly later than start time.
If end time is less than or equal to start time, the primary "创建" action SHALL be disabled.
If end time is less than or equal to start time, the time area SHALL display a validation message: "结束时间需晚于起始时间".

#### Scenario: Invalid time disables create and shows message
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time is less than or equal to the start time
- **THEN** the "创建" button is disabled
- **AND** the validation message "结束时间需晚于起始时间" is shown in the time area

#### Scenario: Valid time enables create
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time becomes later than the start time
- **THEN** the "创建" button becomes enabled
