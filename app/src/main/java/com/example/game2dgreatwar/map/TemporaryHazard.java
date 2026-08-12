package com.example.game2dgreatwar.map;

import static com.example.game2dgreatwar.map.MapLayout.TILE_HEIGHT_PIXELS;
import static com.example.game2dgreatwar.map.MapLayout.TILE_WIDTH_PIXELS;

import android.graphics.Canvas;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.graphics.Sprite;
import com.example.game2dgreatwar.graphics.SpriteSheet;

public class TemporaryHazard {
    public enum Type {
        WATER,
        LAVA
    }

    public static final long TTL_MS = 10_000L;
    public static final int MAX_HAZARDS = 8;

    private final Type type;
    private final int tileCol;
    private final int tileRow;
    private final int tilesWide;
    private final int tilesHigh;
    private final long expiresAtMs;

    public TemporaryHazard(Type type, int tileCol, int tileRow, int tilesWide, int tilesHigh, long expiresAtMs) {
        this.type = type;
        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.tilesWide = Math.max(1, tilesWide);
        this.tilesHigh = Math.max(1, tilesHigh);
        this.expiresAtMs = expiresAtMs;
    }

    public Type getType() {
        return type;
    }

    public boolean isExpired(long nowMs) {
        return nowMs >= expiresAtMs;
    }

    public boolean isLava() {
        return type == Type.LAVA;
    }

    public boolean isSolid() {
        return true;
    }

    public double getLeft() {
        return tileCol * TILE_WIDTH_PIXELS;
    }

    public double getTop() {
        return tileRow * TILE_HEIGHT_PIXELS;
    }

    public double getRight() {
        return (tileCol + tilesWide) * TILE_WIDTH_PIXELS;
    }

    public double getBottom() {
        return (tileRow + tilesHigh) * TILE_HEIGHT_PIXELS;
    }

    public boolean circleIntersects(double cx, double cy, double radius) {
        double closestX = clamp(cx, getLeft(), getRight());
        double closestY = clamp(cy, getTop(), getBottom());
        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay, SpriteSheet spriteSheet) {
        Sprite sprite = type == Type.LAVA ? spriteSheet.getLavaSprite() : spriteSheet.getWaterSprite();
        for (int row = 0; row < tilesHigh; row++) {
            for (int col = 0; col < tilesWide; col++) {
                double worldX = (tileCol + col) * TILE_WIDTH_PIXELS;
                double worldY = (tileRow + row) * TILE_HEIGHT_PIXELS;
                int displayX = (int) gameDisplay.gameToDisplayCoordinatesX(worldX);
                int displayY = (int) gameDisplay.gameToDisplayCoordinatesY(worldY);
                sprite.draw(canvas, displayX, displayY);
            }
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
