## ADDED Requirements

### Requirement: Timeline Entry Context Menu
The system SHALL display a floating context menu near the user's tap location when the user taps a time entry card in the Timeline list while not in multi-select mode.
The context menu SHALL provide actions: Edit, Delete, Split, and Merge.
Tapping a time entry card while not in multi-select mode SHALL NOT navigate directly to editing; Edit SHALL be available only via the context menu.

#### Scenario: Open context menu near tap location
- **GIVEN** the Timeline list renders a time entry card
- **AND** the system is not in multi-select mode
- **WHEN** the user taps the time entry card
- **THEN** a context menu is shown near the tap location
- **AND** the menu remains fully visible on screen (auto-adjusts to screen bounds)

#### Scenario: Tap does not navigate directly to edit
- **GIVEN** the system is not in multi-select mode
- **WHEN** the user taps a time entry card
- **THEN** the app does not navigate to the edit flow unless the user selects Edit from the context menu

#### Scenario: Dismiss context menu
- **GIVEN** the context menu is visible
- **WHEN** the user dismisses the menu (e.g., taps outside or selects an action)
- **THEN** the context menu is no longer visible

### Requirement: Context Menu Actions - Edit
The system SHALL navigate to the time entry edit flow when the user selects Edit from the context menu.

#### Scenario: Edit from context menu
- **GIVEN** the context menu is visible for a time entry
- **WHEN** the user selects Edit
- **THEN** the app navigates to the edit screen for that time entry

### Requirement: Context Menu Actions - Delete
The system SHALL allow deleting a time entry when the user selects Delete from the context menu, and SHALL confirm the deletion before applying it.

#### Scenario: Delete confirmation
- **GIVEN** the context menu is visible for a time entry
- **WHEN** the user selects Delete
- **THEN** the app shows a deletion confirmation UI

#### Scenario: Delete applies and reflects in timeline
- **GIVEN** the deletion confirmation UI is shown for a time entry
- **WHEN** the user confirms deletion
- **THEN** the time entry is deleted
- **AND** the Timeline list updates to reflect the deletion

### Requirement: Context Menu Actions - Split
The system SHALL allow splitting a time entry when the user selects Split from the context menu, and SHALL confirm the split parameters before applying it.

#### Scenario: Split confirmation
- **GIVEN** the context menu is visible for a time entry
- **WHEN** the user selects Split
- **THEN** the app shows a split confirmation UI

#### Scenario: Split applies and reflects in timeline
- **GIVEN** the split confirmation UI is shown for a time entry
- **WHEN** the user confirms the split
- **THEN** the time entry is split
- **AND** the Timeline list updates to reflect the split

### Requirement: Context Menu Actions - Merge
The system SHALL support merging time entries via the Merge action.
The Merge action SHALL be enabled only when the user has selected at least 2 time entries.

#### Scenario: Merge is disabled under threshold
- **GIVEN** the user has selected fewer than 2 time entries
- **WHEN** the context menu is shown
- **THEN** the Merge action is disabled

#### Scenario: Merge applies and reflects in timeline
- **GIVEN** the user has selected at least 2 time entries
- **AND** the Merge action is enabled
- **WHEN** the user confirms the merge
- **THEN** the selected time entries are merged into a single time entry
- **AND** the Timeline list updates to reflect the merge

### Requirement: Timeline Multi-Select Mode
The system SHALL enter multi-select mode when the user long-presses a time entry card in the Timeline list.
While in multi-select mode, the system SHALL allow the user to tap additional time entry cards to toggle selection.
The system SHALL provide a clear exit path for multi-select mode.

#### Scenario: Enter multi-select mode on long press
- **GIVEN** the Timeline list renders a time entry card
- **WHEN** the user long-presses the time entry card
- **THEN** the system enters multi-select mode
- **AND** the long-pressed entry becomes selected

#### Scenario: Toggle selection in multi-select mode
- **GIVEN** the system is in multi-select mode
- **WHEN** the user taps a time entry card
- **THEN** the tapped entry toggles between selected and unselected
- **AND** no context menu is shown

#### Scenario: Exit multi-select mode
- **GIVEN** the system is in multi-select mode
- **WHEN** the user triggers the exit control
- **THEN** the system exits multi-select mode
- **AND** all selections are cleared

