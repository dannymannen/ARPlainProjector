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
import org.opencv.calib3d.Calib3d;
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
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
            snapCounter++;
            images.add(image.clone());
            if (snapCounter == 2) {
                resultButtonAnimation.show();
            }

            Mat imageDisplay = images.get(snapCounter-1);
            for (int i = 0;i<images.size();i++){
                if (images.get(i).equals(images.get(0))){
                }
            }
            Bitmap bm = Bitmap.createBitmap(imageDisplay.cols(), imageDisplay.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imageDisplay, bm);

            // find the imageview and draw it!
            ImageView iv = (ImageView) getView().findViewById(R.id.mostCurrentImage);
            iv.setImageBitmap(bm);

            if(snapCounter==4){
                allSnapsTaken=true;
            }
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        image = inputFrame.rgba();
        return image;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    public ArrayList<Bitmap> calculateResults(){
        //First image
        Mat input = images.get(0).clone();
        Bitmap currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
        ArrayList<Bitmap> results = new ArrayList<Bitmap>();

        FeatureDetector detector=FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint currentImageKeyPoints = new MatOfKeyPoint();
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // Detect first image keypoints
        detector.detect(input,currentImageKeyPoints);
        keyPoints.add(0, currentImageKeyPoints);
        Mat output = new Mat();
        // Draw first image keypoints
        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
        Features2d.drawKeypoints(input, currentImageKeyPoints, output);
        Utils.matToBitmap(output, currentResultImage);
        results.add(currentResultImage);

        // Compute first image descriptor
        descriptors.add(0, new Mat());
        surfExtractor.compute(input,keyPoints.get(0), descriptors.get(0));

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat OriginalImage = images.get(0).clone();
        Imgproc.cvtColor(OriginalImage,OriginalImage,Imgproc.COLOR_BGRA2BGR);


        for (int i =1; i<images.size(); i++){
            input = images.get(i).clone();
            currentResultImage = Bitmap.createBitmap(input.cols()*2, input.rows(),Bitmap.Config.ARGB_8888);

            // Detect ith image keypoints
            currentImageKeyPoints = new MatOfKeyPoint();
            detector.detect(input,currentImageKeyPoints);
            keyPoints.add(i, currentImageKeyPoints);

            // Compute ith image descriptor
            descriptors.add(i, new Mat());
            surfExtractor.compute(images.get(i),keyPoints.get(i), descriptors.get(i));

            // Match discriptor form the first image and the ith image
            matches.add(i-1,new MatOfDMatch());
            matcher.knnMatch(descriptors.get(0), descriptors.get(i), matches, 5);

            // Ratio test
            LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
            for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
                if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.9) {
                    good_matches.add(matOfDMatch.toArray()[0]);
                }
            }

            // Get keypoint coordinates of good matches to find homography and remove outliers using RANSAC
            List<Point> pts1 = new ArrayList<Point>();
            List<Point> pts2 = new ArrayList<Point>();
            for(int j = 0; j<good_matches.size(); j++){
                pts1.add(keyPoints.get(0).toList().get(good_matches.get(j).queryIdx).pt);
                pts2.add(keyPoints.get(i).toList().get(good_matches.get(j).trainIdx).pt);
            }

            // Convertion of data types - there is maybe a more beautiful way
            Mat outputMask = new Mat();
            MatOfPoint2f pts1Mat = new MatOfPoint2f();
            pts1Mat.fromList(pts1);
            MatOfPoint2f pts2Mat = new MatOfPoint2f();
            pts2Mat.fromList(pts2);

            // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
            // the smaller the allowed reprojection error (here 15), the more matches are filtered
            Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 0.995, outputMask);

            // OutputMask contains zeros and ones indicating which matches are filtered
            LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
            for (int j = 0; j < good_matches.size(); j++) {
                if (outputMask.get(j, 0)[0] != 0.0) {
                    better_matches.add(good_matches.get(j));
                }
            }

            // Draw all matches between first and the ith image
            Mat outputImg = new Mat();
            MatOfDMatch better_matches_mat = new MatOfDMatch();
            better_matches_mat.fromList(better_matches);
            Imgproc.cvtColor(OriginalImage, OriginalImage, Imgproc.COLOR_RGBA2RGB, 1);
            Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2RGB, 1);
            Features2d.drawMatches(OriginalImage,  keyPoints.get(0), input,  keyPoints.get(i), better_matches_mat, outputImg);
            Utils.matToBitmap(outputImg, currentResultImage);
            results.add(currentResultImage);
        }

        return results;
    }


    public boolean areAllSnapsTaken() {
        return allSnapsTaken;
    }


}