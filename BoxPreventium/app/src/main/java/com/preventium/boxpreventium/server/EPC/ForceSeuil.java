package com.preventium.boxpreventium.server.EPC;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import java.util.Locale;

public class ForceSeuil {
    public short IDAlert = (short) -1;
    public short TPS = (short) 0;
    public int index = -1;
    public LEVEL_t level = LEVEL_t.LEVEL_UNKNOW;
    public double mG_high = 0.0d;
    public double mG_low = 0.0d;
    public FORCE_t type = FORCE_t.UNKNOW;

    ForceSeuil() {
    }

    public ForceSeuil(int index, short IDAlert, short secs, double mG_min, double mG_max) {
        this.index = index;
        this.IDAlert = IDAlert;
        this.TPS = secs;
        this.mG_low = mG_min;
        this.mG_high = mG_max;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ForceSeuil other = (ForceSeuil) obj;
        if (this.IDAlert == other.IDAlert && this.TPS == other.TPS && this.mG_low == other.mG_low && this.mG_high == other.mG_high && this.level == other.level && this.type == other.type) {
            return true;
        }
        return false;
    }

    public String toString() {
        String name = "";
        if (this.index >= 0 && this.index < 5) {
            name = String.format(Locale.getDefault(), "+X%d", new Object[]{Integer.valueOf(this.index + 1)});
        } else if (this.index >= 5 && this.index < 10) {
            name = String.format(Locale.getDefault(), "-X%d", new Object[]{Integer.valueOf(this.index - 4)});
        } else if (this.index >= 10 && this.index < 15) {
            name = String.format(Locale.getDefault(), "Y%d", new Object[]{Integer.valueOf(this.index - 9)});
        } else if (this.index >= 15 && this.index < 20) {
            name = String.format(Locale.getDefault(), "-Y%d", new Object[]{Integer.valueOf(this.index - 14)});
        }
        return String.format(Locale.getDefault(), "ForceSeuil %s[ IDAlert:%d, secs:%d, mG(%s;%s), type:%s, level:%s ]", new Object[]{name, Short.valueOf(this.IDAlert), Short.valueOf(this.TPS), Double.valueOf(this.mG_low), Double.valueOf(this.mG_high), this.type, this.level});
    }
}
