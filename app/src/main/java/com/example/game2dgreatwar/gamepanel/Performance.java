package com.example.game2dgreatwar.gamepanel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;

import java.util.Locale;

public class Performance {
    private final Paint paint;

    public Performance(Context context) {
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.magenta));
        paint.setTextSize(50);
    }

    public void draw(Canvas canvas, GameLoop gameLoop) {
        if (gameLoop == null) {
            return;
        }
        drawUPS(canvas, gameLoop);
        drawFPS(canvas, gameLoop);
    }

    public void drawUPS(Canvas canvas, GameLoop gameLoop) {
        String averageUPS = String.format(Locale.US, "%.0f", gameLoop.getAverageUPS());
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas, GameLoop gameLoop) {
        String averageFPS = String.format(Locale.US, "%.0f", gameLoop.getAverageFPS());
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }
}
