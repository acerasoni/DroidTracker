package com.ltm.runningtracker.android.fragment;

import static android.app.Activity.RESULT_OK;
import static com.ltm.runningtracker.android.activity.PerformanceActivity.FRAGMENT_TO_ID;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.BrowseRunDetailsActivity;
import com.ltm.runningtracker.android.activity.PerformanceActivity;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.util.SimpleRecyclerViewAdapter;
import com.ltm.runningtracker.util.SimpleRecyclerViewAdapter.ItemClickListener;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import java.util.List;
import java.util.Objects;

/**
 * Abstract Performance fragment extended by weather-specific fragments. Behaviour between all child
 * fragments is shared, as they all contain a ListView formatted in the same way. The only different
 * is the input Cursor utilised to populate it.
 *
 * @see package com.ltm.runningtracker.android.fragment.impl
 */
public abstract class PerformanceFragment extends Fragment {

  public static final int BROWSE_RUN_REQUEST_CODE = 0;

  private RecyclerView recyclerView;
  private SimpleRecyclerViewAdapter adapter;
  private ActivityViewModel performanceViewModel;
  private List<Run> runsByWeather;
  private Class thisClass = this.getClass();

  // Set in child fragments. Needs to be protected in order to be accessed in child class
  protected WeatherClassifier weatherClassifierOfFragment;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    performanceViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);
  }

  /**
   * Inflating the fragment's view and updating its UI state is more appropriate in onCreateView
   * rather than onCreate. This is because, due to Android's Fragment's lifecycle, onCreateView is
   * always called after onCreate but also after onDestroyView.
   *
   * @return inflated View
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.performance_fragment, container, false);
    recyclerView = view.findViewById(R.id.runList);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    AsyncTask.execute(() -> {
      onPopulateList(weatherClassifierOfFragment);
      adapter = new SimpleRecyclerViewAdapter(getActivity(), runsByWeather);
      adapter.setClickListener(new RunItemClickListener());
      getActivity().runOnUiThread(() -> recyclerView.setAdapter(adapter));
    });

    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == BROWSE_RUN_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        AsyncTask.execute(() -> {
          // If no runs available, finish, because nothing more to browse
          if (!performanceViewModel.doRunsExist(getActivity())) {
            Objects.requireNonNull(getActivity()).finish();
          } else {
            // Type was changed or run deleted, refresh cache and update UI
            onUpdateList(weatherClassifierOfFragment);
          }
        });
      }
    }
  }

  protected void onPopulateList(WeatherClassifier weatherClassifier) {
    runsByWeather = performanceViewModel
        .getRunsByWeather(weatherClassifier, Objects.requireNonNull(getContext()));
  }

  protected void onUpdateList(WeatherClassifier weatherClassifier) {
      runsByWeather = performanceViewModel
          .getRunsByWeather(weatherClassifier, Objects.requireNonNull(getContext()));
    Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
      adapter.swapRuns(runsByWeather);
    });
  }

  private class RunItemClickListener implements SimpleRecyclerViewAdapter.ItemClickListener {

    @Override
    public void onItemClick(View view, int position) {
      // Start details activity when item is clicked
      // recyclerView.s((parent, view1, position, id) -> {
      Intent intent = new Intent(getActivity(), BrowseRunDetailsActivity.class);
      Bundle bundle = new Bundle();

      // Get ID of run
      ViewGroup viewGroup = (ViewGroup) view;
      TextView idView = viewGroup.findViewById(R.id.id);

      int runId = Integer.parseInt(idView.getText().toString());
      bundle.putInt(getResources().getString(R.string.run_id), runId);

      // Determine from which fragment so we can fetch the cursor from the right cache index
      bundle.putInt(getResources().getString(R.string.from_fragment),
          FRAGMENT_TO_ID.get(thisClass));

      intent.putExtras(bundle);
      startActivityForResult(intent, BROWSE_RUN_REQUEST_CODE);
    }
  }

}
