package org.codejive.twinkle.screen.util;

public class FrameCounter {
    private int frameCount;
    private long lastFrameTime;
    private double fps;

    public FrameCounter() {
        reset();
    }

    public void reset() {
        frameCount = 0;
        lastFrameTime = System.nanoTime();
        fps = 0.0;
    }

    public void update() {
        frameCount++;
        long currentTime = System.nanoTime();
        long elapsedNanos = currentTime - lastFrameTime;
        if (elapsedNanos > 0) {
            double currentFps = 1_000_000_000.0 / elapsedNanos;
            // Smooth FPS using exponential moving average
            fps = (fps == 0.0) ? currentFps : (fps * 0.9 + currentFps * 0.1);
        }
        lastFrameTime = currentTime;
    }

    public int frames() {
        return frameCount;
    }

    public double average() {
        return fps;
    }
}
