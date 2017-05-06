package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.NativeCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import Utilities.CircularToggleVisibilityAnimation;

public class MainFragment extends Fragment implements CvCameraViewListener2 {

    private int snapCounter;

    private JavaCameraView mOpenCvCameraView;
    private static final String TAG ="AR App :: ";
    private CircularToggleVisibilityAnimation resultButtonAnimation;
    private CircularToggleVisibilityAnimation snapButtonAnimation;
    private Mat image;
    private ArrayList<Mat> images;
    private boolean allSnapsTaken;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getActivity()) {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, getActivity(), mLoaderCallback);
        snapCounter=0;
        allSnapsTaken = false;

        mOpenCvCameraView = (JavaCameraView) getActivity().findViewById(R.id.OpenCvView);
        System.out.println(mOpenCvCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.disableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);

        View resultButton=  getActivity().findViewById(R.id.resultButton);
        View snapButton= getActivity().findViewById(R.id.snapButton);

        resultButton.setVisibility(View.INVISIBLE);

        resultButtonAnimation =new CircularToggleVisibilityAnimation(resultButton);
        snapButtonAnimation =new CircularToggleVisibilityAnimation(snapButton);

        ImageView iv = (ImageView) getView().findViewById(R.id.mostCurrentImage);
        iv.setImageResource(R.mipmap.no_image);

        images = new ArrayList<Mat>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public void clearImages(){
        ImageView iv = (ImageView) getView().findViewById(R.id.mostCurrentImage);
        iv.setImageResource(R.mipmap.no_image);
        images = new ArrayList<Mat>();
        snapCounter =0;
        allSnapsTaken=false;
        resultButtonAnimation.hide();
    }

    public void takeSnap(View v) {
        /*if (snapCounter==0){
            images = new ArrayList<Mat>();
        }*/
        if (image!=null && image.cols()>0 && image.rows()>0){
            System.out.println("trying to snap image with cols: "+image.cols()+" and rows: "+image.rows()+"-------------------------------");
            snapCounter++;
            images.add(image.clone());
            if (snapCounter == 2) {
                resultButtonAnimation.show();
            }

            Mat imageDisplay = images.get(snapCounter-1);
            for (int i = 0;i<images.size();i++){
                if (images.get(i).equals(images.get(0))){
                    System.out.println("Image "+i+" is the same as image 0.----------------");
                }
            }
            Bitmap bm = Bitmap.createBitmap(imageDisplay.cols(), imageDisplay.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imageDisplay, bm);

            // find the imageview and draw it!
            ImageView iv = (ImageView) getView().findViewById(R.id.mostCurrentImage);
            iv.setImageBitmap(bm);
            System.out.println("Number images snap: " + images.size());

            if(snapCounter==4){
                allSnapsTaken=true;
            }
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        image = inputFrame.rgba();
       // System.out.println("Image loaded with cols: "+image.cols()+" and rows: "+image.rows()+"-------------------------------");
        return image;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    public ArrayList<Bitmap> calculateResults(){
        System.out.println("Number images calc: " + images.size());
        Mat input = images.get(0).clone();                          //First image
        Bitmap currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
        ArrayList<Bitmap> results = new ArrayList<Bitmap>();

        FeatureDetector detector=FeatureDetector.create(FeatureDetector.FAST);
        MatOfKeyPoint currentImageKeyPoints = new MatOfKeyPoint();
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);

        detector.detect(input,currentImageKeyPoints);

        Mat output = new Mat();

        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
        Features2d.drawKeypoints(input, currentImageKeyPoints, output);
        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGR2BGRA);
        Utils.matToBitmap(output, currentResultImage);
        results.add(currentResultImage);


        for (int i =1; i<images.size(); i++){
            input = images.get(i).clone();
            currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);

            currentImageKeyPoints = new MatOfKeyPoint();

            detector.detect(input,currentImageKeyPoints);

            output = new Mat();

            Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
            Features2d.drawKeypoints(input, currentImageKeyPoints, output);
            Imgproc.cvtColor(input,input,Imgproc.COLOR_BGR2BGRA);
            Utils.matToBitmap(output, currentResultImage);
            results.add(currentResultImage);
        }


  /*      for (int i =0; i<images.size(); i++){
            Mat input = images.get(i).clone();
            System.out.println(input.toString());
            Bitmap currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(input, currentResultImage);
            results.add(currentResultImage);
            System.out.println(currentResultImage.toString());

        }*/

        return results;
    }


    public boolean areAllSnapsTaken() {
        return allSnapsTaken;
    }


}