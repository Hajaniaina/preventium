package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;

import java.util.ArrayList;

public class ScoreView extends Object {

    private static final String TAG = "ScoreView";

    public static final int CORNER = 0;
    public static final int BRAKE  = 1;
    public static final int ACC    = 2;
    public static final int AVG    = 3;

    private ArrayList<ImageView> scoreViewList;
    private ArrayList<Drawable> lastBackgroundList;
    private AppColor appColor;

    ScoreView (Activity activity) {

        appColor = new AppColor(activity);
        scoreViewList = new ArrayList<>();
        lastBackgroundList = new ArrayList<>();

        scoreViewList.add(((ImageView) activity.findViewById(R.id.corner_note_view)));
        scoreViewList.add(((ImageView) activity.findViewById(R.id.brake_note_view)));
        scoreViewList.add(((ImageView) activity.findViewById(R.id.acc_note_view)));
        scoreViewList.add(((ImageView) activity.findViewById(R.id.avg_note_view)));

        setScore(ScoreView.CORNER, LEVEL_t.LEVEL_UNKNOW);
        setScore(ScoreView.BRAKE, LEVEL_t.LEVEL_UNKNOW);
        setScore(ScoreView.ACC, LEVEL_t.LEVEL_UNKNOW);
        setScore(ScoreView.AVG, LEVEL_t.LEVEL_UNKNOW);
    }

    public void hide (boolean hide) {

        for (ImageView view : scoreViewList) {

            if (hide) {

                view.setVisibility(View.GONE);
            }
            else {

                view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void disable (boolean disable) {

        for (int i = 0; i < scoreViewList.size(); i++) {

            if (disable) {

                Drawable drawable = scoreViewList.get(i).getBackground();
                lastBackgroundList.set(i, drawable);
                drawable.setColorFilter(appColor.getColor(AppColor.GREY), PorterDuff.Mode.SRC_ATOP);

                scoreViewList.get(i).setBackground(drawable);
                scoreViewList.get(i).setEnabled(false);
            }
            else {

                scoreViewList.get(i).setEnabled(true);
                scoreViewList.get(i).setBackground(lastBackgroundList.get(i));
            }
        }
    }

    public void setScore (int id, LEVEL_t level) {

        if (id > AVG || id < 0) {

            id = AVG;
        }

        Drawable drawable = scoreViewList.get(id).getBackground();
        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
        scoreViewList.get(id).setBackground(drawable);
    }
}
