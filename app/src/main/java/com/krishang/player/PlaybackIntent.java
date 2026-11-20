package com.krishang.player;

final class PlaybackIntent {
    enum Focus { WAITING, GAINED, TRANSIENT, DUCK, LOST }

    private boolean requested;
    private Focus focus = Focus.LOST;

    void play() {
        requested = true;
        focus = Focus.WAITING;
    }

    void pause() {
        requested = false;
        focus = Focus.LOST;
    }

    void focusChanged(Focus change) {
        if (!requested) return;
        if (change == Focus.LOST) {
            pause();
        } else {
            focus = change;
        }
    }

    boolean requested() { return requested; }
    boolean playing() { return requested && (focus == Focus.GAINED || focus == Focus.DUCK); }
    float volume() { return focus == Focus.DUCK ? 0.2f : 1f; }
}
