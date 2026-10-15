package com.tictactoe.machine.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static boolean soundOn = true;
    private static final ExecutorService AUDIO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "TicTacToe-AudioWorker");
        t.setDaemon(true);
        return t;
    });

    public static synchronized void setSoundOn(boolean on) {
        soundOn = on;
    }

    public static synchronized boolean isSoundOn() {
        return soundOn;
    }

    public static void beep(int freq) {
        if (!isSoundOn()) return;

        AUDIO_EXECUTOR.submit(() -> {
            try {
                int sampleRate = 8000;
                int durationMs = 120;
                int numSamples = (durationMs * sampleRate) / 1000;
                byte[] buf = new byte[numSamples];

                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                    sdl.open(af);
                    sdl.start();

                    for (int i = 0; i < numSamples; i++) {
                        double angle = i / (sampleRate / (double) freq) * 2.0 * Math.PI;
                        buf[i] = (byte) (Math.sin(angle) * 70.0);
                    }
                    sdl.write(buf, 0, numSamples);
                    sdl.drain();
                }
            } catch (Exception e) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });
    }
}
