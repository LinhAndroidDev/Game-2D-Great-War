package com.example.game2dgreatwar;

/**
 * GameClock is the in-game time source. It only advances when the game updates, so every
 * timer (cooldowns, hazard lifetime, blinking...) freezes while the game is paused.
 */
public final class GameClock {
    private static final long MS_PER_UPDATE = Math.round(1E+3 / GameLoop.MAX_UPS);

    // Monotonic: never reset, so timestamps stored by game objects never end up in the future
    private static volatile long nowMs = 0L;

    private GameClock() {
    }

    public static long nowMs() {
        return nowMs;
    }

    public static void tick() {
        nowMs += MS_PER_UPDATE;
    }
}
