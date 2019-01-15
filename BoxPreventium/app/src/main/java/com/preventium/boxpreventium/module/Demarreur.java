package com.preventium.boxpreventium.module;

import android.content.Context;

import com.github.clans.fab.FloatingActionButton;
import com.preventium.boxpreventium.R;

/**
 * Created by tog on 24/12/2018.
 */

public class Demarreur {

    public interface DemarreurListener {
        void onDemarrer();
        void onArreter();
    }

    // var
    private boolean is_demarrer;
    private int drawable;
    private Context context;
    private DemarreurListener app;

    // button
    private final int stop_button = R.drawable.ic_stop_black_24dp;
    private final int play_button = R.drawable.ic_play;

    public Demarreur(Context context, DemarreurListener app) {
        this.context = context;
        this.app = app;
    }

    // button
    public int getButton () {
        return drawable;
    }

    public void setButton (FloatingActionButton stop_parcour) {
        if( stop_parcour != null ) {
            stop_parcour.setImageResource(drawable);
        }
    }

    // pour demarrer
    public Demarreur demarrer() {
        if( is_demarrer ) {
            drawable = stop_button;
            is_demarrer = true;
            return this;
        }

        // demarre
        app.onDemarrer();
        // update
        is_demarrer = true;
        drawable = stop_button;
        return this;
    }
    public boolean is_demarrer() { return is_demarrer; }

    // arrêter
    public Demarreur arreter() {
        if( is_demarrer ) {
            // on arrête
			// app.onArreter();
			// update
            is_demarrer = false;
        }

        drawable = play_button;
        return this;
    }

    // pauser pour bientôt
    void pauser() {}
    boolean is_pause() { return false; }

}
