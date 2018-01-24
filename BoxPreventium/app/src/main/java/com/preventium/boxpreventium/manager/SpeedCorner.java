package com.preventium.boxpreventium.manager;

import com.preventium.boxpreventium.enums.LEVEL_t;

public class SpeedCorner {
    private LEVEL_t level = LEVEL_t.LEVEL_UNKNOW;
    private int speed_corner = 0;

    public SpeedCorner(int speed_corner, LEVEL_t level) {
        this.speed_corner = speed_corner;
        this.level = level;
    }

    public int getSpeed_corner() {
        return this.speed_corner;
    }

    public LEVEL_t getLevel() {
        return this.level;
    }
}
