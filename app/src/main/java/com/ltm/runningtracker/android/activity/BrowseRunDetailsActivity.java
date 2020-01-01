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
import com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

/**
 * The purpose of this activity is to browse details relating to a specific run, and tag the
 * associated diet Details shown are start location, end location ... //TODO finish
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
  private boolean hasTagBeenModified = false;
  private int runId;
  private WeatherClassifier weatherClassifier;
  private ActivityViewModel browseDetailsActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);
    initialiseViews();

    runId = getIntent().getIntExtra("runId",
        -1);
    int fromFragment = getIntent().getIntExtra("fromFragment", -1);
    WeatherClassifier wc = WeatherClassifier.valueOf(fromFragment);

    browseDetailsActivityViewModel.getShortLivingCache().observe(this, cursor -> {
      if (cursor != null) {
        updateUI(cursor);
      }
      browseDetailsActivityViewModel.getRunCursorByWeather(wc).removeObservers(this);
    });

    browseDetailsActivityViewModel
        .requestRunById(runId, wc, this);
  }

  public void onSave(@Nullable View v) {
    if (hasTagBeenModified) {
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }

    finish();
  }

  public void onDelete(View v) {
    browseDetailsActivityViewModel.onDelete(weatherClassifier, this, runId);
    onSave(null);
  }


  public void onItemSelected(AdapterView<?> parent, View view,
      int pos, long id) {

    browseDetailsActivityViewModel.updateTypeOfRun(runId, pos, this);
    // Will cause listView in previous activity has to refresh its UI state
    hasTagBeenModified = true;
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
  }

  /**
   * Expected cursor to be in correct position
   */
  @SuppressLint({"SetTextI18n", "DefaultLocale"})
  public void updateUI(Cursor c) {
    runOnUiThread(() -> {
      StringBuilder sb;

      activityView.setText("Run #" + runId);
      locationView.setText(c.getString(1));
      dateView.setText(c.getString(2));

      String runType = c.getString(3).toUpperCase();
      spinner.setSelection(RunTypeClassifier.valueOf(runType).getValue());

      sb = new StringBuilder(Integer.toString((int) c.getFloat(4))).append(" metres");
      distanceView.setText(sb.toString());
      durationView.setText(c.getString(5));

      weatherClassifier = WeatherClassifier.valueOf(c.getInt(6));
      weatherView.setText(ActivityViewModel.capitalizeFirstLetter(weatherClassifier.toString()));

      sb = new StringBuilder(String.format("%.2f", c.getFloat(7))).append("°C");
      temperatureView.setText(sb.toString());

      float pace = c.getFloat(9);
      String paceString = String.format("%.2f", pace);
      sb = new StringBuilder(paceString).append(" km/h");
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