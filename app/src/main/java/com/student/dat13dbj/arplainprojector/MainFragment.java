package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
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
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.IOException;
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

    public ArrayList<Bitmap> calculatePointMatchingResults(){
        //First image
        Mat input = images.get(0).clone();
        //Load last supper image
        Mat resourceImage = null;
        try {
            resourceImage = Utils.loadResource(getActivity(), R.mipmap.chess_cyan);
        } catch (IOException e) {
            System.out.println("Fail");
            e.printStackTrace();
        }
        Imgproc.resize(resourceImage,resourceImage,new Size(800,460));

        Bitmap currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
        ArrayList<Bitmap> results = new ArrayList<Bitmap>();

        FeatureDetector detector=FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint currentImageKeyPoints = new MatOfKeyPoint();
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        // Detect first image keypoints
        detector.detect(input,currentImageKeyPoints);
        keyPoints.add(0, currentImageKeyPoints);
        Mat output = new Mat();
        // Draw first image and its keypoints
        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
        Features2d.drawKeypoints(input, currentImageKeyPoints, output);
        Utils.matToBitmap(output, currentResultImage);
        results.add(currentResultImage);
        //Draw resource image
        currentResultImage = Bitmap.createBitmap(resourceImage.cols(), resourceImage.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resourceImage, currentResultImage);
        results.add(currentResultImage);

        // Compute first image descriptor
        descriptors.add(0, new Mat());
        surfExtractor.compute(input,keyPoints.get(0), descriptors.get(0));

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat OriginalImage = images.get(0).clone();
        Imgproc.cvtColor(OriginalImage,OriginalImage,Imgproc.COLOR_BGRA2BGR);


        for (int i =1; i<images.size(); i++){

            try {
                resourceImage = Utils.loadResource(getActivity(), R.mipmap.chess_cyan);
            } catch (IOException e) {
                System.out.println("Fail");
                e.printStackTrace();
            }
            Imgproc.resize(resourceImage,resourceImage,new Size(800,460));

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
            matcher.knnMatch(descriptors.get(0), descriptors.get(i), matches, 2);

            // Ratio test
            LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
            for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
                if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.6) {
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
            if(pts1Mat.rows() >= 4|| pts2Mat.rows() >= 4) {
                Mat homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 0.995, outputMask);

            Mat homogImage= new Mat();

            Imgproc.warpPerspective(resourceImage, homogImage, homog, new Size(resourceImage.cols(), resourceImage.rows()));
            System.out.println("Homog for image "+i+" size is: "+homog.rows()+" "+homog.cols());
            System.out.println("Resource image size is: "+resourceImage.rows()+" "+resourceImage.cols());

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

            /*
            //Calibrate camera
            Mat cameraMat = new Mat();
            Mat dist = new Mat();
            ArrayList<Mat> rvec = new ArrayList<Mat>();
            ArrayList<Mat> tvec = new ArrayList<Mat>();
            ArrayList<Mat> worldPoints = new ArrayList<Mat>();
            worldPoints.add(0, new Mat());
            better_matches_mat.assignTo(worldPoints.get(0));
            ArrayList<Mat> imagePoints = new ArrayList<Mat>();
            imagePoints.add(0, new Mat());
            pts1Mat.assignTo(imagePoints.get(0));
            System.out.println("worldPoints: " + worldPoints.toString());
            System.out.println("imagePoints: " + imagePoints.toString());
            Calib3d.calibrateCamera(worldPoints, imagePoints, OriginalImage.size(), cameraMat, dist, rvec, tvec); //
            System.out.println("CameraMat: " + cameraMat.toString());
            */
            //Update result
            Utils.matToBitmap(outputImg, currentResultImage);
            results.add(currentResultImage);

            currentResultImage = Bitmap.createBitmap(homogImage.cols(), homogImage.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(homogImage, currentResultImage);
            results.add(currentResultImage);
            }
        }

        return results;
    }


    public ArrayList<Bitmap> calculateObjectResults(){
        //First image
        Mat input = images.get(0).clone();

        //Load last supper image
        Mat resourceImage = null;
        try {
            resourceImage = Utils.loadResource(getActivity(), R.mipmap.chess_cyan);
        } catch (IOException e) {
            System.out.println("Fail");
            e.printStackTrace();
        }

        Imgproc.resize(resourceImage,resourceImage,new Size(400,230));
        Imgproc.cvtColor(resourceImage,resourceImage,Imgproc.COLOR_RGB2RGBA);

        for (int i = 0; i<input.rows(); i++){
            if(i>=input.rows()/4&&i<input.rows()*3/4){
                for (int j = 0; j<input.cols(); j++){
                    if(j>=input.cols()/4&&j<input.cols()*3/4){
                        //System.out.println(i-input.rows()/4);
                        //System.out.println(j-input.cols()/4);
                        if(i-input.rows()/4<230&&j-input.cols()/4<400) {
                            input.put(i, j, resourceImage.get(i - input.rows() / 4, j - input.cols() / 4));
                        }
                    }
                }
            }
        }
        Mat canvas=input.submat(input.rows()/4,input.rows()*3/4,input.cols()/4,input.cols()*3/4);
        //resourceImage.copyTo(canvas);
        //canvas.copyTo(input);
        Bitmap currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
        ArrayList<Bitmap> results = new ArrayList<Bitmap>();

        FeatureDetector detector=FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint currentImageKeyPoints = new MatOfKeyPoint();
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        // Detect first image keypoints
        detector.detect(input,currentImageKeyPoints);
        keyPoints.add(0, currentImageKeyPoints);

        // Draw first image and its keypoints
//        Imgproc.cvtColor(input,input,Imgproc.COLOR_BGRA2BGR);
        Utils.matToBitmap(input, currentResultImage);
        results.add(currentResultImage);

        // Compute first image descriptor
        descriptors.add(0, new Mat());
        surfExtractor.compute(input,keyPoints.get(0), descriptors.get(0));

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat OriginalImage = images.get(0).clone();
        Imgproc.cvtColor(OriginalImage,OriginalImage,Imgproc.COLOR_BGRA2BGR);


        for (int i =1; i<images.size(); i++){

            try {
                resourceImage = Utils.loadResource(getActivity(), R.mipmap.chess_cyan);
            } catch (IOException e) {
                System.out.println("Fail");
                e.printStackTrace();
            }

            Imgproc.resize(resourceImage,resourceImage,new Size(400,230));


            input = images.get(i).clone();

            // Detect ith image keypoints
            currentImageKeyPoints = new MatOfKeyPoint();
            detector.detect(input,currentImageKeyPoints);
            keyPoints.add(i, currentImageKeyPoints);

            // Compute ith image descriptor
            descriptors.add(i, new Mat());
            surfExtractor.compute(images.get(i),keyPoints.get(i), descriptors.get(i));

            // Match discriptor form the first image and the ith image
            matches.add(i-1,new MatOfDMatch());
            matcher.knnMatch(descriptors.get(0), descriptors.get(i), matches, 2);

            // Ratio test
            LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
            for (Iterator<MatOfDMatch> iterator = matches.iterator(); iterator.hasNext();) {
                MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
                if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.6) {
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
            if(pts1Mat.rows() >= 4|| pts2Mat.rows() >= 4) {
                Mat homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 0.995, outputMask);

                Mat homogImage= new Mat();

                Imgproc.warpPerspective(resourceImage, homogImage, homog, new Size(input.cols(), input.rows()));
                System.out.println("Homog for image "+i+" size is: "+homog.rows()+" "+homog.cols());
                System.out.println("Resource image size is: "+resourceImage.rows()+" "+resourceImage.cols());

                // OutputMask contains zeros and ones indicating which matches are filtered
                LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
                for (int j = 0; j < good_matches.size(); j++) {
                    if (outputMask.get(j, 0)[0] != 0.0) {
                        better_matches.add(good_matches.get(j));
                    }
                }

                // Draw all matches between first and the ith image
                MatOfDMatch better_matches_mat = new MatOfDMatch();
                better_matches_mat.fromList(better_matches);

                Imgproc.resize(homogImage,homogImage,new Size(input.cols(),input.rows()));

                Imgproc.cvtColor(homogImage,homogImage,Imgproc.COLOR_RGB2RGBA);

                int startFraction =5;
                boolean useFraction = true;

                for (int k = 0; k<input.rows(); k++){
                        for (int l = 0; l<input.cols(); l++){
                            if (useFraction) {
                                if (k - input.rows() / startFraction >= 0 && l - input.cols() / startFraction >= 0) {
                                    if (isNotBlack(homogImage.get(k - input.rows() / startFraction, l - input.cols() / startFraction))) {
                                        //   printColVals(homogImage.get(k - input.rows() / 4, l - input.cols() / 4));
                                        input.put(k, l, homogImage.get(k - input.rows() / startFraction, l - input.cols() / startFraction));
                                    }
                                }
                            }else{
                                if (isNotBlack(homogImage.get(k, l))) {
                                    //   printColVals(homogImage.get(k - input.rows() / 4, l - input.cols() / 4));
                                    input.put(k, l, homogImage.get(k, l));
                                }
                            }
                        }
                }

                canvas=input.submat(input.rows()/4,input.rows()*3/4,input.cols()/4,input.cols()*3/4);
                homogImage.copyTo(canvas);
                currentResultImage = Bitmap.createBitmap(input.cols(), input.rows(),Bitmap.Config.ARGB_8888);
                Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2RGB, 1);

            /*
            //Calibrate camera
            Mat cameraMat = new Mat();
            Mat dist = new Mat();
            ArrayList<Mat> rvec = new ArrayList<Mat>();
            ArrayList<Mat> tvec = new ArrayList<Mat>();
            ArrayList<Mat> worldPoints = new ArrayList<Mat>();
            worldPoints.add(0, new Mat());
            better_matches_mat.assignTo(worldPoints.get(0));
            ArrayList<Mat> imagePoints = new ArrayList<Mat>();
            imagePoints.add(0, new Mat());
            pts1Mat.assignTo(imagePoints.get(0));
            System.out.println("worldPoints: " + worldPoints.toString());
            System.out.println("imagePoints: " + imagePoints.toString());
            Calib3d.calibrateCamera(worldPoints, imagePoints, OriginalImage.size(), cameraMat, dist, rvec, tvec); //
            System.out.println("CameraMat: " + cameraMat.toString());
            */
                //Update result
                Utils.matToBitmap(input, currentResultImage);
                results.add(currentResultImage);
            }
        }

        return results;
    }

    private void printColVals(double[] doubles) {
        for (double d: doubles){
            System.out.println(d);
        }
        System.out.println("");
    }

    private boolean isNotBlack(double[] doubles) {
        double threshold =0;
        int counter =0;
        for (double d: doubles){
            if (d>threshold){
                return true;
            }
            if (counter==2){
                return false;
            }
            counter++;
        }
        return false;
    }

    public boolean areAllSnapsTaken() {
        return allSnapsTaken;
    }


}