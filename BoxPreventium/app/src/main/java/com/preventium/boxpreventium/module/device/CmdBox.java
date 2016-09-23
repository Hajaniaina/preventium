package com.preventium.boxpreventium.module.device;

import com.preventium.boxpreventium.module.enums.CMD_t;

/**
 * Created by Franck on 13/09/2016.
 */

public class CmdBox {

    private CMD_t cmd_t = CMD_t.UNKNOW;

    public static CmdBox newInstance(CMD_t cmd_t ) {
        CmdBox ret = new CmdBox();
        ret.cmd_t = cmd_t;
        return ret;
    }

    public String toString() { return String.format("CmdBox[ %s ]", this.cmd_t.toString()); }

    public byte[] toData() {
        byte[] data = new byte[1];
        data[0] = (byte)this.cmd_t.getValue();
        return data;
    }
}
