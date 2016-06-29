package com.preventium.boxpreventium;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Franck on 29/06/2016.
 */

public class GifView extends View{

    private Movie mMovie;
    private long mMovieStart;
    private boolean mVisible = true;
    private volatile boolean mPaused = false;
    // Scaling factor to fit the animation within view bounds.
    private float mScale;
    // Scaled movie frames width and height.
    private int mMeasuredMovieWidth, mMeasuredMovieHeight;
    // Position for drawing animation frames in the center of the view.
    private float mLeft, mTop;


    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getStyleableAttributes( context, attrs );
    }

    public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getStyleableAttributes( context, attrs );
    }

    private void getStyleableAttributes(Context context, AttributeSet attrs) {
        int src = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", 0);
        loadGIFRessource(context, src);
    }

    public void loadGIFRessource( Context context, int id ) {
        this.setLayerType( View.LAYER_TYPE_SOFTWARE, null );
        InputStream is = context.getResources().openRawResource( id );
        mMovie = Movie.decodeStream( is );
    }

    public void loadGIFAsset( Context context, String filename ) {
        InputStream is;
        try {
            is = context.getResources().getAssets().open( filename );
            mMovie = Movie.decodeStream( is );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPaused( boolean paused ) {
        mPaused = paused;
        invalidateView();
    }

    private void invalidateView() {
        if(mVisible) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                postInvalidateOnAnimation();
            } else {
                invalidate();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisible = (visibility == View.VISIBLE);
        invalidateView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if( mMovie == null ) {
            // No movie set, just set minimum available size.
            setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
        } else {
            int movieWidth = mMovie.width();
            int movieHeight = mMovie.height();

            // Calculate horizontal scaling
            float scaleH = 1f;
            int measureModeWidth = View.MeasureSpec.getMode(widthMeasureSpec);
            if (measureModeWidth != View.MeasureSpec.UNSPECIFIED) {
                int maximumWidth = View.MeasureSpec.getSize(widthMeasureSpec);
                if (movieWidth > maximumWidth) {
                    scaleH = (float) movieWidth / (float) maximumWidth;
                }
            }

            // Calculate vertical scaling
            float scaleW = 1f;
            int measureModeHeight = View.MeasureSpec.getMode(heightMeasureSpec);
            if (measureModeHeight != View.MeasureSpec.UNSPECIFIED) {
                int maximumHeight = View.MeasureSpec.getSize(heightMeasureSpec);
                if (movieHeight > maximumHeight) {
                    scaleW = (float) movieHeight / (float) maximumHeight;
                }
            }

            // Calculate overall scale
            mScale = 1f / Math.max(scaleH, scaleW);
            mMeasuredMovieWidth = (int) (movieWidth * mScale);
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);

        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // Calculate left / top for drawing in center
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
        mVisible = ( getVisibility() == View.VISIBLE );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mMovie != null) {
            if (!mPaused) {
                updateAnimationTime();
                drawMovieFrame(canvas);
                invalidateView();
            } else {
                drawMovieFrame(canvas);
            }
        }
    }


    // Calculate current animation time
    private void updateAnimationTime() {
        long now = SystemClock.uptimeMillis();
        if( mMovieStart == 0 ) mMovieStart = now;
        int relTime = (int)( (now - mMovieStart) % mMovie.duration() );
        mMovie.setTime( relTime );
    }

    // Draw current GIF frame
    private void drawMovieFrame(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScale, mScale);
        mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
        canvas.restore();
    }
}
