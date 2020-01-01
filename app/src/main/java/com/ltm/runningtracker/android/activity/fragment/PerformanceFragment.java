package com.ltm.runningtracker.android.activity.fragment;

import static android.app.Activity.RESULT_OK;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.BrowseRunDetailsActivity;
import com.ltm.runningtracker.android.activity.PerformanceActivity;
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import java.util.Objects;

public abstract class PerformanceFragment extends Fragment {

  public static final int BROWSE_RUN_REQUEST_CODE = 0;

  // Protected because must be accessed by other fragments
  protected final String[] RUN_DISPLAYED_DATA = new String[]{
      DroidProviderContract.ID,
      DroidProviderContract.DATE,
      DroidProviderContract.LOCATION,
      DroidProviderContract.TYPE,
      DroidProviderContract.PACE
  };

  protected final int[] COLUMNS_RESULT_IDS = new int[]{
      R.id.id,
      R.id.date,
      R.id.location,
      R.id.type,
      R.id.pace
  };

  protected SimpleCursorAdapter dataAdapter;
  protected ListView listView;
  protected PerformanceViewModel performanceViewModel;

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
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Intent intent = new Intent(getActivity(), BrowseRunDetailsActivity.class);
      Bundle bundle = new Bundle();

      // Get ID of run
      ViewGroup viewGroup = (ViewGroup) view1;
      TextView idView = viewGroup.findViewById(R.id.id);

      int runId = Integer.parseInt(idView.getText().toString());
      bundle.putInt("runId", runId);

      // Determine from which fragment so we can fetch the cursor from the right cache index
      bundle.putInt("fromFragment", PerformanceActivity.FRAGMENT_TO_ID.get(thisClass));

      intent.putExtras(bundle);
      startActivityForResult(intent, BROWSE_RUN_REQUEST_CODE);
    });

    return view;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BROWSE_RUN_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        AsyncTask.execute(() -> {
          // If no runs available, finish, because nothing more to browse
          if (!getRunRepository().doRunsExist(getActivity())) {
            Objects.requireNonNull(getActivity()).finish();
          } else {
            // Type was changed or run deleted, refresh cache and update UI
            onPopulateList();
          }
        });
      }
    }
  }

  protected abstract void onPopulateList();

}
