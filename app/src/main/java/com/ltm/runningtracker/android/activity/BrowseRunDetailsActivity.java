package com.ltm.runningtracker.android.activity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

/**
 * This activity allows the user to browse details relating to a specific run. He can then do one of
 * the following:
 *
 * 1. Tag the run as a specific type (note that newly completed runs are 'untagged' by default) 2.
 * Modify the run's tag to another run type 3. Delete the run altogether
 */
public class BrowseRunDetailsActivity extends AppCompatActivity implements OnItemSelectedListener {

  // Views
  private TextView activityView;
  private TextView locationView;
  private TextView dateView;
  private TextView weatherView;
  private TextView temperatureView;
  private TextView distanceView;
  private TextView durationView;
  private TextView paceView;
  private Spinner spinner;

  // Internal logic
  private boolean hasTagBeenModified;
  private int runId;
  private int newRunType;
  private WeatherClassifier weatherClassifier;
  private ActivityViewModel browseDetailsActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);
    initialiseViews();

    hasTagBeenModified = false;
    runId = getIntent().getIntExtra(getResources().getString(R.string.run_id),
        -1);
    int fromFragment = getIntent()
        .getIntExtra(getResources().getString(R.string.from_fragment), -1);
    WeatherClassifier wc = WeatherClassifier.valueOf(fromFragment);

    // If the synchronous inspection of cache fails, make another effort to observe short living cache
    // populated by the asynchronous database call
    browseDetailsActivityViewModel.getShortLivingCache().observe(this, cursor -> {
      if (cursor != null) {
        updateUI(cursor);
      }
      browseDetailsActivityViewModel.getRunCursorByWeather(wc).removeObservers(this);
    });

    browseDetailsActivityViewModel
        .getRunById(runId, wc, this);
  }

  public void onSave(@Nullable View v) {
    if (hasTagBeenModified) {
      browseDetailsActivityViewModel.updateTypeOfRun(runId, newRunType, this);
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }

    finish();
  }

  public void onDelete(View v) {
    browseDetailsActivityViewModel.deleteRun(weatherClassifier, this, runId);
    onSave(null);
  }

  // Switch between tabs
  public void onItemSelected(AdapterView<?> parent, View view,
      int pos, long id) {
    newRunType = pos;
    // Will cause listView in previous activity has to refresh its UI state
    hasTagBeenModified = true;
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
  }

  /**
   * This method populates the UI with the details from the run in the Cursor.
   *
   * @param cursor already moved to the correct row position
   */
  @SuppressLint({"SetTextI18n", "DefaultLocale"})
  public void updateUI(Cursor cursor) {
    runOnUiThread(() -> {
      StringBuilder sb;

      sb = new StringBuilder(getResources().getString(R.string.run)).append(" #").append(runId);
      activityView.setText(sb.toString());
      locationView.setText(cursor.getString(1));
      dateView.setText(cursor.getString(2));

      String runType = cursor.getString(3).toUpperCase();
      spinner.setSelection(RunTypeClassifier.valueOf(runType).getValue());

      sb = new StringBuilder(Integer.toString((int) cursor.getFloat(4))).append(" ")
          .append(getResources().getString(R.string.metres));
      distanceView.setText(sb.toString());
      durationView.setText(cursor.getString(5));

      weatherClassifier = WeatherClassifier.valueOf(cursor.getInt(6));
      weatherView.setText(ActivityViewModel.capitalizeFirstLetter(weatherClassifier.toString()));

      sb = new StringBuilder(
          String.format(getResources().getString(R.string.two_decimals_format), cursor.getFloat(7)))
          .append(getResources().getString(R.string.degrees_celsius));
      temperatureView.setText(sb.toString());

      float pace = cursor.getFloat(9);
      String paceString = String
          .format(getResources().getString(R.string.two_decimals_format), pace);
      sb = new StringBuilder(paceString).append(" ")
          .append(getResources().getString(R.string.kilometers_per_hour));
      paceView.setText(sb.toString());
    });

  }

  private void initialiseViews() {
    activityView = findViewById(R.id.activityView);
    locationView = findViewById(R.id.locationView);
    dateView = findViewById(R.id.dateView);
    distanceView = findViewById(R.id.distanceView);
    durationView = findViewById(R.id.durationView);
    weatherView = findViewById(R.id.weatherView);
    temperatureView = findViewById(R.id.temperatureView);
    paceView = findViewById(R.id.paceView);

    spinner = findViewById(R.id.activityList);
    browseDetailsActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.activity_types, R.layout.spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
  }

}