package com.example.game2dgreatwar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.game2dgreatwar.gameobject.Circle;
import com.example.game2dgreatwar.gameobject.Coin;
import com.example.game2dgreatwar.gameobject.Enemy;
import com.example.game2dgreatwar.gameobject.Health;
import com.example.game2dgreatwar.gameobject.Player;
import com.example.game2dgreatwar.gameobject.Spell;
import com.example.game2dgreatwar.gamepanel.Joystick;
import com.example.game2dgreatwar.gamepanel.Performance;
import com.example.game2dgreatwar.graphics.Animator;
import com.example.game2dgreatwar.graphics.SpriteSheet;
import com.example.game2dgreatwar.map.Tilemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Game manages all objects in the game and is responsible for updating all states and render all
 * objects to the screen
 */
public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private final Tilemap tilemap;
    private int joystickPointerId = 0;
    private final Joystick joystick;
    private final Player player;
    private GameLoop gameLoop;
    private final List<Enemy> enemyList = new ArrayList<>();
    private final List<Spell> spellList = new ArrayList<>();
    private final List<Health> healthList = new ArrayList<>();
    private final List<Coin> coinList = new ArrayList<>();
    private int numberOfSpellsToCast = 0;
    private final Performance performance;
    private final GameDisplay gameDisplay;
    GameOverListener gameOverListener;
    private boolean isGameOver = false;
    private int numberOfEnemiesKilled = 0;

    public enum Award {
        COIN(0),
        HEALTH(1);

        private final int value;

        Award(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Award of(int value) {
            for (Award award : Award.values()) {
                if (award.getValue() == value) {
                    return award;
                }
            }
            throw new IllegalArgumentException("Invalid Award value: " + value);
        }
    }

    interface GameOverListener {
        void onGameOver();
    }

    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get surface holder and add callback
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        // Initialize game panels
        performance = new Performance(context, gameLoop);
        MyApplication application = (MyApplication) context.getApplicationContext();
        joystick = new Joystick(275, application.getScreenHeight() * 3 / 4, 120, 90);

        // Initialize game objects
        SpriteSheet spriteSheet = new SpriteSheet(context);
        Animator animator = new Animator(spriteSheet.getPlayerSpriteArray());
        player = new Player(context, joystick, 2*500, 500, 32, animator);

        // Initialize display and center it around the player
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        gameDisplay = new GameDisplay(displayMetrics.widthPixels, displayMetrics.heightPixels, player);

        // Initialize Tilemap
        tilemap = new Tilemap(spriteSheet);

        setFocusable(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Handle user input touch event actions
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (joystick.getIsPressed()) {
                    // Joystick was pressed before this event -> cast spell
                    numberOfSpellsToCast ++;
                    Utils.addSound(getContext(), R.raw.sound_shoot);
                } else if (joystick.isPressed(event.getX(), event.getY())) {
                    // Joystick is pressed in this event -> setIsPressed(true) and store pointer id
                    joystickPointerId = event.getPointerId(event.getActionIndex());
                    joystick.setIsPressed(true);
                } else {
                    // Joystick was not previously, and is not pressed in this event -> cast spell
                    numberOfSpellsToCast ++;
                    Utils.addSound(getContext(), R.raw.sound_shoot);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.getIsPressed()) {
                    // Joystick was pressed previously and is now moved
                    joystick.setActuator(event.getX(), event.getY());
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (joystickPointerId == event.getPointerId(event.getActionIndex())) {
                    // joystick pointer was let go off -> setIsPressed(false) and resetActuator()
                    joystick.setIsPressed(false);
                    joystick.resetActuator();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d("Game.java", "surfaceCreated()");
        if (gameLoop.getState().equals(Thread.State.TERMINATED)) {
            SurfaceHolder surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);
            gameLoop = new GameLoop(this, surfaceHolder);
        }
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("Game.java", "surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d("Game.java", "surfaceDestroyed()");
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Draw Tilemap
        tilemap.draw(canvas, gameDisplay);

        // Draw game objects
        player.draw(canvas, gameDisplay);

        for (Enemy enemy : enemyList) {
            enemy.drawEnemy(canvas, gameDisplay);
        }

        for (Spell spell : spellList) {
            spell.draw(canvas, gameDisplay);
        }

        for (Health health : healthList) {
            health.draw(canvas, gameDisplay);
        }

        for (Coin coin : coinList) {
            coin.draw(canvas, gameDisplay);
        }

        // Draw game panels
        joystick.draw(canvas);
        performance.draw(canvas);

    }

    public void update() {
        // Stop updating the game if the player is dead
        if (player.getHealthPoint() <= 0) {
            if (!isGameOver) {
                isGameOver = true;
                gameOverListener.onGameOver();
            }
            return;
        }

        // Update game state
        joystick.update();
        player.update();

        // Spawn enemy
        if(Enemy.readyToSpawn()) {
            enemyList.add(new Enemy(getContext(), player));
        }

        // Update states of all enemies
        for (Enemy enemy : enemyList) {
            enemy.update();
        }

        // Update states of all spells
        while (numberOfSpellsToCast > 0) {
            spellList.add(new Spell(getContext(), player));
            numberOfSpellsToCast --;
        }
        for (Spell spell : spellList) {
            spell.update();
        }

        // Iterate through enemyList and Check for collision between each enemy and the player and
        // spells in spellList.
        Iterator<Enemy> iteratorEnemy = enemyList.iterator();
        while (iteratorEnemy.hasNext()) {
            Circle enemy = iteratorEnemy.next();
            if (Circle.isColliding(enemy, player)) {
                // Remove enemy if it collides with the player
                Utils.addSound(getContext(), R.raw.sound_enemy_attack);
                iteratorEnemy.remove();
                player.setHealthPoint(player.getHealthPoint() - 1);
                continue;
            }

            Iterator<Spell> iteratorSpell = spellList.iterator();
            while (iteratorSpell.hasNext()) {
                Circle spell = iteratorSpell.next();

                // Remove enemy if it collides with a spell
                if (Circle.isColliding(spell, enemy)) {
                    // Play sound when hitting enemy
                    Utils.addSound(getContext(), R.raw.sound_hit_enemy);

                    // Increase the number of enemies killed
                    if (numberOfEnemiesKilled < 5) {
                        numberOfEnemiesKilled++;

                        // If 5 enemies are destroyed, generate random reward
                        if (numberOfEnemiesKilled == 5) {
                            int randomValue = (int) (Math.random() * 2);
                            switch (Award.of(randomValue)) {
                                case COIN:
                                    // Add coin to the game
                                    coinList.add(new Coin(getContext(), enemy.getPositionX(), enemy.getPositionY()));
                                    Utils.addSound(getContext(), R.raw.sound_coin_appear);
                                    break;
                                case HEALTH:
                                    // Add health to the game
                                    healthList.add(new Health(getContext(), enemy.getPositionX(), enemy.getPositionY()));
                                    break;
                            }
                            numberOfEnemiesKilled = 0;
                        }
                    }

                    // Remove spell and enemy from list
                    iteratorSpell.remove();
                    iteratorEnemy.remove();
                    break;
                }
            }
        }

        // Iterate through healthList and Check for collision between each health and the player
        Iterator<Health> iteratorHealth = healthList.iterator();
        while (iteratorHealth.hasNext()) {
            Health health = iteratorHealth.next();
            if (health.isColliding(player)) {
                // Remove health if it collides with the player
                iteratorHealth.remove();
                if (player.getHealthPoint() < 5) {
                    player.setHealthPoint(player.getHealthPoint() + 1);
                    Utils.addSound(getContext(), R.raw.sound_health);
                }
            }
        }

        // Iterate through coinList and Check for collision between each coin and the player
        Iterator<Coin> iteratorCoin = coinList.iterator();
        while (iteratorCoin.hasNext()) {
            Coin coin = iteratorCoin.next();
            if (coin.isColliding(player)) {
                // Remove coin if it collides with the player
                iteratorCoin.remove();
                Utils.addSound(getContext(), R.raw.sound_coin_recieved);
            }
        }
        
        // Update gameDisplay so that it's center is set to the new center of the player's 
        // game coordinates
        gameDisplay.update();
    }

    public void resetGame() {
        // Reset game state
        player.setHealthPoint(5);
        enemyList.clear();
        spellList.clear();
        healthList.clear();
        coinList.clear();
        numberOfEnemiesKilled = 0;
        numberOfSpellsToCast = 0;
        isGameOver = false;
    }
}
