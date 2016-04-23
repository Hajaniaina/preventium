package com.preventium.boxpreventium.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import com.preventium.boxpreventium.qrcode.CameraOK;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;

/**
 * Created by Franck on 06/06/2016.
 */


public class Camera {

    private static final String TAG = "Camera";
    private Context context;
    private Size mPreviewSize;
    private AutoFitTextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

//    int mWidth = 1280;
//    int mHeight = 720;
//    int mYSize = mWidth*mHeight;
//    int mUVSize = mYSize/4;
//    int mFrameSize = mYSize+(mUVSize*2);
//
//    private HandlerThread mCameraHandlerThread;
//    private Handler mCameraHandler;
//
//    private byte[] tempYbuffer = new byte[mYSize];
//    private byte[] tempUbuffer = new byte[mUVSize];
//    private byte[] tempVbuffer = new byte[mUVSize];
//
//    private ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.JPEG, 30);
//
//    ImageReader.OnImageAvailableListener mImageAvailListener = new ImageReader.OnImageAvailableListener() {
//        @Override
//        public void onImageAvailable(ImageReader reader) {
//            Image image = reader.acquireNextImage();
//            Image.Plane[] planes = image.getPlanes();
//            ByteBuffer sourceBuffer = planes[0].getBuffer();
//            sourceBuffer.get(tempYbuffer, 0, tempYbuffer.length);
//
//            ByteBuffer vByteBuf = planes[1].getBuffer();
//            vByteBuf.get(tempVbuffer);
//
//            ByteBuffer yByteBuf = planes[2].getBuffer();
//            yByteBuf.get(tempUbuffer);
//
//            //free the Image
//            image.close();
//        }
//    };

    public Camera(Context context){
        this.context = context;
        this.mTextureView = null;


    }

    public void setAutoFitTextureView(AutoFitTextureView autoFitTextureView) {
        mTextureView = autoFitTextureView;
        mTextureView.setSurfaceTextureListener( mSurfaceTextureListener );
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    public void closeCamera() {
        if( null != mCameraDevice ) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);

            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            Log.e(TAG, "openCamera E");
            try {
                String cameraId = manager.getCameraIdList()[0];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height, largest);
                if( context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }


                manager.openCamera(cameraId, mStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "openCamera X");

            //openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.e(TAG, "onSurfaceTextureUpdated");
        }

    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {Log.e(TAG, "onDisconnected");}
        @Override
        public void onError(CameraDevice camera, int error) {Log.e(TAG, "onError");}
    };

    protected void startPreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice || null == mTextureView || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
            return;
        }
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {Log.d(TAG, "onConfigureFailed");}
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if(null == mCameraDevice) {Log.e(TAG, "updatePreview error, return");}
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        android.os.Handler backgroundHandler = new android.os.Handler(thread.getLooper());
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize ) {
            return;
        }
        int rotation = context.getResources().getConfiguration().orientation;
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

}
