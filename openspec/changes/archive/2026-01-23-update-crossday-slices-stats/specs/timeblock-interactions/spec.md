## ADDED Requirements

### Requirement: Cross-Day Marker Is Shown At Most Once Per Entry In Time Block
When a cross-day TimeEntry is rendered as multiple hour-row segments in the Time Block day view, the system SHALL show the cross-day marker at most once for that entry within the selected day.

#### Scenario: Marker shown only once across hour segments
- **GIVEN** a cross-day time entry rendered as multiple hour segments within the selected day
- **WHEN** the Time Block day view is rendered
- **THEN** the cross-day marker is shown on at most one segment
