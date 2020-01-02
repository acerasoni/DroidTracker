package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.fragment.impl.ColdPerformanceFragment;
import com.ltm.runningtracker.android.fragment.impl.FreezingPerformanceFragment;
import com.ltm.runningtracker.android.fragment.impl.HotPerformanceFragment;
import com.ltm.runningtracker.android.fragment.impl.MildPerformanceFragment;
import com.ltm.runningtracker.android.fragment.impl.WarmPerformanceFragment;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * This Activity holds the TabLayout and nested ViewPager which allow the user to navigate
 * in between weather fragments. It does not contain additional UI logic.
 */
public class PerformanceActivity extends AppCompatActivity {

  public static final Map<Class, Integer> FRAGMENT_TO_ID;

  // Required to determine which fragment we currently are on
  static {
    FRAGMENT_TO_ID = new HashMap<Class, Integer>() {
      {
        put(FreezingPerformanceFragment.class, 0);
        put(ColdPerformanceFragment.class, 1);
        put(MildPerformanceFragment.class, 2);
        put(WarmPerformanceFragment.class, 3);
        put(HotPerformanceFragment.class, 4);
      }
    };
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance);

    initialiseViews();
  }

  class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    SimpleFragmentPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    // This determines the fragment for each tab
    @NotNull
    @Override
    public Fragment getItem(int position) {
      Fragment fragment;
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
        default:
          throw new IllegalStateException(UNEXPECTED_VALUE + position);
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
          return getResources().getString(R.string.freezing);
        case 1:
          return getResources().getString(R.string.cold);
        case 2:
          return getResources().getString(R.string.mild);
        case 3:
          return getResources().getString(R.string.warm);
        case 4:
          return getResources().getString(R.string.hot);
        default:
          return null;
      }
    }
  }

  public void initialiseViews() {
    SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(
        getSupportFragmentManager());
    ViewPager viewPager = findViewById(R.id.viewPager);
    viewPager.setAdapter(adapter);
    TabLayout tabLayout = findViewById(R.id.tabLayout);
    tabLayout.setupWithViewPager(viewPager);
  }
}
