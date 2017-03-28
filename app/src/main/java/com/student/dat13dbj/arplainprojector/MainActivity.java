package com.student.dat13dbj.arplainprojector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.opencv.android.NativeCameraView;

public class MainActivity extends AppCompatActivity {

    private NativeCameraView mOpenCvCameraView;

   /* private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                   // Log.i(TAG, "OpenCV load ok");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /*mOpenCvCameraView = (NativeCameraView)findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener();*/

    }

    @Override
    public void onResume() {
        super.onResume();
      //  OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }
}