package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.LruCache;

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

    public class ImageCache extends LruCache<String, Bitmap> {

        public ImageCache( int maxSize ) {
            super( maxSize );
        }

        @Override
        protected int sizeOf( String key, Bitmap value ) {
            return value.getByteCount();
        }

        @Override
        protected void entryRemoved( boolean evicted, String key, Bitmap oldValue, Bitmap newValue ) {
            oldValue.recycle();
        }

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

        // check if exist
        Bitmap bitmap = (Bitmap)Cache.getInstance().getLru().get(filepath);
        if( bitmap != null ) return bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(filepath, options);

        // save it
        //Saving bitmap to cache. it will later be retrieved using the bitmap_image key
        // Cache.getInstance().getLru().put(filepath, bitmap);
        return bitmap;
    }

    public static class Cache {

        private static Cache instance;
        private LruCache<String, Bitmap> lru;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;

        private Cache() {
            lru = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        public static Cache getInstance() {
            if (instance == null) {
                instance = new Cache();
            }
            return instance;
        }

        public LruCache<String, Bitmap> getLru() {
            return lru;
        }
    }
}
