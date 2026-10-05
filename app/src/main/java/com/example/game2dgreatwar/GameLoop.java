package com.example.game2dgreatwar;

import android.graphics.Canvas;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * GameLoop runs the game on its own thread.
 *
 * Drawing is paced by the display: posting a frame blocks until the screen can take the next one
 * (vsync). Each frame then runs as many fixed-step updates as the elapsed time requires, so game
 * speed stays the same on 60/90/120Hz screens.
 */
public class GameLoop extends Thread {
    public static final double MAX_UPS = 60.0;

    private static final double UPS_PERIOD = 1E+3 / MAX_UPS; // milliseconds per update
    // Frame times within this distance of a whole number of updates are snapped to it. The timer
    // is noisy by a millisecond or two, which otherwise gives frames with 0 or 2 updates (judder).
    private static final double SNAP_TOLERANCE_MS = 2.0;
    // Upper bound on simulated time per frame, so a long hitch doesn't run a burst of updates
    private static final double MAX_FRAME_MS = 250.0;

    private final Game game;
    private final SurfaceHolder surfaceHolder;

    private volatile boolean isRunning = false;
    private volatile double averageUPS;
    private volatile double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startLoop() {
        isRunning = true;
        start();
    }

    @Override
    public void run() {
        long previousFrameNs = System.nanoTime();
        long statsStartNs = previousFrameNs;
        double accumulatorMs = 0;

        int updateCount = 0;
        int frameCount = 0;

        while (isRunning) {
            long nowNs = System.nanoTime();
            double frameMs = Math.min(MAX_FRAME_MS, (nowNs - previousFrameNs) / 1E+6);
            previousFrameNs = nowNs;

            // Cập nhật logic game theo bước cố định, đủ bù cho thời gian frame vừa qua
            accumulatorMs += snapToUpdatePeriod(frameMs);
            while (accumulatorMs >= UPS_PERIOD) {
                game.update();
                accumulatorMs -= UPS_PERIOD;
                updateCount++;
            }

            // Vẽ game; lock/post chặn tới nhịp màn hình kế tiếp nên vòng lặp tự đi theo vsync
            if (drawFrame()) {
                frameCount++;
            } else {
                // Surface chưa sẵn sàng: tránh quay vòng 100% CPU
                sleepQuietly(UPS_PERIOD);
            }

            // Tính toán trung bình FPS & UPS mỗi giây
            long elapsedStatsNs = nowNs - statsStartNs;
            if (elapsedStatsNs >= 1_000_000_000L) {
                averageUPS = updateCount * 1E+9 / elapsedStatsNs;
                averageFPS = frameCount * 1E+9 / elapsedStatsNs;
                updateCount = 0;
                frameCount = 0;
                statsStartNs = nowNs;
            }
        }
    }

    private static double snapToUpdatePeriod(double frameMs) {
        long periods = Math.round(frameMs / UPS_PERIOD);
        if (periods >= 1 && Math.abs(frameMs - periods * UPS_PERIOD) < SNAP_TOLERANCE_MS) {
            return periods * UPS_PERIOD;
        }
        return frameMs;
    }

    private boolean drawFrame() {
        Canvas canvas = null;
        try {
            canvas = lockCanvas();
            if (canvas == null) {
                return false;
            }
            synchronized (surfaceHolder) {
                game.draw(canvas);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private Canvas lockCanvas() {
        // Hardware canvas draws on the GPU; the software canvas took ~15ms per frame on CPU
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return surfaceHolder.lockHardwareCanvas();
        }
        return surfaceHolder.lockCanvas();
    }

    private static void sleepQuietly(double millis) {
        try {
            Thread.sleep((long) millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stopLoop() {
        Log.d("GameLoop.java", "stopLoop()");
        isRunning = false;
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
