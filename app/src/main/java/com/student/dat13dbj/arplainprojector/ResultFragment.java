package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import Utilities.CircularToggleVisibilityAnimation;

/**
 * Created by dat13aca on 26/04/2017.
 */

public class ResultFragment extends Fragment {




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.result_fragment, container, false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();

        //createNotification("My notification", "Hello World!");
    }

    public void setResults(ArrayList<Bitmap> results){
        ImageView firstResultImage = (ImageView) getView().findViewById(R.id.firstResultImage);
        ImageView secondResultImage = (ImageView) getView().findViewById(R.id.secondResultImage);
        ImageView thirdResultImage = (ImageView) getView().findViewById(R.id.thirdResultImage);
        ImageView fourthResultImage = (ImageView) getView().findViewById(R.id.fourthResultImage);
        firstResultImage.setVisibility(View.VISIBLE);
        secondResultImage.setVisibility(View.VISIBLE);
        thirdResultImage.setVisibility(View.VISIBLE);
        fourthResultImage.setVisibility(View.VISIBLE);

        switch (results.size()){
            case 1:
                firstResultImage.setImageBitmap(results.get(0));
                secondResultImage.setVisibility(View.INVISIBLE);
                thirdResultImage.setVisibility(View.INVISIBLE);
                fourthResultImage.setVisibility(View.INVISIBLE);

                break;
            case 2:
                firstResultImage.setImageBitmap(results.get(0));
                secondResultImage.setImageBitmap(results.get(1));
                thirdResultImage.setVisibility(View.INVISIBLE);
                fourthResultImage.setVisibility(View.INVISIBLE);
                break;
            case 3:
                firstResultImage.setImageBitmap(results.get(0));
                secondResultImage.setImageBitmap(results.get(1));
                thirdResultImage.setImageBitmap(results.get(2));
                fourthResultImage.setVisibility(View.INVISIBLE);
                break;
            case 4:
                firstResultImage.setImageBitmap(results.get(0));
                secondResultImage.setImageBitmap(results.get(1));
                thirdResultImage.setImageBitmap(results.get(2));
                fourthResultImage.setImageBitmap(results.get(3));
                break;
            default:

                break;
        }

    }



}
