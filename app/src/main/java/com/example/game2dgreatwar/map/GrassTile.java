package com.example.game2dgreatwar.map;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.game2dgreatwar.graphics.Sprite;
import com.example.game2dgreatwar.graphics.SpriteSheet;

class GrassTile extends Tile {
    private final Sprite sprite;

    public GrassTile(SpriteSheet spriteSheet, Rect mapLocationRect) {
        super(mapLocationRect);
        sprite = spriteSheet.getGrassSprite();
    }

    @Override
    public void draw(Canvas canvas) {
        sprite.draw(canvas, mapLocationRect.left, mapLocationRect.top);
    }
}
