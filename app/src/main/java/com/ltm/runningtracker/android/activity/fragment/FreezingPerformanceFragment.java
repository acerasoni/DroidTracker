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
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class FreezingPerformanceFragment extends PerformanceFragment {

  @Override
  protected void onPopulateList() {
    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getFreezingRuns();
      getActivity().runOnUiThread(() -> {
        dataAdapter.swapCursor(c);
        dataAdapter.notifyDataSetChanged();
      });
    });
  }

}
