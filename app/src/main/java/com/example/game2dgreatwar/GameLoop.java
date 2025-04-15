package com.example.game2dgreatwar;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread{
    public static final double MAX_UPS = 60.0;
    public static final double MAX_FPS = 120.0;

    private static final double UPS_PERIOD = 1E+3/MAX_UPS; // milliseconds per update
    private static final double FPS_PERIOD = 1E+3 / MAX_FPS; // milliseconds per frame

    private final Game game;
    private final SurfaceHolder surfaceHolder;

    private boolean isRunning = false;
    private double averageUPS;
    private double averageFPS;

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
        long updateStartTime = System.currentTimeMillis();
        long frameStartTime = System.currentTimeMillis();

        int updateCount = 0;
        int frameCount = 0;

        while (isRunning) {
            long currentTime = System.currentTimeMillis();

            // Cập nhật logic game nếu đủ thời gian
            if (currentTime - updateStartTime >= UPS_PERIOD) {
                game.update();
                updateStartTime += (long) UPS_PERIOD;
                updateCount++;
            }

            // Vẽ game nếu đủ thời gian
            if (currentTime - frameStartTime >= FPS_PERIOD) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        game.draw(canvas);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    }
                }
                frameStartTime += (long) FPS_PERIOD;
            }

            // Tính toán trung bình FPS & UPS mỗi giây
            if (currentTime - updateStartTime >= 1000) {
                averageUPS = updateCount;
                averageFPS = frameCount;
                updateCount = 0;
                frameCount = 0;
                updateStartTime = currentTime;
                frameStartTime = currentTime;
            }

            // Giảm CPU usage
            try {
                Thread.sleep(1); // ngủ ngắn để nhường CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
