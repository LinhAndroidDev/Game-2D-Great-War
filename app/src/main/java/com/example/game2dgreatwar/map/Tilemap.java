package com.example.game2dgreatwar.map;

import static com.example.game2dgreatwar.map.MapLayout.NUMBER_OF_COLUMN_TILES;
import static com.example.game2dgreatwar.map.MapLayout.NUMBER_OF_ROW_TILES;
import static com.example.game2dgreatwar.map.MapLayout.TILE_HEIGHT_PIXELS;
import static com.example.game2dgreatwar.map.MapLayout.TILE_WIDTH_PIXELS;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.graphics.SpriteSheet;

public class Tilemap {

    private final MapLayout mapLayout;
    private final SpriteSheet spriteSheet;
    private Bitmap mapBitmap;

    public Tilemap(SpriteSheet spriteSheet) {
        mapLayout = new MapLayout();
        this.spriteSheet = spriteSheet;
        initializeTilemap();
    }

    private void initializeTilemap() {
        int[][] layout = mapLayout.getLayout();
        Tile[][] tilemap = new Tile[NUMBER_OF_ROW_TILES][NUMBER_OF_COLUMN_TILES];
        for (int iRow = 0; iRow < NUMBER_OF_ROW_TILES; iRow++) {
            for (int iCol = 0; iCol < NUMBER_OF_COLUMN_TILES; iCol++) {
                tilemap[iRow][iCol] = Tile.getTile(
                    layout[iRow][iCol],
                    spriteSheet,
                    getRectByIndex(iRow, iCol)
                );
            }
        }

        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        mapBitmap = Bitmap.createBitmap(
                NUMBER_OF_COLUMN_TILES*TILE_WIDTH_PIXELS,
                NUMBER_OF_ROW_TILES*TILE_HEIGHT_PIXELS,
                config
        );

        Canvas mapCanvas = new Canvas(mapBitmap);

        for (int iRow = 0; iRow < NUMBER_OF_ROW_TILES; iRow++) {
            for (int iCol = 0; iCol < NUMBER_OF_COLUMN_TILES; iCol++) {
                tilemap[iRow][iCol].draw(mapCanvas);
            }
        }

    }

    private Rect getRectByIndex(int idxRow, int idxCol) {
        return new Rect(
                idxCol*TILE_WIDTH_PIXELS,
                idxRow*TILE_HEIGHT_PIXELS,
                (idxCol + 1)*TILE_WIDTH_PIXELS,
                (idxRow + 1)*TILE_HEIGHT_PIXELS
        );
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        canvas.drawBitmap(
            mapBitmap,
                gameDisplay.getGameRect(),
                gameDisplay.DISPLAY_RECT,
                null
        );
    }
}
