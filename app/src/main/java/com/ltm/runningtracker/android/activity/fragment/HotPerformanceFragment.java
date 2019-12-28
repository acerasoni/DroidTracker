package com.ltm.runningtracker.android.activity.fragment;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.database.Cursor;
import android.os.AsyncTask;

public class HotPerformanceFragment extends PerformanceFragment {

  @Override
  protected void onPopulateList() {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getHotRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
        listView.setAdapter(dataAdapter);
      });
    });
  }
}
