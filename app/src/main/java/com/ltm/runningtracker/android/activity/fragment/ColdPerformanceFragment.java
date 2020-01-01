package com.ltm.runningtracker.android.activity.fragment;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.database.Cursor;
import android.os.AsyncTask;

import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import java.util.Objects;

public class ColdPerformanceFragment extends PerformanceFragment {

  @Override
  public void onPopulateList() {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getRunsAsync(Objects.requireNonNull(getContext()), WeatherClassifier.COLD);
      Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
        // Must do the following sequentially to ensure correct behaviour
        // - fetch cursor from database asynchronously
        //  - Swap data adaptor's cursor with new one on UI thread
        // - Notify data adapter on UI thread
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }
}
