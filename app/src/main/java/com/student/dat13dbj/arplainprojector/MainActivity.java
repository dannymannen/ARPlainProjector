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

import org.opencv.core.Mat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private MainFragment mainFragment;
    private ResultFragment resultFragment;

    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;

    private ArrayList<Mat> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        System.out.println("Hej1");
       // android.os.Debug.waitForDebugger();
        System.out.println("Hej2");

        mainFragment = new MainFragment();
        resultFragment = new ResultFragment();

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);


        viewPager = (ViewPager) findViewById(R.id.pager);
        Fragment[] fragments = {mainFragment,resultFragment};
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),fragments);
        pagerAdapter.setLocked(true,0);
        viewPager.setAdapter(pagerAdapter);


       // System.out.println("fragment: "+viewPager.getCurrentItem());


    }

    @Override
    public void onResume() {
        System.out.println("hejjj");
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void showResults(View v){
        ArrayList<Bitmap> resultImages = mainFragment.calculateResults();
        for (Bitmap b:
                resultImages ) {
            System.out.println(b.toString());
        }
        pagerAdapter.setLocked(true,1);
        for (Bitmap b:
                resultImages ) {
            System.out.println(b.toString());
        }
        resultFragment.setResults(resultImages);
    }

    public void enterSnapMode(View v){
        pagerAdapter.setLocked(true,0);
        mainFragment.clearImages();
    }

    public void takeSnap(View v){
        mainFragment.takeSnap(v);
        if (mainFragment.areAllSnapsTaken()){
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