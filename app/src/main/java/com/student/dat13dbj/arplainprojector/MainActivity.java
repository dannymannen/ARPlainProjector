package com.student.dat13dbj.arplainprojector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainFragment mainFragment;
    private PointMatchingResultFragment pointMatchingResultFragment;
    private ObjectResultFragment objectResultFragment;

    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mainFragment = new MainFragment();
        pointMatchingResultFragment = new PointMatchingResultFragment();
        objectResultFragment = new ObjectResultFragment();

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        viewPager = (ViewPager) findViewById(R.id.pager);
        Fragment[] fragments = {mainFragment, pointMatchingResultFragment, objectResultFragment};
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), fragments);
        pagerAdapter.setLocked(true, 0);
        viewPager.setAdapter(pagerAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void showResults(View v) {

        Switch resultModeSwitch = (Switch) findViewById(R.id.resultSwitch);
        boolean showMatchedPointsResult = resultModeSwitch.isChecked();

        ArrayList<Bitmap> pointMatchingResultImages = mainFragment.calculatePointMatchingResults();
        ArrayList<Bitmap> objectResultImages = mainFragment.calculateObjectResults();

        if (showMatchedPointsResult) {
            pagerAdapter.setLocked(true, 1);
            pointMatchingResultFragment.setResults(pointMatchingResultImages);
        } else {
            pagerAdapter.setLocked(true, 2);
            objectResultFragment.setResults(objectResultImages);
        }
    }

    public void enterSnapMode(View v) {
        pagerAdapter.setLocked(true, 0);
        mainFragment.clearImages();
    }

    public void takeSnap(View v) {
        mainFragment.takeSnap(v);
        if (mainFragment.areAllSnapsTaken()) {
            showResults(v);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private Fragment[] fragments;
        private boolean locked = true;
        private int lockedIndex;

        public ScreenSlidePagerAdapter(FragmentManager fm, Fragment[] fragments) {
            super(fm);
            this.fragments = fragments;
        }

        public void setLocked(boolean locked, int page) {
            this.locked = locked;
            lockedIndex = page;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            if (locked) return fragments[lockedIndex];
            return fragments[position];

        }

        @Override
        public int getCount() {
            if (locked) return 1;
            return fragments.length;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}