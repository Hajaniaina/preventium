package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;

import com.preventium.boxpreventium.gui.MainActivity;

/**
 * Created by tog on 06/11/2018.
 */

public class LoadImage {

    // variable
    private Context context;
    private MainActivity main;

    public LoadImage (Context context) {
        this.context = context;
        main = getMain(context);
    }

    private MainActivity getMain(Context context) {
        return (MainActivity) context;
    }

    public Bitmap drawableToBitmap(int res) {
        Bitmap bitmap = null;
        try {
            bitmap = ((BitmapDrawable) context.getResources().getDrawable(res)).getBitmap();
        }catch(Exception e) {
            VectorDrawable vector = ((VectorDrawable) context.getResources().getDrawable(res));

            bitmap = Bitmap.createBitmap(vector.getIntrinsicWidth(), vector.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            bitmap.setHasAlpha(true);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            vector.setBounds(0, 0, vector.getIntrinsicWidth(), vector.getIntrinsicHeight());
            vector.draw(canvas);
        }
        return bitmap;
    }

    public Bitmap fileToBitmap(String filepath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        return bitmap;
    }
}
