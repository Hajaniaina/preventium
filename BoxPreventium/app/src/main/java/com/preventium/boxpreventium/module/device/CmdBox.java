package com.preventium.boxpreventium.module.device;

import com.preventium.boxpreventium.module.enums.CMD_t;

public class CmdBox {
    private CMD_t cmd_t = CMD_t.UNKNOW;

    public static CmdBox newInstance(CMD_t cmd_t) {
        CmdBox ret = new CmdBox();
        ret.cmd_t = cmd_t;
        return ret;
    }

    public String toString() {
        return String.format("CmdBox[ %s ]", new Object[]{this.cmd_t.toString()});
    }

    public byte[] toData() {
        return new byte[]{(byte) this.cmd_t.getValue()};
    }
}
