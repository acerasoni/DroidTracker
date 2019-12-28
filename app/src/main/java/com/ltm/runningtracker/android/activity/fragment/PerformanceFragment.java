package com.ltm.runningtracker.android.activity.fragment;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public abstract class PerformanceFragment extends Fragment {

  // Protected because must be accessed by other fragments
  protected final String RUN_DISPLAYED_DATA[] = new String[]{
      ContentProviderContract.DATE,
      ContentProviderContract.WEATHER,
      ContentProviderContract.DURATION
  };

  protected final int[] COLUMNS_RESULT_IDS = new int[]{
      R.id.value1,
      R.id.value2,
      R.id.value3
  };

  protected SimpleCursorAdapter dataAdapter;
  protected ListView listView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.performance_fragment, container, false);
    listView = view.findViewById(R.id.runList);
    dataAdapter = new SimpleCursorAdapter(
        getApplicationContext(),
        R.layout.run_list_item,
        null,
        RUN_DISPLAYED_DATA,
        COLUMNS_RESULT_IDS,
        0);
    onPopulateList();
    return view;
  }

  protected abstract void onPopulateList();

}
