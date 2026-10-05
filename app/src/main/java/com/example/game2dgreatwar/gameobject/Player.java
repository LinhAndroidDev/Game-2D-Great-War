package com.example.game2dgreatwar.gameobject;

import android.content.Context;
import android.graphics.Canvas;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameClock;
import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;
import com.example.game2dgreatwar.Utils;
import com.example.game2dgreatwar.gamepanel.HealthBar;
import com.example.game2dgreatwar.gamepanel.Joystick;
import com.example.game2dgreatwar.graphics.Animator;
import com.example.game2dgreatwar.map.Tilemap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Player is the main character of the game, which the user can control with a touch joystick.
 */
public class Player extends Circle {
    public static final double SPEED_PIXELS_PER_SECOND = 700.0;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;
    public static final int MAX_HEALTH_POINTS = 5;
    private static final int LAVA_DAMAGE = 2;
    private static final long LAVA_DAMAGE_COOLDOWN_MS = 1000L;
    private static final long I_FRAME_MS = 750L;

    private final Joystick joystick;
    private final HealthBar healthBar;
    private int healthPoints = MAX_HEALTH_POINTS;
    private final Animator animator;
    private final PlayerState playerState;
    private final Tilemap tilemap;
    private long lastLavaDamageTimeMs = -LAVA_DAMAGE_COOLDOWN_MS;
    private long iFrameUntilMs = 0L;

    public Player(
            Context context,
            Joystick joystick,
            double positionX,
            double positionY,
            double radius,
            Animator animator,
            Tilemap tilemap
    ) {
        super(context, ContextCompat.getColor(context, R.color.player), positionX, positionY, radius);
        this.joystick = joystick;
        this.healthBar = new HealthBar(context, this);
        this.animator = animator;
        this.playerState = new PlayerState(this);
        this.tilemap = tilemap;
    }

    public void update() {
        velocityX = joystick.getActuatorX() * MAX_SPEED;
        velocityY = joystick.getActuatorY() * MAX_SPEED;

        boolean touchedLava = false;

        positionX += velocityX;
        if (tilemap.circleIntersectsSolid(positionX, positionY, radius)) {
            if (tilemap.circleIntersectsLava(positionX, positionY, radius)) {
                touchedLava = true;
            }
            positionX -= velocityX;
        }

        positionY += velocityY;
        if (tilemap.circleIntersectsSolid(positionX, positionY, radius)) {
            if (tilemap.circleIntersectsLava(positionX, positionY, radius)) {
                touchedLava = true;
            }
            positionY -= velocityY;
        }

        if (tilemap.circleIntersectsLava(positionX, positionY, radius)) {
            touchedLava = true;
        }

        if (touchedLava) {
            applyLavaDamage();
        }

        clampToMapBounds();

        if (velocityX != 0 || velocityY != 0) {
            double distance = Utils.getDistanceBetweenPoints(0, 0, velocityX, velocityY);
            directionX = velocityX / distance;
            directionY = velocityY / distance;
        }

        playerState.update();
    }

    private void applyLavaDamage() {
        long now = GameClock.nowMs();
        if (now - lastLavaDamageTimeMs < LAVA_DAMAGE_COOLDOWN_MS) {
            return;
        }
        lastLavaDamageTimeMs = now;
        takeDamage(LAVA_DAMAGE);
    }

    public boolean takeDamage(int amount) {
        long now = GameClock.nowMs();
        if (now < iFrameUntilMs) {
            return false;
        }
        healthPoints = Math.max(0, healthPoints - amount);
        iFrameUntilMs = now + I_FRAME_MS;
        return true;
    }

    public void healFull() {
        healthPoints = MAX_HEALTH_POINTS;
        iFrameUntilMs = 0L;
    }

    public void setPositionSafe(double x, double y) {
        setPosition(x, y);
        clampToMapBounds();
    }

    private void clampToMapBounds() {
        double minX = radius;
        double minY = radius;
        double maxX = tilemap.getMapWidthPixels() - radius;
        double maxY = tilemap.getMapHeightPixels() - radius;
        positionX = Math.max(minX, Math.min(maxX, positionX));
        positionY = Math.max(minY, Math.min(maxY, positionY));
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        animator.draw(canvas, gameDisplay, this);
        healthBar.draw(canvas, gameDisplay);
    }

    public int getHealthPoint() {
        return healthPoints;
    }

    public void setHealthPoint(int healthPoints) {
        this.healthPoints = Math.max(0, healthPoints);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", positionX);
        json.put("y", positionY);
        json.put("hp", healthPoints);
        json.put("directionX", directionX);
        json.put("directionY", directionY);
        // Timers are stored relative to "now" so they keep their remaining time after loading
        json.put("iFrameOffsetMs", GameClock.toOffset(iFrameUntilMs));
        json.put("lastLavaOffsetMs", GameClock.toOffset(lastLavaDamageTimeMs));
        return json;
    }

    public void restoreFromJson(JSONObject json) throws JSONException {
        setPositionSafe(json.getDouble("x"), json.getDouble("y"));
        healthPoints = Math.max(1, Math.min(MAX_HEALTH_POINTS, json.getInt("hp")));
        directionX = json.getDouble("directionX");
        directionY = json.getDouble("directionY");
        velocityX = 0;
        velocityY = 0;
        iFrameUntilMs = GameClock.fromOffset(json.getLong("iFrameOffsetMs"));
        lastLavaDamageTimeMs = GameClock.fromOffset(json.getLong("lastLavaOffsetMs"));
    }

    public PlayerState getPlayerState() {
        return playerState;
    }
}
