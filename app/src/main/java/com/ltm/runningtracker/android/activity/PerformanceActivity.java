package com.ltm.runningtracker.android.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.jjoe64.graphview.GraphView;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.fragment.ColdPerformanceFragment;
import com.ltm.runningtracker.android.activity.fragment.FreezingPerformanceFragment;
import com.ltm.runningtracker.android.activity.fragment.HotPerformanceFragment;
import com.ltm.runningtracker.android.activity.fragment.MildPerformanceFragment;
import com.ltm.runningtracker.android.activity.fragment.WarmPerformanceFragment;

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

    SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this,
        getSupportFragmentManager());

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
      Fragment fragment = null;
      switch (position) {
        case 0:
          fragment = new FreezingPerformanceFragment();
        break;
        case 1:
          fragment = new ColdPerformanceFragment();
        break;
        case 2:
          fragment = new MildPerformanceFragment();
        break;
        case 3:
          fragment = new WarmPerformanceFragment();
        break;
        case 4:
          fragment = new HotPerformanceFragment();
        break;
      }

      return fragment;
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
