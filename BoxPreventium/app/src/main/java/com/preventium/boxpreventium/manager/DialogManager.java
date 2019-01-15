package com.preventium.boxpreventium.manager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.module.Demarreur;

/**
 * Created by tog on 29/11/2018.
 */

public class DialogManager {
    private Context context;
    private AlertDialog.Builder build;
    private MainActivity main;

    public DialogManager (Context context) {
        this.context = context;
        this.main = (MainActivity) context;
        build = new AlertDialog.Builder(context);
        build.setCancelable(true);
    }

    public interface Callback {
        void onCall(DialogInterface dialogInterface);
        void onCall();
    }

    // avec callback
    public void Dialog (String message, final Callback callback) {
        build.setMessage(message);
        build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.onCall(dialogInterface);
            }
        });
        build.create().show();
    }

    /** Sans callback */
    public void Dialog (String message) {
        build.setMessage(message);
        build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        build.create().show();
    }

    public void Alert(final String msg, int duration) {

        switch(duration) {
            case Toast.LENGTH_LONG:
            case Toast.LENGTH_SHORT:
                break;
            default:
                duration = Toast.LENGTH_LONG;
        }

        final int dure = duration;
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run () {
                Toast.makeText(context, msg, dure).show();
            }
        });
    }

    public void askEndParcoursConfirm(final Demarreur demarreur, final FloatingActionButton stop_parcour) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.end_parcours, null);
        build.setView(view);
        build.setMessage(context.getString(R.string.end_parcours_confirm_string));
        final AlertDialog dialog = build.create();

        ((Button)view.findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ((Button)view.findViewById(R.id.no_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.getAppManager().setPause();
                dialog.dismiss();
            }
        });

        ((Button)view.findViewById(R.id.yes_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.getAppManager().setStopped(true);
                if( demarreur != null && stop_parcour != null  ) demarreur.arreter().setButton(stop_parcour);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
