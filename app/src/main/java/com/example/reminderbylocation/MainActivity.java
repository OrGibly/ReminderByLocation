package com.example.reminderbylocation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.reminderbylocation.Adapters.MyFragmentPagerAdapter;
import com.example.reminderbylocation.Data.DataBaseManager;
import com.example.locationbyreminder.R;

public class MainActivity extends AppCompatActivity {

    TabLayout tableLayout;
    ViewPager viewPager;
    MyFragmentPagerAdapter fragmentPagerAdapter;
    Bundle savedInstanceState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        tableLayout = findViewById(R.id.tabLayout);

        // ViewPager
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);

        // TabLayout
        tableLayout.setupWithViewPager(viewPager);

        showNewAlertSnackBarReminder();
    }

    private void showNewAlertSnackBarReminder(){
        if(savedInstanceState!=null)return;
        int numOfEntrances = App.getEntriesCount();
        DataBaseManager.DataSet dataSet = DataBaseManager.getInstance().getDataSet();
        //show snackbar if there are no alerts on list, or at the 1st,3rd,5th,7th
        // entrance to the app.
        if(dataSet.size()==0 ||
                (numOfEntrances<=7 && numOfEntrances%2==1)){
            Snackbar mySnackbar = Snackbar.make(findViewById(R.id.mainContainer),
                    R.string.snackbar_msg, Snackbar.LENGTH_INDEFINITE);
            View.OnClickListener doNothingListener = new View.OnClickListener(){@Override public void onClick(View v){}};
            mySnackbar.setAction(R.string.snackbar_action, doNothingListener);
            mySnackbar.setActionTextColor(getResources().getColor(R.color.colorSnackbarActionText));
            mySnackbar.show();
        }
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener(){
        @Override
        public void onPageSelected(int i) {
            fragmentPagerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPageScrollStateChanged(int i) {}
        @Override
        public void onPageScrolled(int i, float v, int i1) {}
    };
}
