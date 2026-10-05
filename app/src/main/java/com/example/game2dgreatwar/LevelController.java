package com.example.game2dgreatwar;

import com.example.game2dgreatwar.gameobject.EnemyType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tracks level progression, kill thresholds and boss-fight state.
 */
public class LevelController {
    public static final int MAX_LEVEL = 4;

    private int level = 1;
    private int kills = 0;
    private boolean bossFight = false;
    private boolean victory = false;

    public int getLevel() {
        return level;
    }

    public int getKills() {
        return kills;
    }

    public boolean isBossFight() {
        return bossFight;
    }

    public boolean isVictory() {
        return victory;
    }

    public EnemyType getCurrentEnemyType() {
        return EnemyType.forLevel(level);
    }

    public int getKillThreshold() {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 20;
            case 3:
                return 10;
            case 4:
            default:
                return 20;
        }
    }

    public int getBossMaxHealth() {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 20;
            case 3:
                return 30;
            case 4:
            default:
                return 40;
        }
    }

    public int getBossShotDirections() {
        switch (level) {
            case 1:
                return 10;
            case 2:
                return 15;
            case 3:
                return 6;
            case 4:
            default:
                return 10;
        }
    }

    public boolean bossShotsBounce() {
        return level == 3;
    }

    public boolean bossCanReflect() {
        return level == 4;
    }

    public float getProgressRatio() {
        int threshold = getKillThreshold();
        if (threshold <= 0) {
            return 0f;
        }
        return Math.min(1f, (float) kills / threshold);
    }

    public boolean shouldSpawnMinions() {
        return !bossFight && !victory;
    }

    /**
     * @return true if boss fight should start
     */
    public boolean onNormalEnemyKilled() {
        if (bossFight || victory) {
            return false;
        }
        kills++;
        return kills >= getKillThreshold();
    }

    public void enterBossFight() {
        bossFight = true;
    }

    /**
     * @return true if game is won, false if advanced to next level
     */
    public boolean onBossKilled() {
        bossFight = false;
        if (level >= MAX_LEVEL) {
            victory = true;
            return true;
        }
        level++;
        kills = 0;
        return false;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("level", level);
        json.put("kills", kills);
        json.put("bossFight", bossFight);
        json.put("victory", victory);
        return json;
    }

    public void restoreFromJson(JSONObject json) throws JSONException {
        level = Math.max(1, Math.min(MAX_LEVEL, json.getInt("level")));
        kills = Math.max(0, json.getInt("kills"));
        bossFight = json.getBoolean("bossFight");
        victory = json.getBoolean("victory");
    }

    public void reset() {
        startAtLevel(1);
    }

    public void startAtLevel(int targetLevel) {
        level = Math.max(1, Math.min(MAX_LEVEL, targetLevel));
        kills = 0;
        bossFight = false;
        victory = false;
    }
}
