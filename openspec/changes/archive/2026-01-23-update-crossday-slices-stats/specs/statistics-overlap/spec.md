## ADDED Requirements

### Requirement: Statistics Attribute Minutes By Overlap
The system SHALL compute all statistics based on overlap minutes between a TimeEntry and a statistics window.
The statistics window SHALL be interpreted in the user's local time zone.
The statistics window SHALL be a half-open time range: `[startTime, endTime)`.
For a given entry and window, the contributed minutes SHALL equal `overlapMinutes(entry, window)`.
The system SHALL compute overlap minutes by truncating to minute precision (no rounding).
The system SHALL NOT attribute minutes solely based on the entry start date.

#### Scenario: Cross-day entry contributes to both days in daily trends
- **GIVEN** a time entry from 23:30 on day D to 00:30 on day D+1
- **WHEN** computing daily trend totals for day D and day D+1
- **THEN** the entry contributes 30 minutes to day D
- **AND** the entry contributes 30 minutes to day D+1

#### Scenario: Hourly distribution splits by hour buckets
- **GIVEN** a time entry from 23:30 on day D to 00:30 on day D+1
- **WHEN** computing hourly distribution buckets for hour 23 on day D and hour 00 on day D+1
- **THEN** the entry contributes 30 minutes to the 23:00 bucket
- **AND** the entry contributes 30 minutes to the 00:00 bucket

#### Scenario: Category totals use overlap minutes within window
- **GIVEN** a statistics window that overlaps with a time entry tagged with an activity type and tags
- **WHEN** computing category totals within the window
- **THEN** the contributed minutes are added based on the overlapped minutes within the window

### Requirement: Daily Trend Totals Equal Sum Of Day Slices
For any given natural date D, the total minutes for that date SHALL equal the sum of overlapped minutes of all entries within that day window.
The day window SHALL be interpreted in the user's local time zone and SHALL be `[D 00:00:00, D+1 00:00:00)`.

#### Scenario: Daily total equals sum of day-window overlaps
- **GIVEN** multiple time entries where some overlap day D and some do not
- **WHEN** computing the total minutes for day D
- **THEN** the total equals the sum of each entry's overlap minutes with `[D 00:00:00, D+1 00:00:00)`

### Requirement: Category And Tag Statistics Use Window Overlap
When computing totals by activity type or by tag, the system SHALL sum overlap minutes within the selected statistics window.

#### Scenario: Cross-day entry is counted partially in the selected window
- **GIVEN** a time entry that overlaps the statistics window only partially
- **WHEN** computing totals by activity type or tag
- **THEN** only the overlapped minutes within the window are counted
