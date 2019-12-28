package com.ltm.runningtracker.android.activity.fragment;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class PerformanceFragment extends Fragment {

  private final String RUN_DISPLAYED_DATA[] = new String[]{
      ContentProviderContract.DATE,
      ContentProviderContract.WEATHER,
      ContentProviderContract.DURATION
  };

  private final int[] COLUMNS_RESULT_IDS = new int[]{
      R.id.value1,
      R.id.value2,
      R.id.value3
  };

  private PerformanceViewModel mViewModel;
  private SimpleCursorAdapter dataAdapter;
  private ListView listView;
  private TabLayout tabLayout;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.performance_fragment, container, false);
    mViewModel = ViewModelProviders.of(this).get(PerformanceViewModel.class);
    listView = view.findViewById(R.id.runList);
    dataAdapter = new SimpleCursorAdapter(
        getApplicationContext(),
        R.layout.run_list_item,
        null,
        RUN_DISPLAYED_DATA,
        COLUMNS_RESULT_IDS,
        0);

    tabLayout = view.findViewById(R.id.weatherTabs);
    tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
      @Override
      public void onTabSelected(Tab tab) {
        switch (tab.getPosition()) {
          case 0:
            onFreezing(null);
            break;
          case 1:
            onCold(null);
            break;
          case 2:
            onMild(null);
            break;
          case 3:
            onWarm(null);
            break;
          case 4:
            onHot(null);
            break;
        }
      }

      @Override
      public void onTabUnselected(Tab tab) {

      }

      @Override
      public void onTabReselected(Tab tab) {

      }
    });

    // Begin on freezing tab
    onFreezing(null);

    return view;
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  public void onFreezing(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getFreezingRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
      });
    });
  }

  public void onCold(@Nullable View v) {

    // Must do the following sequentially to ensure correct behaviour
    // - fetch cursor from database asynchronously
    // - Swap data adaptor's cursor with new one on UI thread
    // - Notify data adapter on UI thread
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getColdRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onMild(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getMildRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onWarm(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getWarmRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onHot(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getHotRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onAllRuns(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getAllRuns();
      getActivity().runOnUiThread(() -> listView.setAdapter(dataAdapter));
    });
  }

}
