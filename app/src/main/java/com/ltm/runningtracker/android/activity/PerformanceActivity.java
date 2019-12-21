package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class PerformanceActivity extends AppCompatActivity {


  private SimpleCursorAdapter dataAdapter;
  private ListView listView;
  private TabLayout tabLayout;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance);
    listView = findViewById(R.id.runList);
    dataAdapter = new SimpleCursorAdapter(
        getApplicationContext(),
        R.layout.run_list_item,
        null,
        RUN_DISPLAYED_DATA,
        COLUMNS_RESULT_IDS,
        0);

    tabLayout = findViewById(R.id.weatherTabs);
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
  }

  public void onFreezing(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getFreezingRuns();
      runOnUiThread(() -> {
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
      runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onMild(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getMildRuns();
      runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onWarm(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getWarmRuns();
      runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onHot(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getHotRuns();
      runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }

  public void onAllRuns(@Nullable View v) {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getAllRuns();
      runOnUiThread(() -> listView.setAdapter(dataAdapter));
    });
  }

}
