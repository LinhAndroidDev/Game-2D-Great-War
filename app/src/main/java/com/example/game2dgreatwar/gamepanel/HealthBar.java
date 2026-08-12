package com.example.game2dgreatwar.gamepanel;

import android.content.Context;
import android.graphics.Canvas;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.gameobject.Player;

/**
 * HealthBar display the players health to the screen
 */
public class HealthBar {
    private final Player player;
    private final EntityHealthBar entityHealthBar;

    public HealthBar(Context context, Player player) {
        this.player = player;
        this.entityHealthBar = new EntityHealthBar(context);
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        entityHealthBar.draw(
                canvas,
                gameDisplay,
                player.getPositionX(),
                player.getPositionY(),
                player.getHealthPoint(),
                Player.MAX_HEALTH_POINTS
        );
    }
}
