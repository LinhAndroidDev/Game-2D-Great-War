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
import com.example.game2dgreatwar.gameobject.EnemyProjectile;
import com.example.game2dgreatwar.gameobject.EnemyType;
import com.example.game2dgreatwar.gameobject.Health;
import com.example.game2dgreatwar.gameobject.Player;
import com.example.game2dgreatwar.gameobject.Spell;
import com.example.game2dgreatwar.gamepanel.Joystick;
import com.example.game2dgreatwar.gamepanel.LevelProgressPanel;
import com.example.game2dgreatwar.gamepanel.Performance;
import com.example.game2dgreatwar.graphics.Animator;
import com.example.game2dgreatwar.graphics.SpriteSheet;
import com.example.game2dgreatwar.map.MapLayout;
import com.example.game2dgreatwar.map.TemporaryHazard;
import com.example.game2dgreatwar.map.Tilemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Game manages all objects in the game and is responsible for updating all states and render all
 * objects to the screen
 */
public class Game extends SurfaceView implements SurfaceHolder.Callback {

    private static final double BOSS_PLAYER_OFFSET = 280;

    private final Tilemap tilemap;
    private int joystickPointerId = 0;
    private final Joystick joystick;
    private final Player player;
    private GameLoop gameLoop;
    private final List<Enemy> enemyList = new ArrayList<>();
    private final List<Spell> spellList = new ArrayList<>();
    private final List<EnemyProjectile> enemyProjectileList = new ArrayList<>();
    private final List<Health> healthList = new ArrayList<>();
    private final List<Coin> coinList = new ArrayList<>();
    private int numberOfSpellsToCast = 0;
    private final Performance performance;
    private final LevelProgressPanel levelProgressPanel = new LevelProgressPanel();
    private final LevelController levelController = new LevelController();
    private final GameDisplay gameDisplay;
    private float screenWidthPixels;

    GameOverListener gameOverListener;
    VictoryListener victoryListener;
    LevelChangedListener levelChangedListener;
    PauseStateListener pauseStateListener;
    private boolean isGameOver = false;
    private volatile boolean isPaused = false;
    private int awardKillCounter = 0;

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

    interface VictoryListener {
        void onVictory();
    }

    interface LevelChangedListener {
        void onLevelChanged(int level);
    }

    interface PauseStateListener {
        void onPauseChanged(boolean paused);
    }

    public Game(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        performance = new Performance(context);

        // Load sounds and images up front so nothing is decoded on the game thread mid-fight
        SoundManager.init(context);
        Enemy.preloadBitmaps(context);
        Coin.preloadBitmaps(context);
        Health.preloadBitmaps(context);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidthPixels = displayMetrics.widthPixels;

        joystick = new Joystick(0, 0, 1, 1);
        joystick.layoutForScreen(displayMetrics.widthPixels, displayMetrics.heightPixels);

        SpriteSheet spriteSheet = new SpriteSheet(context);
        tilemap = new Tilemap(spriteSheet);

        Animator animator = new Animator(spriteSheet.getPlayerSpriteArray());
        player = new Player(
                context,
                joystick,
                MapLayout.PLAYER_SPAWN_X,
                MapLayout.PLAYER_SPAWN_Y,
                32,
                animator,
                tilemap
        );

        gameDisplay = new GameDisplay(displayMetrics.widthPixels, displayMetrics.heightPixels, player);
        setFocusable(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isPaused) {
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (joystick.getIsPressed()) {
                    numberOfSpellsToCast++;
                    Utils.addSound(getContext(), R.raw.sound_shoot);
                } else if (joystick.isPressed(event.getX(), event.getY())) {
                    joystickPointerId = event.getPointerId(event.getActionIndex());
                    joystick.setIsPressed(true);
                } else {
                    numberOfSpellsToCast++;
                    Utils.addSound(getContext(), R.raw.sound_shoot);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (joystick.getIsPressed()) {
                    joystick.setActuator(event.getX(), event.getY());
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (joystickPointerId == event.getPointerId(event.getActionIndex())) {
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
        // A Thread can only be started once, so every new surface gets a fresh loop
        gameLoop = new GameLoop(this, holder);
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("Game.java", "surfaceChanged()");
        screenWidthPixels = width;
        joystick.layoutForScreen(width, height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d("Game.java", "surfaceDestroyed()");
        // Must stop drawing before this callback returns, the surface is gone afterwards
        if (gameLoop != null) {
            gameLoop.stopLoop();
            gameLoop = null;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        tilemap.draw(canvas, gameDisplay);
        player.draw(canvas, gameDisplay);

        for (Enemy enemy : enemyList) {
            enemy.drawEnemy(canvas, gameDisplay);
        }
        for (Spell spell : spellList) {
            spell.draw(canvas, gameDisplay);
        }
        for (EnemyProjectile projectile : enemyProjectileList) {
            projectile.draw(canvas, gameDisplay);
        }
        for (Health health : healthList) {
            health.draw(canvas, gameDisplay);
        }
        for (Coin coin : coinList) {
            coin.draw(canvas, gameDisplay);
        }

        joystick.draw(canvas);
        performance.draw(canvas, gameLoop);
        levelProgressPanel.draw(canvas, levelController, screenWidthPixels);
    }

    public void update() {
        if (isPaused) {
            return;
        }
        GameClock.tick();

        if (levelController.isVictory()) {
            return;
        }

        if (player.getHealthPoint() <= 0) {
            if (!isGameOver) {
                isGameOver = true;
                if (gameOverListener != null) {
                    gameOverListener.onGameOver();
                }
            }
            return;
        }

        joystick.update();
        player.update();
        tilemap.updateTemporaryHazards();

        if (levelController.shouldSpawnMinions() && Enemy.readyToSpawn()) {
            enemyList.add(Enemy.createNormal(
                    getContext(),
                    player,
                    levelController.getCurrentEnemyType()
            ));
        }

        for (Enemy enemy : enemyList) {
            enemy.update();
            handleEnemyAbilities(enemy);
        }

        while (numberOfSpellsToCast > 0) {
            spellList.add(new Spell(getContext(), player));
            numberOfSpellsToCast--;
        }
        for (Spell spell : spellList) {
            spell.update();
        }

        updateEnemyProjectiles();
        removeInvalidPlayerSpells();
        resolveCombat();
        collectPickups();

        gameDisplay.update();
    }

    private void handleEnemyAbilities(Enemy enemy) {
        TemporaryHazard hazard = enemy.tryPlaceHazard(tilemap, player);
        if (hazard != null) {
            tilemap.addTemporaryHazard(hazard);
        }

        List<EnemyProjectile> shots = enemy.tryShoot(getContext());
        for (EnemyProjectile shot : shots) {
            addEnemyProjectile(shot);
        }
    }

    private void addEnemyProjectile(EnemyProjectile projectile) {
        if (projectile.isBouncing()) {
            int bouncingCount = 0;
            for (EnemyProjectile existing : enemyProjectileList) {
                if (existing.isBouncing()) {
                    bouncingCount++;
                }
            }
            if (bouncingCount >= EnemyProjectile.MAX_BOUNCING_PROJECTILES) {
                return;
            }
        }
        enemyProjectileList.add(projectile);
    }

    private void updateEnemyProjectiles() {
        Iterator<EnemyProjectile> iterator = enemyProjectileList.iterator();
        while (iterator.hasNext()) {
            EnemyProjectile projectile = iterator.next();
            if (projectile.isBouncing()) {
                projectile.updateWithCollision(tilemap);
            } else {
                projectile.update();
                if (projectile.shouldBeRemoved(tilemap)) {
                    iterator.remove();
                    continue;
                }
            }

            if (Circle.isColliding(projectile, player)) {
                if (player.takeDamage(1)) {
                    Utils.addSound(getContext(), R.raw.sound_enemy_attack);
                }
                if (!projectile.isBouncing()) {
                    iterator.remove();
                }
            }
        }
    }

    private void removeInvalidPlayerSpells() {
        Iterator<Spell> iteratorSpellTile = spellList.iterator();
        while (iteratorSpellTile.hasNext()) {
            Spell spell = iteratorSpellTile.next();
            if (isSpellOutOfBounds(spell)
                    || tilemap.circleIntersectsSolid(
                    spell.getPositionX(),
                    spell.getPositionY(),
                    spell.getRadius())) {
                iteratorSpellTile.remove();
            }
        }
    }

    private void resolveCombat() {
        boolean shouldStartBossFight = false;
        boolean bossWasDefeated = false;

        Iterator<Enemy> iteratorEnemy = enemyList.iterator();
        while (iteratorEnemy.hasNext()) {
            Enemy enemy = iteratorEnemy.next();

            if (enemy.canDealContactDamage() && Circle.isColliding(enemy, player)) {
                if (player.takeDamage(1)) {
                    Utils.addSound(getContext(), R.raw.sound_enemy_attack);
                }
                // Normal enemies kamikaze on contact; bosses stay and keep chasing
                if (!enemy.isBoss()) {
                    iteratorEnemy.remove();
                    continue;
                }
            }

            Iterator<Spell> iteratorSpell = spellList.iterator();
            while (iteratorSpell.hasNext()) {
                Spell spell = iteratorSpell.next();
                if (!Circle.isColliding(spell, enemy)) {
                    continue;
                }

                iteratorSpell.remove();

                if (enemy.tryConsumeReflect()) {
                    addEnemyProjectile(new EnemyProjectile(
                            getContext(),
                            enemy.getPositionX(),
                            enemy.getPositionY(),
                            -spell.getVelocityX(),
                            -spell.getVelocityY(),
                            false
                    ));
                    Utils.addSound(getContext(), R.raw.sound_hit_enemy);
                    break;
                }

                if (!enemy.canTakeDamage()) {
                    break;
                }

                Utils.addSound(getContext(), R.raw.sound_hit_enemy);
                enemy.takeDamage(1);

                if (!enemy.isDead()) {
                    break;
                }

                boolean wasBoss = enemy.isBoss();
                double deathX = enemy.getPositionX();
                double deathY = enemy.getPositionY();
                iteratorEnemy.remove();

                if (wasBoss) {
                    bossWasDefeated = true;
                } else {
                    registerNormalKillRewards(deathX, deathY);
                    if (levelController.onNormalEnemyKilled()) {
                        shouldStartBossFight = true;
                    }
                }
                break;
            }

            // List may be cleared by boss flow — stop iterating safely
            if (shouldStartBossFight || bossWasDefeated) {
                break;
            }
        }

        if (bossWasDefeated) {
            handleBossDefeated();
        } else if (shouldStartBossFight) {
            startBossFight();
        }
    }

    private void registerNormalKillRewards(double deathX, double deathY) {
        awardKillCounter++;
        if (awardKillCounter >= 5) {
            awardKillCounter = 0;
            int randomValue = (int) (Math.random() * 2);
            spawnAwardAt(Award.of(randomValue), deathX, deathY);
        }
    }

    private void startBossFight() {
        enemyList.clear();
        enemyProjectileList.clear();
        tilemap.clearTemporaryHazards();
        spellList.clear();
        player.healFull();

        gameDisplay.update();
        double bossX = gameDisplay.getGameCenterX();
        double bossY = gameDisplay.getGameCenterY();
        double[] bossPos = tilemap.findNearestWalkablePosition(bossX, bossY);
        if (bossPos != null) {
            bossX = bossPos[0];
            bossY = bossPos[1];
        }

        EnemyType type = levelController.getCurrentEnemyType();
        Enemy boss = Enemy.createBoss(
                getContext(),
                player,
                type,
                bossX,
                bossY,
                levelController.getBossMaxHealth()
        );
        enemyList.add(boss);

        double playerX = bossX;
        double playerY = bossY + BOSS_PLAYER_OFFSET;
        double[] playerPos = tilemap.findNearestWalkablePosition(playerX, playerY);
        if (playerPos != null) {
            player.setPositionSafe(playerPos[0], playerPos[1]);
        } else {
            player.setPositionSafe(playerX, playerY);
        }

        levelController.enterBossFight();
        Enemy.resetSpawnTimer();
    }

    private void handleBossDefeated() {
        enemyProjectileList.clear();
        tilemap.clearTemporaryHazards();
        spellList.clear();
        player.healFull();

        boolean won = levelController.onBossKilled();
        if (won) {
            if (victoryListener != null) {
                victoryListener.onVictory();
            }
            return;
        }

        awardKillCounter = 0;
        Enemy.resetSpawnTimer();
        if (levelChangedListener != null) {
            levelChangedListener.onLevelChanged(levelController.getLevel());
        }
    }

    private void collectPickups() {
        Iterator<Health> iteratorHealth = healthList.iterator();
        while (iteratorHealth.hasNext()) {
            Health health = iteratorHealth.next();
            if (health.isColliding(player)) {
                iteratorHealth.remove();
                if (player.getHealthPoint() < Player.MAX_HEALTH_POINTS) {
                    player.setHealthPoint(player.getHealthPoint() + 1);
                    Utils.addSound(getContext(), R.raw.sound_health);
                }
            }
        }

        Iterator<Coin> iteratorCoin = coinList.iterator();
        while (iteratorCoin.hasNext()) {
            Coin coin = iteratorCoin.next();
            if (coin.isColliding(player)) {
                iteratorCoin.remove();
                Utils.addSound(getContext(), R.raw.sound_coin_recieved);
            }
        }
    }

    public void pause() {
        if (isPaused) {
            return;
        }
        isPaused = true;
        // Drop held input so the player doesn't keep running/shooting after resume
        joystick.setIsPressed(false);
        joystick.resetActuator();
        numberOfSpellsToCast = 0;
        notifyPauseChanged();
    }

    public void resume() {
        if (!isPaused) {
            return;
        }
        isPaused = false;
        notifyPauseChanged();
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void notifyPauseChanged() {
        if (pauseStateListener != null) {
            pauseStateListener.onPauseChanged(isPaused);
        }
    }

    public void resetGame() {
        startAtLevel(1);
    }

    public void startAtLevel(int level) {
        player.healFull();
        player.setPositionSafe(MapLayout.PLAYER_SPAWN_X, MapLayout.PLAYER_SPAWN_Y);
        enemyList.clear();
        spellList.clear();
        enemyProjectileList.clear();
        healthList.clear();
        coinList.clear();
        tilemap.clearTemporaryHazards();
        numberOfSpellsToCast = 0;
        awardKillCounter = 0;
        isGameOver = false;
        levelController.startAtLevel(level);
        Enemy.resetSpawnTimer();
        gameDisplay.update();
        resume();
    }

    public int getCurrentLevel() {
        return levelController.getLevel();
    }

    private boolean isSpellOutOfBounds(Spell spell) {
        double x = spell.getPositionX();
        double y = spell.getPositionY();
        return x < 0
                || y < 0
                || x > tilemap.getMapWidthPixels()
                || y > tilemap.getMapHeightPixels();
    }

    private void spawnAwardAt(Award award, double worldX, double worldY) {
        double[] walkablePosition = tilemap.findNearestWalkablePosition(worldX, worldY);
        if (walkablePosition == null) {
            return;
        }

        switch (award) {
            case COIN:
                coinList.add(new Coin(getContext(), walkablePosition[0], walkablePosition[1]));
                Utils.addSound(getContext(), R.raw.sound_coin_appear);
                break;
            case HEALTH:
                healthList.add(new Health(getContext(), walkablePosition[0], walkablePosition[1]));
                break;
        }
    }
}
