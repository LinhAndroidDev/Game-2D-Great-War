package com.example.game2dgreatwar.gameobject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameClock;
import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;
import com.example.game2dgreatwar.gamepanel.EntityHealthBar;
import com.example.game2dgreatwar.graphics.BitmapCache;
import com.example.game2dgreatwar.map.MapLayout;
import com.example.game2dgreatwar.map.TemporaryHazard;
import com.example.game2dgreatwar.map.Tilemap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Enemy character with typed abilities, HP and optional boss scaling.
 */
public class Enemy extends Circle {

    private static final double BASE_SPEED_PIXELS_PER_SECOND = Player.SPEED_PIXELS_PER_SECOND * 0.6;
    private static final double SPAWNS_PER_MINUTE = 20;
    private static final double SPAWNS_PER_SECOND = SPAWNS_PER_MINUTE / 60.0;
    private static final double UPDATES_PER_SPAWN = GameLoop.MAX_UPS / SPAWNS_PER_SECOND;
    private static double updatesUntilNextSpawn = UPDATES_PER_SPAWN;

    private static final double NORMAL_RADIUS = 40;
    private static final int NORMAL_BITMAP_SIZE = 80;
    private static final long ABILITY_INTERVAL_MS = 3000L;
    private static final long BLINK_INTERVAL_MS = 1000L;

    private final Player player;
    private final EnemyType type;
    private final EnemyRole role;
    private final Bitmap bitmap;
    private final EntityHealthBar healthBar;

    private int healthPoints;
    private final int maxHealthPoints;
    private final double maxSpeed;

    private boolean visible = true;
    private long lastBlinkToggleMs;
    private long lastAbilityMs;
    private boolean reflectReady = true;
    private long lastReflectWindowMs;

    private Enemy(
            Context context,
            Player player,
            EnemyType type,
            EnemyRole role,
            double positionX,
            double positionY,
            int maxHealthPoints,
            double speedMultiplier
    ) {
        super(
                context,
                ContextCompat.getColor(context, R.color.enemy),
                positionX,
                positionY,
                role == EnemyRole.BOSS ? NORMAL_RADIUS * 5 : NORMAL_RADIUS
        );
        this.player = player;
        this.type = type;
        this.role = role;
        this.maxHealthPoints = maxHealthPoints;
        this.healthPoints = maxHealthPoints;
        this.maxSpeed = (BASE_SPEED_PIXELS_PER_SECOND * speedMultiplier) / GameLoop.MAX_UPS;
        this.healthBar = new EntityHealthBar(
                context,
                role == EnemyRole.BOSS ? 180 : 90,
                role == EnemyRole.BOSS ? 24 : 16,
                role == EnemyRole.BOSS ? 90 : 40
        );

        this.bitmap = BitmapCache.get(context, drawableForType(type), bitmapSizeFor(role));

        long now = GameClock.nowMs();
        this.lastBlinkToggleMs = now;
        this.lastAbilityMs = now;
        this.lastReflectWindowMs = now;
    }

    private static int bitmapSizeFor(EnemyRole role) {
        return role == EnemyRole.BOSS ? NORMAL_BITMAP_SIZE * 5 : NORMAL_BITMAP_SIZE;
    }

    /** Decodes every enemy/boss image up front so spawning never decodes during gameplay. */
    public static void preloadBitmaps(Context context) {
        for (EnemyType type : EnemyType.values()) {
            for (EnemyRole role : EnemyRole.values()) {
                BitmapCache.get(context, drawableForType(type), bitmapSizeFor(role));
            }
        }
    }

    public static Enemy createNormal(Context context, Player player, EnemyType type) {
        double speedMultiplier = speedMultiplierFor(type, EnemyRole.NORMAL);
        return new Enemy(
                context,
                player,
                type,
                EnemyRole.NORMAL,
                Math.random() * MapLayout.MAP_WIDTH_PIXELS,
                Math.random() * MapLayout.MAP_HEIGHT_PIXELS,
                normalMaxHpFor(type),
                speedMultiplier
        );
    }

    public static Enemy createBoss(
            Context context,
            Player player,
            EnemyType type,
            double positionX,
            double positionY,
            int maxHealth
    ) {
        double speedMultiplier = speedMultiplierFor(type, EnemyRole.BOSS);
        return new Enemy(
                context,
                player,
                type,
                EnemyRole.BOSS,
                positionX,
                positionY,
                maxHealth,
                speedMultiplier
        );
    }

    private static double speedMultiplierFor(EnemyType type, EnemyRole role) {
        if (type == EnemyType.WHITE) {
            return 0.75;
        }
        if (type == EnemyType.BAT) {
            return 1.1;
        }
        return 1.0;
    }

    private static int normalMaxHpFor(EnemyType type) {
        switch (type) {
            case WHITE:
                return 1;
            case SCARY:
                return 2;
            case BLUE:
            case BAT:
            default:
                return 3;
        }
    }

    private static int drawableForType(EnemyType type) {
        switch (type) {
            case WHITE:
                return R.drawable.ic_ghost_white;
            case SCARY:
                return R.drawable.ic_ghost_scary;
            case BLUE:
                return R.drawable.ic_ghost_blue;
            case BAT:
            default:
                return R.drawable.ic_bat;
        }
    }

    public static boolean readyToSpawn() {
        if (updatesUntilNextSpawn <= 0) {
            updatesUntilNextSpawn += UPDATES_PER_SPAWN;
            return true;
        }
        updatesUntilNextSpawn--;
        return false;
    }

    public static void resetSpawnTimer() {
        updatesUntilNextSpawn = UPDATES_PER_SPAWN;
    }

    public static double getSpawnTimer() {
        return updatesUntilNextSpawn;
    }

    public static void restoreSpawnTimer(double updates) {
        updatesUntilNextSpawn = Math.max(0, Math.min(UPDATES_PER_SPAWN, updates));
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type.name());
        json.put("role", role.name());
        json.put("x", positionX);
        json.put("y", positionY);
        json.put("hp", healthPoints);
        json.put("maxHp", maxHealthPoints);
        json.put("visible", visible);
        json.put("reflectReady", reflectReady);
        // Timers are stored relative to "now" so they keep their remaining time after loading
        json.put("lastBlinkOffsetMs", GameClock.toOffset(lastBlinkToggleMs));
        json.put("lastAbilityOffsetMs", GameClock.toOffset(lastAbilityMs));
        json.put("lastReflectOffsetMs", GameClock.toOffset(lastReflectWindowMs));
        return json;
    }

    public static Enemy fromJson(Context context, Player player, JSONObject json) throws JSONException {
        EnemyType type = EnemyType.valueOf(json.getString("type"));
        EnemyRole role = EnemyRole.valueOf(json.getString("role"));
        int maxHealth = Math.max(1, json.getInt("maxHp"));
        Enemy enemy = new Enemy(
                context,
                player,
                type,
                role,
                json.getDouble("x"),
                json.getDouble("y"),
                maxHealth,
                speedMultiplierFor(type, role)
        );
        enemy.healthPoints = Math.max(1, Math.min(maxHealth, json.getInt("hp")));
        enemy.visible = json.getBoolean("visible");
        enemy.reflectReady = json.getBoolean("reflectReady");
        enemy.lastBlinkToggleMs = GameClock.fromOffset(json.getLong("lastBlinkOffsetMs"));
        enemy.lastAbilityMs = GameClock.fromOffset(json.getLong("lastAbilityOffsetMs"));
        enemy.lastReflectWindowMs = GameClock.fromOffset(json.getLong("lastReflectOffsetMs"));
        return enemy;
    }

    public EnemyType getType() {
        return type;
    }

    public EnemyRole getRole() {
        return role;
    }

    public boolean isBoss() {
        return role == EnemyRole.BOSS;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean canTakeDamage() {
        return visible;
    }

    public boolean canDealContactDamage() {
        return visible;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    public int getMaxHealthPoints() {
        return maxHealthPoints;
    }

    public void takeDamage(int amount) {
        if (!canTakeDamage()) {
            return;
        }
        healthPoints = Math.max(0, healthPoints - amount);
    }

    public boolean isDead() {
        return healthPoints <= 0;
    }

    /**
     * Bat boss reflect: first player spell in each 3s window is reflected.
     */
    public boolean tryConsumeReflect() {
        if (!isBoss() || type != EnemyType.BAT) {
            return false;
        }
        long now = GameClock.nowMs();
        if (now - lastReflectWindowMs >= ABILITY_INTERVAL_MS) {
            reflectReady = true;
            lastReflectWindowMs = now;
        }
        if (!reflectReady) {
            return false;
        }
        reflectReady = false;
        return true;
    }

    public int getShotDirectionCount() {
        if (isBoss()) {
            switch (type) {
                case WHITE:
                    return 10;
                case SCARY:
                    return 15;
                case BLUE:
                    return 6;
                case BAT:
                default:
                    return 10;
            }
        }
        if (type == EnemyType.WHITE) {
            return 4;
        }
        return 0;
    }

    public boolean shotsBounce() {
        return isBoss() && type == EnemyType.BLUE;
    }

    @Override
    public void update() {
        updateBlinkState();
        chasePlayer();
    }

    private void updateBlinkState() {
        if (type != EnemyType.SCARY || isBoss()) {
            visible = true;
            return;
        }
        long now = GameClock.nowMs();
        if (now - lastBlinkToggleMs >= BLINK_INTERVAL_MS) {
            visible = !visible;
            lastBlinkToggleMs = now;
        }
    }

    private void chasePlayer() {
        double distanceToPlayerX = player.getPositionX() - positionX;
        double distanceToPlayerY = player.getPositionY() - positionY;
        double distanceToPlayer = GameObject.getDistanceBetweenObjects(this, player);

        if (distanceToPlayer > 0) {
            velocityX = (distanceToPlayerX / distanceToPlayer) * maxSpeed;
            velocityY = (distanceToPlayerY / distanceToPlayer) * maxSpeed;
        } else {
            velocityX = 0;
            velocityY = 0;
        }

        positionX += velocityX;
        positionY += velocityY;
    }

    public List<EnemyProjectile> tryShoot(Context context) {
        List<EnemyProjectile> shots = new ArrayList<>();
        int directions = getShotDirectionCount();
        if (directions <= 0) {
            return shots;
        }
        long now = GameClock.nowMs();
        if (now - lastAbilityMs < ABILITY_INTERVAL_MS) {
            return shots;
        }
        // For types that both shoot and place hazards, share the same timer cadence.
        if (type == EnemyType.BLUE && !isBoss()) {
            return shots;
        }
        if (type == EnemyType.BAT && !isBoss()) {
            return shots;
        }

        lastAbilityMs = now;
        boolean bounce = shotsBounce();
        for (int i = 0; i < directions; i++) {
            double angle = (Math.PI * 2 * i) / directions;
            shots.add(new EnemyProjectile(
                    context,
                    positionX,
                    positionY,
                    Math.cos(angle),
                    Math.sin(angle),
                    bounce
            ));
        }
        return shots;
    }

    public TemporaryHazard tryPlaceHazard(Tilemap tilemap, Player player) {
        boolean placesWater = type == EnemyType.BLUE && !isBoss();
        boolean placesLava = type == EnemyType.BAT && !isBoss();
        if (!placesWater && !placesLava) {
            return null;
        }

        long now = GameClock.nowMs();
        if (now - lastAbilityMs < ABILITY_INTERVAL_MS) {
            return null;
        }

        TemporaryHazard.Type hazardType = placesLava
                ? TemporaryHazard.Type.LAVA
                : TemporaryHazard.Type.WATER;

        int tileSize = Math.random() < 0.5 ? 1 : 2;
        int col = (int) (positionX / MapLayout.TILE_WIDTH_PIXELS);
        int row = (int) (positionY / MapLayout.TILE_HEIGHT_PIXELS);
        col += (int) (Math.random() * 3) - 1;
        row += (int) (Math.random() * 3) - 1;
        col = Math.max(0, Math.min(MapLayout.NUMBER_OF_COLUMN_TILES - tileSize, col));
        row = Math.max(0, Math.min(MapLayout.NUMBER_OF_ROW_TILES - tileSize, row));

        TemporaryHazard hazard = new TemporaryHazard(
                hazardType,
                col,
                row,
                tileSize,
                tileSize,
                now + TemporaryHazard.TTL_MS
        );

        if (hazard.circleIntersects(player.getPositionX(), player.getPositionY(), player.getRadius())) {
            return null;
        }
        lastAbilityMs = now;
        return hazard;
    }

    public void drawEnemy(Canvas canvas, GameDisplay gameDisplay) {
        if (!visible) {
            return;
        }
        float left = (float) gameDisplay.gameToDisplayCoordinatesX(positionX) - bitmap.getWidth() / 2f;
        float top = (float) gameDisplay.gameToDisplayCoordinatesY(positionY) - bitmap.getHeight() / 2f;
        canvas.drawBitmap(bitmap, left, top, null);
        healthBar.draw(canvas, gameDisplay, positionX, positionY, healthPoints, maxHealthPoints);
    }

    @Override
    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        drawEnemy(canvas, gameDisplay);
    }
}
