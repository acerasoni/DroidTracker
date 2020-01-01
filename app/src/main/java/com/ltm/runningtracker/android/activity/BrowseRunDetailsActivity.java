package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);

    spinner = findViewById(R.id.activityList);

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.activity_types, R.layout.spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);

    runId = getIntent().getIntExtra("runId",
        -1);
    int fromFragment = getIntent().getIntExtra("fromFragment", -1);

    // Check if cached has the row we need
    // Start by getting all runs associated with the weather
    Cursor c = getRunRepository().getRunsSync(WeatherClassifier.valueOf(fromFragment));
    if (c != null) {
      // Search for our ID in cursor
      c.moveToFirst();
      for (int x = 0; x < c.getCount(); x++) {
        if (c.getInt(0) == runId) {
          // Correct row has been found
          fetchDataSync(c, x);
          break;
        }
      }
    }

    // If cache is empty query DB asynchronously
    AsyncTask.execute(() -> fetchDataAsync());
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
    // Flush cache
    getRunRepository().flushCacheByWeather(weatherClassifier);

    // DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + runId);
    AsyncTask.execute(() -> {
      getContentResolver().delete(uri, null, null);

      // Refresh cache
      getRunRepository().getRunsAsync(this, weatherClassifier);
    });

    onSave(null);
  }


  public void onItemSelected(AdapterView<?> parent, View view,
      int pos, long id) {

    //UPDATE DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + runId);
    ContentValues contentValues = new ContentValues();
    String newType = capitalizeFirstLetter(RunTypeClassifier.valueOf(pos).toString());
    contentValues.put("type", newType);
    AsyncTask.execute(() -> getContentResolver().update(uri, contentValues, null, null));

    // Will cause listView in previous activity has to refresh its UI state
    hasTagBeenModified = true;
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
  }

  private void fetchDataSync(Cursor c, int id) {
    c.moveToPosition(id);
    updateUI(c);
  }

  @SuppressLint("DefaultLocale")
  private void fetchDataAsync() {
    Uri customUri = Uri.parse(RUNS_URI.toString() + "/" + runId);
    Cursor c = getContentResolver().query(customUri, null, null, null, null);

    if (c != null) {
      c.moveToFirst();
      updateUI(c);
    }

  }

  @SuppressLint({"SetTextI18n", "DefaultLocale"})
  public void updateUI(Cursor c) {
    runOnUiThread(() -> {
      StringBuilder sb;

      activityView = findViewById(R.id.activityView);
      activityView.setText("Run #" + runId);

      locationView = findViewById(R.id.locationView);
      locationView.setText(c.getString(1));

      dateView = findViewById(R.id.dateView);
      dateView.setText(c.getString(2));

      String runType = c.getString(3).toUpperCase();

      spinner.setSelection(RunTypeClassifier.valueOf(runType).getValue());

      sb = new StringBuilder(Integer.toString((int) c.getFloat(4))).append(" metres");
      distanceView = findViewById(R.id.distanceView);
      distanceView.setText(sb.toString());

      durationView = findViewById(R.id.durationView);
      durationView.setText(c.getString(5));

      weatherClassifier = WeatherClassifier.valueOf(c.getInt(6));
      weatherView = findViewById(R.id.weatherView);
      weatherView.setText(capitalizeFirstLetter(weatherClassifier.toString()));

      sb = new StringBuilder(String.format("%.2f", c.getFloat(7))).append("°C");
      temperatureView = findViewById(R.id.temperatureView);
      temperatureView.setText(sb.toString());

      float pace = c.getFloat(9);
      String paceString = String.format("%.2f", pace);
      sb = new StringBuilder(paceString).append(" km/h");
      paceView = findViewById(R.id.paceView);
      paceView.setText(sb.toString());
    });

  }

  public static String capitalizeFirstLetter(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }

}