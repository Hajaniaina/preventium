package com.preventium.boxpreventium.module.Load;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.preventium.boxpreventium.R;

import java.lang.ref.WeakReference;

/**
 * Created by tog on 13/12/2018.
 */

public class LoadFormateur {

    private WeakReference<Context> contextWeakReference;
    private boolean is_formateur = false;
    public LoadFormateur(Context context, boolean is_formateur) {
        contextWeakReference = new WeakReference<Context>(context);
        this.is_formateur = is_formateur;
    }

    public void init () {
        Context context = contextWeakReference.get();
        if( context != null ) {

            // right
            final Activity activity = (Activity) context;
            LinearLayout layout_right = activity.findViewById(R.id.layout_right);
            if( !is_formateur )
                layout_right.setVisibility(View.GONE);
            else
                layout_right.setVisibility(View.VISIBLE);

            // bottom
            LinearLayout layout_bottom = activity.findViewById(R.id.layout_F_Bottom);
            if( !is_formateur )
                layout_bottom.setVisibility(View.GONE);
            else
                layout_bottom.setVisibility(View.VISIBLE);

            Button close = activity.findViewById(R.id.button_quit);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.finish();
                    System.exit(-1);
                }
            });
        }
    }
}
