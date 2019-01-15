package com.preventium.boxpreventium.module.Load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.LruCache;

import com.preventium.boxpreventium.gui.MainActivity;
import com.preventium.boxpreventium.gui.PreventiumApplication;

/**
 * Created by tog on 06/11/2018.
 */

public class LoadImage {

    // variable
    private Context context;
    private MainActivity main;

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

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap drawableToBitmap(int res) {
        Context context = PreventiumApplication.getContext();
        return drawableToBitmap(ResourcesCompat.getDrawable(context.getResources(), res, null));
    }

    public static Bitmap fileToBitmap(String filepath) {

        // check if exist
        Bitmap bitmap = (Bitmap)Cache.getInstance().getLru().get(filepath);
        if( bitmap != null ) return bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
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
