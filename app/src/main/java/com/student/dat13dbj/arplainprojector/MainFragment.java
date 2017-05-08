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
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
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
    private ArrayList<MatOfKeyPoint> keyPoints;
    private ArrayList<Mat> descriptors;
    private ArrayList<MatOfDMatch> matches;

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
        keyPoints = new ArrayList<MatOfKeyPoint>();
        descriptors = new ArrayList<Mat>();
        matches = new ArrayList<MatOfDMatch>();
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
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        detector.detect(input,currentImageKeyPoints);
        System.out.println("Detect original kp check");
        keyPoints.add(0, currentImageKeyPoints);
        Mat output = new Mat();

        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
        Features2d.drawKeypoints(input, currentImageKeyPoints, output);
        System.out.println("Draw original kp check");

        Utils.matToBitmap(output, currentResultImage);
        results.add(currentResultImage);

        descriptors.add(0, new Mat());
        surfExtractor.compute(input,keyPoints.get(0), descriptors.get(0));
        System.out.println("Descriptors created check");

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        System.out.println("Matcher created check");

        Mat OriginalImage = images.get(0).clone();
        Imgproc.cvtColor(OriginalImage,OriginalImage,Imgproc.COLOR_BGRA2BGR);


        for (int i =1; i<images.size(); i++){
            input = images.get(i).clone();
            currentResultImage = Bitmap.createBitmap(input.cols()*2, input.rows(),Bitmap.Config.ARGB_8888);

            currentImageKeyPoints = new MatOfKeyPoint();

            detector.detect(input,currentImageKeyPoints);
            System.out.println("Detected keypoints for image "+i+" check");

            keyPoints.add(i, currentImageKeyPoints);
            output = new Mat();

            //Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
           // Features2d.drawKeypoints(input, currentImageKeyPoints, output);
            //Imgproc.cvtColor(input,input,Imgproc.COLOR_BGR2BGRA);
            //Utils.matToBitmap(output, currentResultImage);
            //results.add(currentResultImage);

            descriptors.add(i, new Mat());
            surfExtractor.compute(images.get(i),keyPoints.get(i), descriptors.get(i));
            System.out.println("Computed descriptors for image "+i+" check");

            matches.add(i-1,new MatOfDMatch());
            matcher.match(descriptors.get(0), descriptors.get(i), matches.get(i-1));
            System.out.println("Matched descriptors for image "+i+" check");


            Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
            //Features2d.drawMatches(OriginalImage, keyPoints.get(0), input, keyPoints.get(i), matches.get(i-1),output);
            System.out.println("Draw matches for image "+i+" check");
            System.out.println("input width: " + input.cols() + "input height: " + input.rows());
            System.out.println("original width: " + OriginalImage.cols() + "original height: " + OriginalImage.rows());
            System.out.println("output width: " + output.cols() + "output height: " + output.rows());


            //feature and connection colors
            Scalar RED = new Scalar(255,0,0);
            Scalar GREEN = new Scalar(0,255,0);
            //output image
            MatOfByte drawnMatches = new MatOfByte();
            //this will draw all matches, works fine
            List<DMatch> matchList = matches.get(i-1).toList();
            ArrayList<DMatch> matches_final = new ArrayList<DMatch>();
            MatOfDMatch matches_final_mat = new MatOfDMatch();

            int DIST_LIMIT = 80;
            for(int j=0; j<matchList.size(); j++){
                if(matchList.get(j).distance <= DIST_LIMIT){
                    matches_final.add(matches.get(i-1).toList().get(j));
                }
            }

            matches_final_mat.fromList(matches_final);
         /*   for(int j=0; j< matches_final.size(); j++){
                System.out.println( matches_final.get(j));
            }


            Point floatPoint= keyPoints.get(0).toArray()[0].pt;
            System.out.println("x: "+floatPoint.x+" y: "+floatPoint.y+"------------------------------------------");

            matcher.knnMatch(descriptors.get(i), descriptors.get(0), matches, 2, new Mat() , false );
            for(int j=0; j<matches.get(i-1).rows(); j++){

                if(matches.get(i-1).toArray()[j*matches.get(i-1).cols()].distance > matches.get(i-1).toArray()[j*matches.get(i-1).cols()+1].distance * 0.6){
                    matches_final.add(i-1,matches.get(i-1).toArray()[j*matches.get(i-1).cols()+1]);
                }
            }
            matches_final_mat.fromList(matches_final);*/

            Features2d.drawMatches(OriginalImage,  keyPoints.get(0), input,  keyPoints.get(i), matches_final_mat,
                    output, GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
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