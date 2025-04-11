package com.example.game2dgreatwar.map;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.game2dgreatwar.graphics.Sprite;
import com.example.game2dgreatwar.graphics.SpriteSheet;

class TreeTile extends Tile {
    private final Sprite grassSprite;
    private final Sprite treeSprite;

    public TreeTile(SpriteSheet spriteSheet, Rect mapLocationRect) {
        super(mapLocationRect);
        grassSprite = spriteSheet.getGrassSprite();
        treeSprite = spriteSheet.getTreeSprite();
    }

    @Override
    public void draw(Canvas canvas) {
        grassSprite.draw(canvas, mapLocationRect.left, mapLocationRect.top);
        treeSprite.draw(canvas, mapLocationRect.left, mapLocationRect.top);
    }
}
