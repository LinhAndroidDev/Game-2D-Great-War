package com.example.game2dgreatwar.map;

import static com.example.game2dgreatwar.map.MapLayout.MAP_HEIGHT_PIXELS;
import static com.example.game2dgreatwar.map.MapLayout.MAP_WIDTH_PIXELS;
import static com.example.game2dgreatwar.map.MapLayout.NUMBER_OF_COLUMN_TILES;
import static com.example.game2dgreatwar.map.MapLayout.NUMBER_OF_ROW_TILES;
import static com.example.game2dgreatwar.map.MapLayout.TILE_HEIGHT_PIXELS;
import static com.example.game2dgreatwar.map.MapLayout.TILE_WIDTH_PIXELS;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.example.game2dgreatwar.GameClock;
import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.graphics.SpriteSheet;
import com.example.game2dgreatwar.map.Tile.TileType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tilemap {

    private final MapLayout mapLayout;
    private final SpriteSheet spriteSheet;
    private final int[][] layout;
    private Bitmap mapBitmap;
    private final List<TemporaryHazard> temporaryHazards = new ArrayList<>();

    public Tilemap(SpriteSheet spriteSheet) {
        mapLayout = new MapLayout();
        this.spriteSheet = spriteSheet;
        this.layout = mapLayout.getLayout();
        initializeTilemap();
    }

    private void initializeTilemap() {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        mapBitmap = Bitmap.createBitmap(
                NUMBER_OF_COLUMN_TILES*TILE_WIDTH_PIXELS,
                NUMBER_OF_ROW_TILES*TILE_HEIGHT_PIXELS,
                config
        );
        renderMapBitmap();
    }

    /** Pre-draws every tile of the current layout into the map bitmap. */
    private void renderMapBitmap() {
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

        Canvas mapCanvas = new Canvas(mapBitmap);

        for (int iRow = 0; iRow < NUMBER_OF_ROW_TILES; iRow++) {
            for (int iCol = 0; iCol < NUMBER_OF_COLUMN_TILES; iCol++) {
                tilemap[iRow][iCol].draw(mapCanvas);
            }
        }

    }

    public JSONObject toJson() throws JSONException {
        // One digit per tile, row by row: compact and easy to validate
        StringBuilder tiles = new StringBuilder(NUMBER_OF_ROW_TILES * NUMBER_OF_COLUMN_TILES);
        for (int[] row : layout) {
            for (int tile : row) {
                tiles.append((char) ('0' + tile));
            }
        }

        JSONArray hazards = new JSONArray();
        for (TemporaryHazard hazard : temporaryHazards) {
            hazards.put(hazard.toJson());
        }

        JSONObject json = new JSONObject();
        json.put("rows", NUMBER_OF_ROW_TILES);
        json.put("cols", NUMBER_OF_COLUMN_TILES);
        json.put("tiles", tiles.toString());
        json.put("hazards", hazards);
        return json;
    }

    public void restoreFromJson(JSONObject json) throws JSONException {
        String tiles = json.getString("tiles");
        if (json.getInt("rows") != NUMBER_OF_ROW_TILES
                || json.getInt("cols") != NUMBER_OF_COLUMN_TILES
                || tiles.length() != NUMBER_OF_ROW_TILES * NUMBER_OF_COLUMN_TILES) {
            throw new JSONException("Saved map size does not match");
        }

        int tileTypeCount = TileType.values().length;
        int[][] restored = new int[NUMBER_OF_ROW_TILES][NUMBER_OF_COLUMN_TILES];
        for (int i = 0; i < tiles.length(); i++) {
            int tile = tiles.charAt(i) - '0';
            if (tile < 0 || tile >= tileTypeCount) {
                throw new JSONException("Invalid tile in saved map: " + tiles.charAt(i));
            }
            restored[i / NUMBER_OF_COLUMN_TILES][i % NUMBER_OF_COLUMN_TILES] = tile;
        }

        List<TemporaryHazard> restoredHazards = new ArrayList<>();
        JSONArray hazards = json.getJSONArray("hazards");
        for (int i = 0; i < hazards.length(); i++) {
            restoredHazards.add(TemporaryHazard.fromJson(hazards.getJSONObject(i)));
        }

        // Everything parsed fine: apply it
        for (int row = 0; row < NUMBER_OF_ROW_TILES; row++) {
            System.arraycopy(restored[row], 0, layout[row], 0, NUMBER_OF_COLUMN_TILES);
        }
        renderMapBitmap();
        temporaryHazards.clear();
        temporaryHazards.addAll(restoredHazards);
    }

    private Rect getRectByIndex(int idxRow, int idxCol) {
        return new Rect(
                idxCol*TILE_WIDTH_PIXELS,
                idxRow*TILE_HEIGHT_PIXELS,
                (idxCol + 1)*TILE_WIDTH_PIXELS,
                (idxRow + 1)*TILE_HEIGHT_PIXELS
        );
    }

    public int getMapWidthPixels() {
        return MAP_WIDTH_PIXELS;
    }

    public int getMapHeightPixels() {
        return MAP_HEIGHT_PIXELS;
    }

    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    public TileType getTileTypeAt(double worldX, double worldY) {
        int col = (int) (worldX / TILE_WIDTH_PIXELS);
        int row = (int) (worldY / TILE_HEIGHT_PIXELS);
        if (row < 0 || row >= NUMBER_OF_ROW_TILES || col < 0 || col >= NUMBER_OF_COLUMN_TILES) {
            return TileType.TREE_TILE;
        }
        return TileType.values()[layout[row][col]];
    }

    public boolean isSolidAt(double worldX, double worldY) {
        return getTileTypeAt(worldX, worldY).isSolid();
    }

    public boolean isLavaAt(double worldX, double worldY) {
        return getTileTypeAt(worldX, worldY).isLava();
    }

    public boolean isWalkableAt(double worldX, double worldY) {
        if (!getTileTypeAt(worldX, worldY).isWalkable()) {
            return false;
        }
        for (TemporaryHazard hazard : temporaryHazards) {
            if (hazard.circleIntersects(worldX, worldY, 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean circleIntersectsSolid(double centerX, double centerY, double radius) {
        if (circleIntersectsTileMatching(centerX, centerY, radius, true, false)) {
            return true;
        }
        for (TemporaryHazard hazard : temporaryHazards) {
            if (hazard.isSolid() && hazard.circleIntersects(centerX, centerY, radius)) {
                return true;
            }
        }
        return false;
    }

    public boolean circleIntersectsLava(double centerX, double centerY, double radius) {
        if (circleIntersectsTileMatching(centerX, centerY, radius, false, true)) {
            return true;
        }
        for (TemporaryHazard hazard : temporaryHazards) {
            if (hazard.isLava() && hazard.circleIntersects(centerX, centerY, radius)) {
                return true;
            }
        }
        return false;
    }

    public void addTemporaryHazard(TemporaryHazard hazard) {
        if (hazard == null) {
            return;
        }
        while (temporaryHazards.size() >= TemporaryHazard.MAX_HAZARDS) {
            temporaryHazards.remove(0);
        }
        temporaryHazards.add(hazard);
    }

    public void clearTemporaryHazards() {
        temporaryHazards.clear();
    }

    public void updateTemporaryHazards() {
        long now = GameClock.nowMs();
        Iterator<TemporaryHazard> iterator = temporaryHazards.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired(now)) {
                iterator.remove();
            }
        }
    }

    public double[] findNearestWalkablePosition(double worldX, double worldY) {
        if (isWalkableAt(worldX, worldY)
                && worldX >= 0 && worldX < MAP_WIDTH_PIXELS
                && worldY >= 0 && worldY < MAP_HEIGHT_PIXELS) {
            return new double[]{worldX, worldY};
        }

        int originCol = clamp((int) (worldX / TILE_WIDTH_PIXELS), 0, NUMBER_OF_COLUMN_TILES - 1);
        int originRow = clamp((int) (worldY / TILE_HEIGHT_PIXELS), 0, NUMBER_OF_ROW_TILES - 1);

        for (int radius = 1; radius <= 8; radius++) {
            for (int dRow = -radius; dRow <= radius; dRow++) {
                for (int dCol = -radius; dCol <= radius; dCol++) {
                    if (Math.abs(dRow) != radius && Math.abs(dCol) != radius) {
                        continue;
                    }
                    int row = originRow + dRow;
                    int col = originCol + dCol;
                    if (row < 0 || row >= NUMBER_OF_ROW_TILES || col < 0 || col >= NUMBER_OF_COLUMN_TILES) {
                        continue;
                    }
                    double candidateX = col * TILE_WIDTH_PIXELS + TILE_WIDTH_PIXELS / 2.0;
                    double candidateY = row * TILE_HEIGHT_PIXELS + TILE_HEIGHT_PIXELS / 2.0;
                    if (isWalkableAt(candidateX, candidateY)) {
                        return new double[]{candidateX, candidateY};
                    }
                }
            }
        }
        return null;
    }

    private boolean circleIntersectsTileMatching(
            double centerX,
            double centerY,
            double radius,
            boolean matchSolid,
            boolean matchLava
    ) {
        int minCol = (int) Math.floor((centerX - radius) / TILE_WIDTH_PIXELS);
        int maxCol = (int) Math.floor((centerX + radius) / TILE_WIDTH_PIXELS);
        int minRow = (int) Math.floor((centerY - radius) / TILE_HEIGHT_PIXELS);
        int maxRow = (int) Math.floor((centerY + radius) / TILE_HEIGHT_PIXELS);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                if (row < 0 || row >= NUMBER_OF_ROW_TILES || col < 0 || col >= NUMBER_OF_COLUMN_TILES) {
                    if (matchSolid) {
                        return true;
                    }
                    continue;
                }

                TileType type = TileType.values()[layout[row][col]];
                boolean matches = (matchSolid && type.isSolid()) || (matchLava && type.isLava());
                if (!matches) {
                    continue;
                }

                double tileLeft = col * TILE_WIDTH_PIXELS;
                double tileTop = row * TILE_HEIGHT_PIXELS;
                double tileRight = tileLeft + TILE_WIDTH_PIXELS;
                double tileBottom = tileTop + TILE_HEIGHT_PIXELS;

                if (circleIntersectsRect(centerX, centerY, radius, tileLeft, tileTop, tileRight, tileBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean circleIntersectsRect(
            double cx, double cy, double radius,
            double left, double top, double right, double bottom
    ) {
        double closestX = clamp(cx, left, right);
        double closestY = clamp(cy, top, bottom);
        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy < radius * radius;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        canvas.drawBitmap(
            mapBitmap,
                gameDisplay.getGameRect(),
                gameDisplay.DISPLAY_RECT,
                null
        );
        for (TemporaryHazard hazard : temporaryHazards) {
            hazard.draw(canvas, gameDisplay, spriteSheet);
        }
    }
}
