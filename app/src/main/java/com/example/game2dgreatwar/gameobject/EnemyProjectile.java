package com.example.game2dgreatwar.gameobject;

import android.content.Context;
import android.graphics.Canvas;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;
import com.example.game2dgreatwar.map.Tilemap;

import org.json.JSONException;
import org.json.JSONObject;

public class EnemyProjectile extends Circle {
    public static final double SPEED_PIXELS_PER_SECOND = 500.0;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;
    public static final int MAX_BOUNCING_PROJECTILES = 24;

    private final boolean bounces;

    public EnemyProjectile(
            Context context,
            double positionX,
            double positionY,
            double directionX,
            double directionY,
            boolean bounces
    ) {
        super(
                context,
                ContextCompat.getColor(context, R.color.enemyProjectile),
                positionX,
                positionY,
                18
        );
        double length = Math.sqrt(directionX * directionX + directionY * directionY);
        if (length == 0) {
            length = 1;
        }
        this.velocityX = (directionX / length) * MAX_SPEED;
        this.velocityY = (directionY / length) * MAX_SPEED;
        this.bounces = bounces;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", positionX);
        json.put("y", positionY);
        json.put("vx", velocityX);
        json.put("vy", velocityY);
        json.put("bounces", bounces);
        return json;
    }

    public static EnemyProjectile fromJson(Context context, JSONObject json) throws JSONException {
        // The constructor normalizes the direction, so the saved velocity works as a direction
        return new EnemyProjectile(
                context,
                json.getDouble("x"),
                json.getDouble("y"),
                json.getDouble("vx"),
                json.getDouble("vy"),
                json.getBoolean("bounces")
        );
    }

    public boolean isBouncing() {
        return bounces;
    }

    @Override
    public void update() {
        positionX += velocityX;
        positionY += velocityY;
    }

    public void updateWithCollision(Tilemap tilemap) {
        positionX += velocityX;
        if (hitsSolidOrBounds(tilemap, positionX, positionY)) {
            if (bounces) {
                velocityX = -velocityX;
                positionX += velocityX;
            }
        }

        positionY += velocityY;
        if (hitsSolidOrBounds(tilemap, positionX, positionY)) {
            if (bounces) {
                velocityY = -velocityY;
                positionY += velocityY;
            }
        }
    }

    public boolean shouldBeRemoved(Tilemap tilemap) {
        if (bounces) {
            return false;
        }
        return hitsSolidOrBounds(tilemap, positionX, positionY);
    }

    private boolean hitsSolidOrBounds(Tilemap tilemap, double x, double y) {
        if (x < radius
                || y < radius
                || x > tilemap.getMapWidthPixels() - radius
                || y > tilemap.getMapHeightPixels() - radius) {
            return true;
        }
        return tilemap.circleIntersectsSolid(x, y, radius);
    }

    @Override
    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        super.draw(canvas, gameDisplay);
    }
}
