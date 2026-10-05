package com.example.game2dgreatwar.gameobject;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameLoop;
import com.example.game2dgreatwar.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Spell extends Circle {
    public static final double SPEED_PIXELS_PER_SECOND = 1200.0;
    private static final double MAX_SPEED = SPEED_PIXELS_PER_SECOND / GameLoop.MAX_UPS;

    public Spell(Context context, Player spellcaster) {
        super(
            context,
            ContextCompat.getColor(context, R.color.spell),
            spellcaster.getPositionX(),
            spellcaster.getPositionY(),
      25
        );
        velocityX = spellcaster.getDirectionX()*MAX_SPEED;
        velocityY = spellcaster.getDirectionY()*MAX_SPEED;
    }

    private Spell(Context context, double positionX, double positionY, double velocityX, double velocityY) {
        super(context, ContextCompat.getColor(context, R.color.spell), positionX, positionY, 25);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", positionX);
        json.put("y", positionY);
        json.put("vx", velocityX);
        json.put("vy", velocityY);
        return json;
    }

    public static Spell fromJson(Context context, JSONObject json) throws JSONException {
        return new Spell(
                context,
                json.getDouble("x"),
                json.getDouble("y"),
                json.getDouble("vx"),
                json.getDouble("vy")
        );
    }

    @Override
    public void update() {
        positionX = positionX + velocityX;
        positionY = positionY + velocityY;
    }
}
