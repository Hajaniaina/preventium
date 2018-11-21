package com.preventium.boxpreventium.widget;

import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

/**
 * Created by tog on 02/11/2018.
 */

public class Widget {

    public static Widget instance;
    public static Widget get() {
        if( instance == null ) instance = new Widget();
        return instance;
    }

    public Widget () {
        this.note = 20;
        this.note_color = LEVEL_t.LEVEL_1;
        this.color_avg = LEVEL_t.LEVEL_1;
        this.force = FORCE_t.UNKNOW;
        this.force_color = LEVEL_t.LEVEL_UNKNOW;
    }

    protected int note;
    protected LEVEL_t note_color;
    protected LEVEL_t color_avg;
    protected LEVEL_t force_color;
    protected FORCE_t force;

    public void setNote (int note) {
        this.note = note;
    }
    public int getNote () {
        return note;
    }

    public void setNoteColor (LEVEL_t note_color) {
        this.note_color = note_color;
    }
    public LEVEL_t getNoteColor () {
        return note_color;
    }

    public void setNoteColorAVG (LEVEL_t color_avg) {
        this.color_avg = color_avg;
    }
    public LEVEL_t getNoteColorAVG () {
        return color_avg;
    }

    public void setForceColor (LEVEL_t force_color) {
        this.force_color = force_color;
    }
    public LEVEL_t getForceColor () {
        return force_color;
    }

    public void setForce (FORCE_t force) {
        this.force = force;
    }
    public FORCE_t getForce () {
        return force;
    }
}
