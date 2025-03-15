# Interruption-aware media player

Track user playback intent alongside Android audio focus events so navigation prompts and calls interrupt playback predictably.

## First acceptance test

Start playback, simulate transient focus loss, then press pause while the interruption is active. On focus gain, playback must remain paused. Repeat without the manual pause and verify playback resumes.