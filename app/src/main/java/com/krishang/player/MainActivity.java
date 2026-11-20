package com.krishang.player;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private final PlaybackIntent intent = new PlaybackIntent();
    private AudioManager audio;
    private AudioFocusRequest request;
    private MediaPlayer player;
    private TextView status;
    private Button play;
    private long generation;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        audio = getSystemService(AudioManager.class);
        player = MediaPlayer.create(this, R.raw.demo);
        if (player != null) {
            player.setLooping(true);
            player.setOnErrorListener((source, what, extra) -> {
                player = null;
                source.release();
                stopPlayback();
                return true;
            });
        }
        var layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(24 * getResources().getDisplayMetrics().density);
        layout.setOnApplyWindowInsetsListener((view, insets) -> {
            var bars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
            view.setPadding(padding + bars.left, padding + bars.top,
                    padding + bars.right, padding + bars.bottom);
            return insets;
        });
        setContentView(layout);
        status = new TextView(this);
        status.setTextSize(24);
        layout.addView(status);
        play = addButton(layout, "Play", () -> {
            if (intent.requested()) stopPlayback();
            else startPlayback();
        });
        addButton(layout, "Simulate transient loss", () -> focus(PlaybackIntent.Focus.TRANSIENT));
        addButton(layout, "Simulate focus gain", () -> focus(PlaybackIntent.Focus.GAINED));
        render();
    }

    private Button addButton(LinearLayout layout, String title, Runnable action) {
        var button = new Button(this);
        button.setText(title);
        button.setOnClickListener(view -> action.run());
        layout.addView(button);
        return button;
    }

    private void startPlayback() {
        intent.play();
        long requestGeneration = ++generation;
        request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(change -> {
                    if (requestGeneration != generation) return;
                    switch (change) {
                        case AudioManager.AUDIOFOCUS_GAIN -> focus(PlaybackIntent.Focus.GAINED);
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> focus(PlaybackIntent.Focus.TRANSIENT);
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> focus(PlaybackIntent.Focus.DUCK);
                        case AudioManager.AUDIOFOCUS_LOSS -> stopPlayback();
                        default -> { }
                    }
                }, new Handler(Looper.getMainLooper()))
                .build();
        int result = audio.requestAudioFocus(request);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) focus(PlaybackIntent.Focus.GAINED);
        else if (result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) render();
        else stopPlayback();
    }

    private void focus(PlaybackIntent.Focus focus) {
        intent.focusChanged(focus);
        render();
    }

    private void stopPlayback() {
        generation++;
        intent.pause();
        if (request != null) {
            audio.abandonAudioFocusRequest(request);
            request = null;
        }
        render();
    }

    private void render() {
        play.setText(intent.requested() ? "Pause" : "Play");
        play.setEnabled(player != null);
        if (player == null) {
            status.setText("Demo audio unavailable");
        } else if (intent.playing()) {
            player.setVolume(intent.volume(), intent.volume());
            if (!player.isPlaying()) player.start();
            status.setText(intent.volume() < 1 ? "Playing quietly" : "Playing");
        } else {
            if (player.isPlaying()) player.pause();
            status.setText(intent.requested() ? "Waiting for audio focus" : "Paused");
        }
    }

    @Override protected void onStop() {
        stopPlayback();
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (player != null) player.release();
        super.onDestroy();
    }
}
