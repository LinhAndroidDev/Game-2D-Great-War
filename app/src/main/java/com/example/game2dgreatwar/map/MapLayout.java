package com.example.game2dgreatwar.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapLayout {
    public static final int TILE_WIDTH_PIXELS = 64;
    public static final int TILE_HEIGHT_PIXELS = 64;
    public static final int NUMBER_OF_ROW_TILES = 60;
    public static final int NUMBER_OF_COLUMN_TILES = 60;

    public static final int MAP_WIDTH_PIXELS = NUMBER_OF_COLUMN_TILES * TILE_WIDTH_PIXELS;
    public static final int MAP_HEIGHT_PIXELS = NUMBER_OF_ROW_TILES * TILE_HEIGHT_PIXELS;

    /** Player spawn in Game.java: (1000, 500) */
    public static final double PLAYER_SPAWN_X = 1000;
    public static final double PLAYER_SPAWN_Y = 500;
    private static final int SAFE_ZONE_TILE_RADIUS = 4;

    // Grass regions: solid filled ellipses, trees only inside grass
    private static final int GRASS_REGION_COUNT_MIN = 5;
    private static final int GRASS_REGION_COUNT_MAX = 8;
    private static final int GRASS_REGION_SIZE_MIN = 18;
    private static final int GRASS_REGION_SIZE_MAX = 45;
    private static final int TREES_PER_GRASS_REGION_MIN = 2;
    private static final int TREES_PER_GRASS_REGION_MAX = 6;

    // Water: solid filled ellipses with size caps
    private static final int WATER_BLOB_COUNT_MIN = 3;
    private static final int WATER_BLOB_COUNT_MAX = 5;
    private static final int WATER_BLOB_SIZE_MIN = 8;
    private static final int WATER_BLOB_SIZE_MAX = 28;

    // Lava can stay slightly organic but still filled ellipses for consistency
    private static final int LAVA_BLOB_COUNT_MIN = 2;
    private static final int LAVA_BLOB_COUNT_MAX = 4;
    private static final int LAVA_BLOB_SIZE_MIN = 6;
    private static final int LAVA_BLOB_SIZE_MAX = 22;

    private int[][] layout;
    private final Random random = new Random();
    private int safeCol;
    private int safeRow;

    public MapLayout() {
        initializeLayout();
    }

    public int[][] getLayout() {
        return layout;
    }

    private void initializeLayout() {
        layout = new int[NUMBER_OF_ROW_TILES][NUMBER_OF_COLUMN_TILES];
        safeCol = (int) (PLAYER_SPAWN_X / TILE_WIDTH_PIXELS);
        safeRow = (int) (PLAYER_SPAWN_Y / TILE_HEIGHT_PIXELS);

        fillWithGround();
        placeGrassRegionsWithTrees();
        placeFilledRegions(
                Tile.TileType.WATER_TILE.ordinal(),
                randomRange(WATER_BLOB_COUNT_MIN, WATER_BLOB_COUNT_MAX),
                WATER_BLOB_SIZE_MIN,
                WATER_BLOB_SIZE_MAX,
                false
        );
        placeFilledRegions(
                Tile.TileType.LAVA_TILE.ordinal(),
                randomRange(LAVA_BLOB_COUNT_MIN, LAVA_BLOB_COUNT_MAX),
                LAVA_BLOB_SIZE_MIN,
                LAVA_BLOB_SIZE_MAX,
                false
        );
        enforceSafeZone();
    }

    private void fillWithGround() {
        int ground = Tile.TileType.GROUND_TILE.ordinal();
        for (int row = 0; row < NUMBER_OF_ROW_TILES; row++) {
            for (int col = 0; col < NUMBER_OF_COLUMN_TILES; col++) {
                layout[row][col] = ground;
            }
        }
    }

    private void placeGrassRegionsWithTrees() {
        int regionCount = randomRange(GRASS_REGION_COUNT_MIN, GRASS_REGION_COUNT_MAX);
        int grass = Tile.TileType.GRASS_TILE.ordinal();
        int tree = Tile.TileType.TREE_TILE.ordinal();

        for (int i = 0; i < regionCount; i++) {
            List<int[]> regionTiles = paintFilledEllipse(
                    grass,
                    randomRange(GRASS_REGION_SIZE_MIN, GRASS_REGION_SIZE_MAX),
                    true
            );
            if (regionTiles.isEmpty()) {
                continue;
            }

            int treeCount = Math.min(
                    randomRange(TREES_PER_GRASS_REGION_MIN, TREES_PER_GRASS_REGION_MAX),
                    regionTiles.size()
            );
            placeRandomTreesInRegion(regionTiles, treeCount, tree);
        }
    }

    private void placeFilledRegions(
            int tileType,
            int regionCount,
            int sizeMin,
            int sizeMax,
            boolean onlyOnGround
    ) {
        for (int i = 0; i < regionCount; i++) {
            paintFilledEllipse(tileType, randomRange(sizeMin, sizeMax), onlyOnGround);
        }
    }

    private void placeRandomTreesInRegion(List<int[]> regionTiles, int treeCount, int treeType) {
        List<int[]> candidates = new ArrayList<>(regionTiles);
        for (int i = 0; i < treeCount && !candidates.isEmpty(); i++) {
            int index = random.nextInt(candidates.size());
            int[] tile = candidates.remove(index);
            int row = tile[0];
            int col = tile[1];
            if (isInSafeZone(row, col)) {
                continue;
            }
            layout[row][col] = treeType;
        }
    }

    /**
     * Paints a solid filled ellipse (no holes in the middle).
     * Target size controls approximate area; aspect ratio varies for natural shapes.
     */
    private List<int[]> paintFilledEllipse(int tileType, int targetSize, boolean onlyOnGround) {
        List<int[]> paintedTiles = new ArrayList<>();
        int[] center = findRegionCenter(onlyOnGround);
        if (center == null) {
            return paintedTiles;
        }

        double aspect = 0.65 + random.nextDouble() * 0.7; // 0.65 .. 1.35
        double radiusX = Math.sqrt(targetSize / (Math.PI * aspect));
        double radiusY = radiusX * aspect;
        // Keep regions from becoming oversized
        radiusX = Math.min(radiusX, 6.5);
        radiusY = Math.min(radiusY, 6.5);

        int centerRow = center[0];
        int centerCol = center[1];
        int rowMin = Math.max(0, (int) Math.floor(centerRow - radiusY));
        int rowMax = Math.min(NUMBER_OF_ROW_TILES - 1, (int) Math.ceil(centerRow + radiusY));
        int colMin = Math.max(0, (int) Math.floor(centerCol - radiusX));
        int colMax = Math.min(NUMBER_OF_COLUMN_TILES - 1, (int) Math.ceil(centerCol + radiusX));

        int ground = Tile.TileType.GROUND_TILE.ordinal();
        int grass = Tile.TileType.GRASS_TILE.ordinal();

        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                if (isInSafeZone(row, col)) {
                    continue;
                }

                double dx = (col - centerCol) / radiusX;
                double dy = (row - centerRow) / radiusY;
                if (dx * dx + dy * dy > 1.0) {
                    continue;
                }

                int existing = layout[row][col];
                if (onlyOnGround && existing != ground) {
                    continue;
                }
                if (!onlyOnGround && existing != ground && existing != grass) {
                    continue;
                }

                layout[row][col] = tileType;
                paintedTiles.add(new int[]{row, col});
            }
        }

        return paintedTiles;
    }

    private int[] findRegionCenter(boolean onlyOnGround) {
        int ground = Tile.TileType.GROUND_TILE.ordinal();
        for (int attempt = 0; attempt < 80; attempt++) {
            int row = random.nextInt(NUMBER_OF_ROW_TILES);
            int col = random.nextInt(NUMBER_OF_COLUMN_TILES);
            if (isInSafeZone(row, col)) {
                continue;
            }
            if (onlyOnGround && layout[row][col] != ground) {
                continue;
            }
            if (!onlyOnGround
                    && layout[row][col] != ground
                    && layout[row][col] != Tile.TileType.GRASS_TILE.ordinal()) {
                continue;
            }
            return new int[]{row, col};
        }
        return null;
    }

    private void enforceSafeZone() {
        int ground = Tile.TileType.GROUND_TILE.ordinal();
        for (int row = 0; row < NUMBER_OF_ROW_TILES; row++) {
            for (int col = 0; col < NUMBER_OF_COLUMN_TILES; col++) {
                if (isInSafeZone(row, col)) {
                    layout[row][col] = ground;
                }
            }
        }
    }

    private boolean isInSafeZone(int row, int col) {
        return Math.abs(row - safeRow) <= SAFE_ZONE_TILE_RADIUS
                && Math.abs(col - safeCol) <= SAFE_ZONE_TILE_RADIUS;
    }

    private int randomRange(int minInclusive, int maxInclusive) {
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }
}
