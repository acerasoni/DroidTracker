package com.ltm.runningtracker.android.activity.fragment;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.PerformanceActivity;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class ColdPerformanceFragment extends PerformanceFragment {

  @Override
  public void onPopulateList() {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getRunsAsync(getContext(), WeatherClassifier.COLD);
      getActivity().runOnUiThread(() -> {
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
