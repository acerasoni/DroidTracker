package com.ltm.runningtracker.android.activity.fragment;

import static android.app.Activity.RESULT_OK;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.BrowseRunDetailsActivity;
import com.ltm.runningtracker.android.activity.PerformanceActivity;
import com.ltm.runningtracker.android.activity.viewmodel.MainScreenActivityViewModel;
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;

public abstract class PerformanceFragment extends Fragment {

  public static final int BROWSE_RUN_REQUEST_CODE = 0;

  // Protected because must be accessed by other fragments
  protected final String RUN_DISPLAYED_DATA[] = new String[]{
      DroidProviderContract.ID,
      DroidProviderContract.DATE,
      DroidProviderContract.LOCATION,
      DroidProviderContract.TYPE
  };

  protected final int[] COLUMNS_RESULT_IDS = new int[]{
      R.id.id,
      R.id.date,
      R.id.location,
      R.id.type,
  };

  protected SimpleCursorAdapter dataAdapter;
  protected ListView listView;
  protected PerformanceViewModel performanceViewModel;
  protected View listLayout;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    performanceViewModel = ViewModelProviders.of(this).get(PerformanceViewModel.class);

    dataAdapter = new SimpleCursorAdapter(
        getActivity(),
        R.layout.run_list_item,
        // Will be swapped out in child classes
        null,
        RUN_DISPLAYED_DATA,
        COLUMNS_RESULT_IDS,
        0);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    Log.d("Create View", "Called");
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.performance_fragment, container, false);
    listView = view.findViewById(R.id.runList);
    onPopulateList();

    Class thisClass = this.getClass();

    // Start details activity when item is clicked
    listView.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowseRunDetailsActivity.class);
        Bundle bundle = new Bundle();

        // Get ID of run
        ViewGroup viewGroup = (ViewGroup) view;
        Integer runId = Integer.parseInt(((TextView) (viewGroup.getChildAt(0))).getText().toString());
        bundle.putInt("runId", runId);

        // Determine from which fragment so we can fetch the cursor from the right cache index
        bundle.putInt("fromFragment", PerformanceActivity.FRAGMENT_TO_ID.get(thisClass));

        intent.putExtras(bundle);
        startActivityForResult(intent, BROWSE_RUN_REQUEST_CODE);
      }
    });

    return view;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BROWSE_RUN_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        // Type was changed, refresh cache and update UI
        onPopulateList();
      } else {
        // Nothing changed, no need to re-populate
      }
    }
  }

  protected abstract void onPopulateList();

}
