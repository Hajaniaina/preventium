package com.preventium.boxpreventium.camera;

import android.app.Activity;
import android.os.Bundle;

import com.preventium.boxpreventium.R;

/**
 * Created by Franck on 06/06/2016.
 */

public class CameraActivity extends Activity {

    private Camera camera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        camera = new Camera(getApplicationContext());
        camera.setAutoFitTextureView( (AutoFitTextureView)findViewById(R.id.textureView) );
        camera.openCamera();
    }

    @Override
    protected void onPause(){
        super.onPause();
        camera.closeCamera();
    }

}
