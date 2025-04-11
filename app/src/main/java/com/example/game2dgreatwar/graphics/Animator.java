package com.example.game2dgreatwar.graphics;

import android.graphics.Canvas;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.gameobject.Player;

public class Animator {
    private final Sprite[] playerSpriteArray;
    private final int idxNotMovingFrame = 0;
    private int idxMovingFrame = 1;
    private int updatesBeforeNextMoveFrame;
    private static final int MAX_UPDATES_BEFORE_NEXT_MOVE_FRAME = 5;

    public Animator(Sprite[] playerSpriteArray) {
        this.playerSpriteArray = playerSpriteArray;
    }


    public void draw(Canvas canvas, GameDisplay gameDisplay, Player player) {
        switch (player.getPlayerState().getState()) {
            case NOT_MOVING:
                drawFrame(canvas, gameDisplay, player, playerSpriteArray[idxNotMovingFrame]);
                break;
            case STARED_MOVING:
                updatesBeforeNextMoveFrame = MAX_UPDATES_BEFORE_NEXT_MOVE_FRAME;
                drawFrame(canvas, gameDisplay, player, playerSpriteArray[idxMovingFrame]);
                break;
            case IS_MOVING:
                updatesBeforeNextMoveFrame--;
                if(updatesBeforeNextMoveFrame == 0) {
                    updatesBeforeNextMoveFrame = MAX_UPDATES_BEFORE_NEXT_MOVE_FRAME;
                    toggleIdxMovingFrame();
                }
                drawFrame(canvas, gameDisplay, player, playerSpriteArray[idxMovingFrame]);
                break;
            default:
                break;
        }
    }

    private void toggleIdxMovingFrame() {
        if(idxMovingFrame == 1)
            idxMovingFrame = 2;
        else
            idxMovingFrame = 1;
    }

    public void drawFrame(Canvas canvas, GameDisplay gameDisplay, Player player, Sprite sprite) {
        sprite.draw(
                canvas,
                (int) gameDisplay.gameToDisplayCoordinatesX(player.getPositionX()) - sprite.getWidth()/2,
                (int) gameDisplay.gameToDisplayCoordinatesY(player.getPositionY()) - sprite.getHeight()/2
        );
    }
}
