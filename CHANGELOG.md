# Lessgo muss! 2.0.0

## Study with a plan

- A dedicated Plan tab with subject-linked tasks, due dates, priorities, estimated minutes, editing, deletion, and completion.
- Today, All open, and Completed views; overdue work and planned workload are visible immediately.
- Start a focus session directly from a task; view total study time invested in that task.
- Today's plan on the home screen, with quick completion checkboxes.

## More useful focus sessions

- Open-ended timers or 15, 25, 50, and 90 minute focus blocks.
- Set a default focus duration for sessions launched from the plan.
- Remaining-time display and an optional local block-completion alert. The timer continues until you pause or finish, so you retain control of recorded study time.
- Optional learning notes and a 1–5 focus rating when saving a session. The timer pauses while you reflect.
- Log study time manually for today or a past date, with a subject, duration, and notes. Manual entries end at the current time of day on the selected date.
- A compact active-timer bar across other screens.
- Timer targets and task links survive activity recreation and process restarts.

## Insights and history

- Custom daily and weekly study goals.
- Weekly progress, comparisons with matching days last week, days reaching the current daily goal, and the longest historical streak.
- Average self-reported focus this week, with the number of rated sessions.
- The start hour accounting for the most total study time across saved history. This describes study volume, not a claim about cognitive performance.
- Search all history by subject or session note from Calendar; up to 100 matching sessions are displayed at a time.
- Deleted subjects remain represented in subject breakdown totals as archived subjects.
- Notes and ratings appear alongside saved sessions.

## Flexible notifications

- Daily summary at any hour and minute.
- Up to 20 additional recurring reminders, each with its own time, selected weekdays, and enable/disable switch.
- Start-studying reminders, goal check-ins that skip completed goals or active timers, and task reminders that only alert when unfinished tasks are due.
- Optional quiet hours, including overnight windows. Quiet-hour boundaries use whole hours. Silenced alerts are skipped rather than deferred; identical boundaries disable the quiet period.
- Separate Android notification channels for focus blocks and routine reminders, plus a shortcut to Android's notification settings.
- Rescheduling after reboot, clock/timezone changes, app updates, and changes to timer or reminder settings.
- Alerts use Android's battery-friendly inexact alarms and may be delayed. They require notification permission and the app's Reminders switch. Android force-stop restrictions still apply.

## Design and compatibility

- Calmer teal accents, neutral surfaces, lighter typography, simplified cards, and standard Material navigation: Today, Plan, Insights, Calendar, Settings.
- Subject management is available from Today and Plan.
- Same app ID and release signing configuration as v1.
- Reads existing on-device v1 data and imports v1 JSON backups. New exports use backup format v2 and include tasks, notes, ratings, and new preferences. Version 2 backups cannot be imported into v1.
- All study data remains on-device. No account, analytics service, internet permission, or cloud backend was added.

## Validation

Run `scripts/build-local.sh testDebugUnitTest assembleDebug assembleRelease lintDebug`.
Automated coverage includes v1 backup migration, v2 backup round trips, invalid input rejection, running and paused timer restoration, weekday/minute scheduling, overnight quiet hours, streaks, and goal-day calculations.
A device or emulator is still needed to check screen rendering, notification delivery under battery restrictions, and install-over-v1 behavior.
