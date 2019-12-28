package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.COLD_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.FREEZING_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.HOT_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.MILD_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.WARM_RUNS_URI;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.fragment.PerformanceFragment;

/**
 * Graph 1 = Running pace (average speed) Graph 2 = Total distance Graph 3 = Total time
 */
public class PerformanceActivity extends AppCompatActivity {

  private GraphView graphView;
  private TabLayout tabLayout;
  private ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance);

    SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());

    viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(adapter);
    tabLayout = findViewById(R.id.tabLayout);
    tabLayout.setupWithViewPager(viewPager);
  }

  class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
      super(fm);
      mContext = context;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
      if (position == 0) {
        return new PerformanceFragment();
      } else if (position == 1){
        return new PerformanceFragment();
      } else if (position == 2){
        return new PerformanceFragment();
      } else {
        return new PerformanceFragment();
      }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
      return 5;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
      // Generate title based on item position
      switch (position) {
        case 0:
          return "Freezing";
        case 1:
          return "Cold";
        case 2:
          return "Mild";
        case 3:
          return "Warm";
        case 4:
          return "Hot";
        default:
          return null;
      }
    }

  }

}
