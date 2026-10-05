package com.example.game2dgreatwar.gamepanel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.game2dgreatwar.LevelController;

public class LevelProgressPanel {
    private final Paint textPaint = new Paint();
    private final Paint barBgPaint = new Paint();
    private final Paint barFillPaint = new Paint();
    private final Paint bossPaint = new Paint();
    private final RectF barRect = new RectF();
    private final RectF fillRect = new RectF();

    public LevelProgressPanel() {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(42f);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);

        barBgPaint.setColor(Color.argb(160, 40, 40, 40));
        barFillPaint.setColor(Color.argb(220, 80, 200, 80));
        bossPaint.setColor(Color.argb(220, 220, 80, 80));
    }

    public void draw(Canvas canvas, LevelController levelController, float screenWidth) {
        float left = screenWidth - 320f;
        float top = 40f;
        float barWidth = 260f;
        float barHeight = 28f;

        String title = "Level " + levelController.getLevel();
        canvas.drawText(title, left, top, textPaint);

        barRect.set(left, top + 16f, left + barWidth, top + 16f + barHeight);
        canvas.drawRoundRect(barRect, 8f, 8f, barBgPaint);

        if (levelController.isBossFight()) {
            canvas.drawRoundRect(barRect, 8f, 8f, bossPaint);
            canvas.drawText("BOSS", left + 80f, top + 38f, textPaint);
        } else {
            float progress = levelController.getProgressRatio();
            fillRect.set(
                    barRect.left,
                    barRect.top,
                    barRect.left + barWidth * progress,
                    barRect.bottom
            );
            canvas.drawRoundRect(fillRect, 8f, 8f, barFillPaint);
            String label = levelController.getKills() + "/" + levelController.getKillThreshold();
            canvas.drawText(label, left + 70f, top + 38f, textPaint);
        }
    }
}
