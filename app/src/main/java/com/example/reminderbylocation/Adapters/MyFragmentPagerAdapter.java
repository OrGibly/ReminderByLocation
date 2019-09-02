package com.example.reminderbylocation.Adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.reminderbylocation.AlertsListFragment;
import com.example.reminderbylocation.AlertsMapFragment;
import com.example.reminderbylocation.Updateable;

//An adapter which adapts the two main fragments (alert list and map)
// to the ViewPager in the main activity.
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int NUM_OF_FRAGMENTS=2;
    //number of titles(titles.length) must be NUM_OF_FRAGMENTS.
    private CharSequence[] titles =  new String[]{"Alerts","Map"};

    FragmentManager fragmentManager;

    /**
     * Constructor.
     * @param fm The FragmentManager used for the fragments.
     */
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        //Number of fragments here must be NUM_OF_FRAGMENTS.
        if(position==0)fragment=new AlertsListFragment();
        else if(position==1)fragment=new AlertsMapFragment();
        else fragment=null;
        return fragment;
    }

    @Override
    public int getCount() {
        return NUM_OF_FRAGMENTS;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        Updateable f = (Updateable)object;
        if (f != null) {
            f.update();
        }
        return super.getItemPosition(object);
    }
}
