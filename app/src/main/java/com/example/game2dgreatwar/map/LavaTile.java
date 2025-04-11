package com.example.game2dgreatwar.map;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.game2dgreatwar.graphics.Sprite;
import com.example.game2dgreatwar.graphics.SpriteSheet;

class LavaTile extends Tile {
    private final Sprite sprite;

    public LavaTile(SpriteSheet spriteSheet, Rect mapLocationRect) {
        super(mapLocationRect);
        sprite = spriteSheet.getLavaSprite();
    }

    @Override
    public void draw(Canvas canvas) {
        sprite.draw(canvas, mapLocationRect.left, mapLocationRect.top);
    }
}
