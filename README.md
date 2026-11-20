# Interruption-aware media player

Track user playback intent alongside Android audio focus events so navigation prompts and calls interrupt playback predictably.

## First acceptance test

Start playback, simulate transient focus loss, then press pause while the interruption is active. On focus gain, playback must remain paused. Repeat without the manual pause and verify playback resumes.

The foreground demo loops an original synthesized tone. Android focus events and
the on-screen simulation feed the playback state machine. Delayed focus waits,
transient loss pauses, ducking reduces volume, and permanent loss clears play
intent. Leaving the activity releases audio focus.

Use JDK 17 or 21 and Android SDK Platform 35 with Build Tools 34.0.0. Run
`gradlew.bat :app:testDebugUnitTest :app:assembleDebug` (Windows) or
`./gradlew :app:testDebugUnitTest :app:assembleDebug` (macOS/Linux). The debug
APK supports API 31 and newer. Unit tests cover both interruption paths,
delayed start, duck and restore, and permanent loss.
