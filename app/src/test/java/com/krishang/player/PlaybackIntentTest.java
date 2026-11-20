package com.krishang.player;

import static org.junit.Assert.*;
import org.junit.Test;
import static com.krishang.player.PlaybackIntent.Focus.*;

public class PlaybackIntentTest {
    @Test public void manualPauseCancelsResume() {
        var state = new PlaybackIntent();
        state.play();
        state.focusChanged(GAINED);
        assertTrue(state.playing());
        state.focusChanged(TRANSIENT);
        assertFalse(state.playing());
        state.pause();
        state.focusChanged(GAINED);
        assertFalse(state.requested());
        assertFalse(state.playing());
    }

    @Test public void transientLossResumesWhenIntentIsStillPlay() {
        var state = new PlaybackIntent();
        state.play();
        assertFalse(state.playing());
        state.focusChanged(GAINED);
        state.focusChanged(TRANSIENT);
        assertTrue(state.requested());
        assertFalse(state.playing());
        state.focusChanged(GAINED);
        assertTrue(state.playing());
    }

    @Test public void duckThenRestoreVolume() {
        var state = new PlaybackIntent();
        state.play();
        state.focusChanged(DUCK);
        assertTrue(state.playing());
        assertEquals(0.2f, state.volume(), 0f);
        state.focusChanged(GAINED);
        assertEquals(1f, state.volume(), 0f);
    }

    @Test public void permanentLossRequiresNewPlay() {
        var state = new PlaybackIntent();
        state.play();
        state.focusChanged(LOST);
        state.focusChanged(GAINED);
        assertFalse(state.playing());
        state.play();
        state.focusChanged(GAINED);
        assertTrue(state.playing());
    }
}
