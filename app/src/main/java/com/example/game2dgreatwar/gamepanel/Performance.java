package com.example.game2dgreatwar.gamepanel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;

public class Performance {
    private final GameLoop gameLoop;
    private final Context context;

    public Performance(Context context, GameLoop gameLoop) {
        this.context = context;
        this.gameLoop = gameLoop;
    }

    public void draw(Canvas canvas) {
        drawUPS(canvas);
        drawFPS(canvas);
    }
    public void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(context, R.color.magenta);
        paint.setColor(color);
        paint.setTextSize(50);
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas) {
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(context, R.color.magenta);
        paint.setColor(color);
        paint.setTextSize(50);
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }
}
