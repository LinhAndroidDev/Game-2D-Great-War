package com.example.game2dgreatwar.gamepanel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.R;

/**
 * Generic health bar drawn above a world position.
 */
public class EntityHealthBar {
    private final Paint borderPaint;
    private final Paint healthPaint;
    private final int width;
    private final int height;
    private final int margin;
    private final float offsetY;

    public EntityHealthBar(Context context) {
        this(context, 100, 20, 30);
    }

    public EntityHealthBar(Context context, int width, int height, float offsetY) {
        this.width = width;
        this.height = height;
        this.margin = 2;
        this.offsetY = offsetY;

        borderPaint = new Paint();
        borderPaint.setColor(ContextCompat.getColor(context, R.color.healthBarBorder));

        healthPaint = new Paint();
        healthPaint.setColor(ContextCompat.getColor(context, R.color.healthBarHealth));
    }

    public void draw(
            Canvas canvas,
            GameDisplay gameDisplay,
            double worldX,
            double worldY,
            int healthPoints,
            int maxHealthPoints
    ) {
        if (maxHealthPoints <= 0) {
            return;
        }
        float healthPointPercentage = Math.max(0f, Math.min(1f, (float) healthPoints / maxHealthPoints));

        float borderLeft = (float) worldX - width / 2f;
        float borderRight = (float) worldX + width / 2f;
        float borderBottom = (float) worldY - offsetY;
        float borderTop = borderBottom - height;

        canvas.drawRect(
                (float) gameDisplay.gameToDisplayCoordinatesX(borderLeft),
                (float) gameDisplay.gameToDisplayCoordinatesY(borderTop),
                (float) gameDisplay.gameToDisplayCoordinatesX(borderRight),
                (float) gameDisplay.gameToDisplayCoordinatesY(borderBottom),
                borderPaint
        );

        float healthWidth = width - 2f * margin;
        float healthHeight = height - 2f * margin;
        float healthLeft = borderLeft + margin;
        float healthRight = healthLeft + healthWidth * healthPointPercentage;
        float healthBottom = borderBottom - margin;
        float healthTop = healthBottom - healthHeight;

        canvas.drawRect(
                (float) gameDisplay.gameToDisplayCoordinatesX(healthLeft),
                (float) gameDisplay.gameToDisplayCoordinatesY(healthTop),
                (float) gameDisplay.gameToDisplayCoordinatesX(healthRight),
                (float) gameDisplay.gameToDisplayCoordinatesY(healthBottom),
                healthPaint
        );
    }
}
