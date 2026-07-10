# Medication

An Android medication tracker for logging doses and getting reminded before the next one is due.

## What it does

Tap the **+** button to record a medication. If it's on a recurring schedule (e.g. every 12 hours), the countdown starts from the moment the dose was taken — not from when you added it. Five minutes before each dose is due, a full-screen notification fires with **Mark taken** and **Snooze 5 min** actions, so refills happen from your lock screen without opening the app.

The home screen is a single chronological list:
- **Upcoming** shows the next scheduled dose per medication, with a live countdown.
- **History** shows every dose that's been logged, newest first.

## Features

- **Add medications** with a name and optional recurring interval (every N hours).
- **Backfill** the taken-at date and time via a date/time picker if you're logging a dose after the fact — the next-dose window recomputes from the actual take time.
- **Live countdown** on each upcoming dose, updated every second.
- **Full-screen reminder** 5 minutes before each dose, shown even on the lock screen, with actions to mark taken (restarts the countdown) or snooze 5 minutes.
- **Edit** any medication's name or interval; the current schedule and alarm recompute automatically.
- **Delete** a medication (removes all its history and cancels its alarm) or delete a single history entry.
- **Boot survival**: pending reminders re-arm themselves after a device reboot.
- **Monochrome UI**: pure black and white with grays, in both light and dark themes.
- **Offline-first**: all data lives locally in a Room database; no accounts, no network calls.

## Tech stack

- Kotlin, Jetpack Compose, Material 3
- Hilt (DI), Room (persistence), Navigation Compose
- `AlarmManager` (exact alarms) + `BroadcastReceiver` + full-screen `Notification`

## Requirements

- Android 7.0 (API 24) or higher
- Notification permission (requested on first launch, API 33+)
- Exact-alarm permission is declared for API 31+

## Build

```
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Version

**1.0** (versionCode 1)
