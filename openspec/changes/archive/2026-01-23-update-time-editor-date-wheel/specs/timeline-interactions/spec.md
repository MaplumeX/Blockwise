## MODIFIED Requirements

### Requirement: Default Time Prefill Rules (Create Mode)
When the create mode Bottom Sheet opens, the start time and end time SHALL be prefilled as "current time".
The prefilled times SHALL use minute precision (consistent with the v1.2 time wheel step) and SHALL align to a whole minute by flooring seconds (i.e., align to :00 seconds).
The start and end date components SHALL be determined by the selected Timeline date, with a future-date guard:
- If the selected date is today, the system SHALL use the system current date and time.
- If the selected date is before today, the system SHALL use the selected date combined with the system current hour/minute.
- If the selected date is after today, the system SHALL clamp the start date to today.
The end date SHALL default to the start date.

#### Scenario: Selected date is today
- **GIVEN** the selected Timeline date is today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start date and end date are today
- **AND** start time and end time show the system current time aligned to minute precision

#### Scenario: Selected date is before today
- **GIVEN** the selected Timeline date is before today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start date and end date are the selected date
- **AND** start/end hour and minute match the system current hour and minute

#### Scenario: Selected date is after today
- **GIVEN** the selected Timeline date is after today
- **WHEN** the create mode Bottom Sheet opens
- **THEN** start date and end date are clamped to today

## MODIFIED Requirements

### Requirement: Create Time Constraint And Inline Validation
In create mode, the system SHALL require that the end time point is strictly later than the start time point.
If the end time point is less than or equal to the start time point, the primary "创建" action SHALL be disabled.
If the end time point is less than or equal to the start time point, the time area SHALL display a validation message: "结束时间需晚于起始时间".

#### Scenario: Invalid time point disables create and shows message
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time point is less than or equal to the start time point
- **THEN** the "创建" button is disabled
- **AND** the validation message "结束时间需晚于起始时间" is shown in the time area

#### Scenario: Valid time point enables create
- **GIVEN** the create mode Bottom Sheet is visible
- **WHEN** the end time point becomes later than the start time point
- **THEN** the "创建" button becomes enabled

## MODIFIED Requirements

### Requirement: Bottom Sheet Content - Time Range Editing
The Bottom Sheet SHALL provide start and end time controls in a single row.
Each time control (start and end) SHALL use wheel-based pickers composed of three aligned wheel columns:
- Date wheel: one-day step, non-cyclic, and restricted to today and the previous 29 days (inclusive).
- Hour wheel: values 00–23 with a 1-hour step.
- Minute wheel: values 00–59 with a 1-minute step.
The date wheel column SHALL be placed to the left of the hour wheel (column order: date -> hour -> minute).
The selected start and end time points SHALL update immediately as the wheels change.
When the user changes any wheel value, the system SHALL update the displayed duration immediately.

#### Scenario: Start and end controls each have date/hour/minute wheels
- **GIVEN** the Bottom Sheet is visible
- **WHEN** the user views the start time control
- **THEN** the start time control shows three wheel columns: date, hour, and minute
- **AND** the date wheel is non-cyclic and allows only the last 30 days (including today)
- **AND** the hour wheel ranges 00–23 and steps by 1 hour
- **AND** the minute wheel ranges 00–59 and steps by 1 minute
- **WHEN** the user views the end time control
- **THEN** the end time control shows three wheel columns: date, hour, and minute

#### Scenario: Duration updates immediately when any wheel changes
- **GIVEN** the Bottom Sheet is visible for a time entry with a duration
- **WHEN** the user changes the start date or time using the wheel pickers
- **THEN** the displayed duration updates immediately
- **WHEN** the user changes the end date or time using the wheel pickers
- **THEN** the displayed duration updates immediately

## ADDED Requirements

### Requirement: Time Editor Cross-Day Auto-Inference (No Future Dates)
When the user is editing time points using the Timeline Bottom Sheet time wheels, the system SHALL support a cross-day auto-inference rule to reduce friction.
If the end date equals the start date and the user sets the end time-of-day to less than or equal to the start time-of-day, the system SHALL adjust the end date to the next day (start date + 1 day) and SHALL update the UI immediately.
If adjusting the end date to the next day would result in a future date (later than today), the system SHALL NOT auto-adjust the end date and SHALL treat the input as invalid.

#### Scenario: Auto-infer next-day end when start date is before today
- **GIVEN** the start date is before today
- **AND** the end date equals the start date
- **WHEN** the user sets the end time-of-day to less than or equal to the start time-of-day
- **THEN** the end date is automatically adjusted to start date + 1 day
- **AND** the displayed duration updates immediately

#### Scenario: Do not auto-infer into the future
- **GIVEN** the start date is today
- **AND** the end date equals the start date
- **WHEN** the user sets the end time-of-day to less than or equal to the start time-of-day
- **THEN** the end date is not automatically adjusted to tomorrow
- **AND** the primary bottom action remains disabled
- **AND** the validation message "结束时间需晚于起始时间" is shown

### Requirement: Edit Time Constraint And Inline Validation
In edit mode, the system SHALL require that the end time point is strictly later than the start time point.
If the end time point is less than or equal to the start time point, the primary "保存" action SHALL be disabled.
If the end time point is less than or equal to the start time point, the time area SHALL display a validation message: "结束时间需晚于起始时间".

#### Scenario: Invalid time point disables save in edit mode
- **GIVEN** the edit mode Bottom Sheet is visible
- **WHEN** the end time point is less than or equal to the start time point
- **THEN** the "保存" button is disabled
- **AND** the validation message "结束时间需晚于起始时间" is shown in the time area

#### Scenario: Valid time point enables save in edit mode
- **GIVEN** the edit mode Bottom Sheet is visible
- **WHEN** the end time point becomes later than the start time point
- **THEN** the "保存" button becomes enabled