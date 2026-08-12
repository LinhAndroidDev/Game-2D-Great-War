package com.example.game2dgreatwar.gameobject;

public enum EnemyType {
    WHITE,
    SCARY,
    BLUE,
    BAT;

    public static EnemyType forLevel(int level) {
        switch (level) {
            case 1:
                return WHITE;
            case 2:
                return SCARY;
            case 3:
                return BLUE;
            case 4:
            default:
                return BAT;
        }
    }
}
