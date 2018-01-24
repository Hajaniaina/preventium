package com.preventium.boxpreventium.manager;

import com.preventium.boxpreventium.location.CustomMarker;

public class DataMarker {
    private String data;
    private CustomMarker marker;
    private int type;

    public CustomMarker getMarker() {
        return this.marker;
    }

    public String getData() {
        return this.data;
    }

    public int getType() {
        return this.type;
    }

    public DataMarker(CustomMarker marker, String data, int type) {
        this.marker = marker;
        this.data = data;
        this.type = type;
    }
}
