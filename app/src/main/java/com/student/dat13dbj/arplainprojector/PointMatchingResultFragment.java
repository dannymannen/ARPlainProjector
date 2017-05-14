package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dat13aca on 26/04/2017.
 */

public class PointMatchingResultFragment extends Fragment {


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.point_matching_result_fragment, container, false);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setResults(ArrayList<Bitmap> results) {
        ImageView firstResultImage = (ImageView) getView().findViewById(R.id.firstResultImage);
        ImageView secondResultImage = (ImageView) getView().findViewById(R.id.secondResultImage);
        ImageView thirdResultImage = (ImageView) getView().findViewById(R.id.thirdResultImage);
        ImageView fourthResultImage = (ImageView) getView().findViewById(R.id.fourthResultImage);

        ImageView firstHomogImage = (ImageView) getView().findViewById(R.id.firstHomogImage);
        ImageView secondHomogImage = (ImageView) getView().findViewById(R.id.secondHomogImage);
        ImageView thirdHomogImage = (ImageView) getView().findViewById(R.id.thirdHomogImage);
        ImageView fourthHomogImage = (ImageView) getView().findViewById(R.id.fourthHomogImage);

        TextView errorLabel = (TextView) getView().findViewById(R.id.notEnoughMatchesError);

        firstResultImage.setVisibility(View.VISIBLE);
        secondResultImage.setVisibility(View.VISIBLE);
        thirdResultImage.setVisibility(View.VISIBLE);
        fourthResultImage.setVisibility(View.VISIBLE);

        firstHomogImage.setVisibility(View.VISIBLE);
        secondHomogImage.setVisibility(View.VISIBLE);
        thirdHomogImage.setVisibility(View.VISIBLE);
        fourthHomogImage.setVisibility(View.VISIBLE);

        errorLabel.setVisibility(View.INVISIBLE);

        System.out.println(results.size());

        switch (results.size()) {
            case 2:
                firstResultImage.setImageBitmap(results.get(0));
                firstHomogImage.setImageBitmap(results.get(1));

                secondResultImage.setVisibility(View.INVISIBLE);
                thirdResultImage.setVisibility(View.INVISIBLE);
                fourthResultImage.setVisibility(View.INVISIBLE);

                secondHomogImage.setVisibility(View.INVISIBLE);
                thirdHomogImage.setVisibility(View.INVISIBLE);
                fourthHomogImage.setVisibility(View.INVISIBLE);

                errorLabel.setVisibility(View.VISIBLE);
                break;
            case 4:
                firstResultImage.setImageBitmap(results.get(0));
                firstHomogImage.setImageBitmap(results.get(1));
                secondResultImage.setImageBitmap(results.get(2));
                secondHomogImage.setImageBitmap(results.get(3));

                thirdHomogImage.setVisibility(View.INVISIBLE);
                fourthHomogImage.setVisibility(View.INVISIBLE);

                thirdResultImage.setVisibility(View.INVISIBLE);
                fourthResultImage.setVisibility(View.INVISIBLE);
                break;
            case 6:
                firstResultImage.setImageBitmap(results.get(0));
                firstHomogImage.setImageBitmap(results.get(1));
                secondResultImage.setImageBitmap(results.get(2));
                secondHomogImage.setImageBitmap(results.get(3));
                thirdResultImage.setImageBitmap(results.get(4));
                thirdHomogImage.setImageBitmap(results.get(5));

                fourthHomogImage.setVisibility(View.INVISIBLE);

                fourthResultImage.setVisibility(View.INVISIBLE);
                break;
            case 8:
                firstResultImage.setImageBitmap(results.get(0));
                firstHomogImage.setImageBitmap(results.get(1));
                secondResultImage.setImageBitmap(results.get(2));
                secondHomogImage.setImageBitmap(results.get(3));
                thirdResultImage.setImageBitmap(results.get(4));
                thirdHomogImage.setImageBitmap(results.get(5));
                fourthResultImage.setImageBitmap(results.get(6));
                fourthHomogImage.setImageBitmap(results.get(7));
                break;
            default:

                break;
        }

    }


}
