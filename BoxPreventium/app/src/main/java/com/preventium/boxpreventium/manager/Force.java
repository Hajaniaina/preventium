package com.preventium.boxpreventium.manager;

import com.google.android.gms.maps.model.LatLng;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

public class Force {
    private String force;
    private LEVEL_t level_X;
    private LatLng loc;
    private String speed_c;
    private String speed_l;
    private FORCE_t type_X;

    public Force(FORCE_t t_x, LEVEL_t l_x, LatLng lc, String force, String speed_l, String speed_c) {
        this.type_X = t_x;
        this.level_X = l_x;
        this.loc = lc;
        this.force = force;
        this.speed_l = speed_l;
        this.speed_c = speed_c;
    }

    public FORCE_t getType_X() {
        return this.type_X;
    }

    public LEVEL_t getLevel_X() {
        return this.level_X;
    }

    public LatLng getLoc() {
        return this.loc;
    }

    public String getForce() {
        return this.force;
    }

    public String getSpeed_l() {
        return this.speed_l;
    }

    public String getSpeed_c() {
        return this.speed_c;
    }
}
