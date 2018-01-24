package com.preventium.boxpreventium.manager;

import com.preventium.boxpreventium.enums.LEVEL_t;

public class SpeedLine {
    private LEVEL_t level = LEVEL_t.LEVEL_UNKNOW;
    private int speed_line = 0;

    public SpeedLine(int speed_line, LEVEL_t level) {
        this.speed_line = speed_line;
        this.level = level;
    }

    public int getSpeed_line() {
        return this.speed_line;
    }

    public LEVEL_t getLevel() {
        return this.level;
    }
}
