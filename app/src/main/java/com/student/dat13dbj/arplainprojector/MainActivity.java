package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;

import Utilities.CircularToggleVisibilityAnimation;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2{


    private int snapCounter;
    private JavaCameraView mOpenCvCameraView;
    private static final String TAG ="AR App :: ";
    private CircularToggleVisibilityAnimation resultAnimation;

    private Mat image;
    private ArrayList<Mat> images;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV load ok");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        View resultButton = findViewById(R.id.resultButton);
        resultButton.setVisibility(View.INVISIBLE);
        resultAnimation = new CircularToggleVisibilityAnimation(resultButton);

        //  Bitmap bMap= BitmapFactory.decodeResource(getResources(),R.drawable.image1);
        //ImageView img = (ImageView) findViewById(R.id.cameraView);
        //img.setImageResource(R.drawable.image1);

        mOpenCvCameraView = (JavaCameraView)findViewById(R.id.OpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.disableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);

        snapCounter=0;
        images = new ArrayList<Mat>();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        image = inputFrame.rgba();
        return image;
    }

    public void takeSnap(View v) {
        snapCounter++;
        if(snapCounter<5) {
            images.add(image);
            if (snapCounter == 2) {
                resultAnimation.show();
            }

            Mat imageDisplay = images.get(snapCounter-1);
            Bitmap bm = Bitmap.createBitmap(imageDisplay.cols(), imageDisplay.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imageDisplay, bm);

            // find the imageview and draw it!
                ImageView iv = (ImageView) findViewById(R.id.imageView);
                iv.setImageBitmap(bm);

        }
    }
}