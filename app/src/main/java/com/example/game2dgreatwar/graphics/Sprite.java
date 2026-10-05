package com.example.game2dgreatwar.graphics;

import android.graphics.Canvas;
import android.graphics.Rect;

public class Sprite {

    private final SpriteSheet spriteSheet;
    private final Rect rect;
    // Reused for every draw call to avoid allocating a Rect per sprite per frame
    private final Rect destinationRect = new Rect();

    public Sprite(SpriteSheet spriteSheet, Rect rect) {
        this.spriteSheet = spriteSheet;
        this.rect = rect;
    }

    public void draw(Canvas canvas, int x, int y) {
        destinationRect.set(x, y, x + getWidth(), y + getHeight());
        canvas.drawBitmap(
            spriteSheet.getBitmap(),
                rect,
                destinationRect,
                null
        );
    }

    public int getWidth() {
        return rect.width();
    }

    public int getHeight() {
        return rect.height();
    }


}
