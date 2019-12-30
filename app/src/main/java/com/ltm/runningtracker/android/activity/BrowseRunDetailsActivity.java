package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

/**
 * The purpose of this activity is to browse details relating to a specific run, and tag the
 * associated diet Details shown are start location, end location ... //TODO finish
 */
public class BrowseRunDetailsActivity extends AppCompatActivity {

  private TextView activityView, locationView, dateView, typeView,
      weatherView, temperatureView, distanceView, durationView, paceView;

  private int runId;
  private int fromFragment;
  private boolean isSet;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);

    runId = getIntent().getIntExtra("runId",
        -1);
    fromFragment = getIntent().getIntExtra("fromFragment", -1);

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

    // If for some reason cache is empty query DB asynchronously
    AsyncTask.execute(() -> fetchDataAsync());
  }

  private void fetchDataSync(Cursor c, int id) {
    isSet = true;

    c.moveToPosition(id);
    updateUI(c);
  }

  @SuppressLint("DefaultLocale")
  private void fetchDataAsync() {
    Uri customUri = Uri.parse(RUNS_URI.toString() + "/" + runId);
    Cursor c = getContentResolver().query(customUri, null, null, null, null);

    c.moveToFirst();
    updateUI(c);
  }

  public void updateUI(Cursor c) {
    runOnUiThread(() -> {
      StringBuilder sb;

      activityView = findViewById(R.id.activityView);
      activityView.setText("Activity #" + runId);

      locationView = findViewById(R.id.locationView);
      locationView.setText(c.getString(1));

      dateView = findViewById(R.id.dateView);
      dateView.setText(c.getString(2));

      typeView = findViewById(R.id.typeView);
      typeView.setText(capitalizeFirstLetter(RunTypeClassifier.valueOf(c.getInt(5)).toString()));

      sb = new StringBuilder(String.format("%.2f", c.getDouble(3))).append(" metres");
      distanceView = findViewById(R.id.distanceView);
      distanceView.setText(sb.toString());

      durationView = findViewById(R.id.durationView);
      durationView.setText(c.getString(4));

      weatherView = findViewById(R.id.weatherView);
      weatherView.setText(capitalizeFirstLetter(WeatherClassifier.valueOf(c.getInt(6)).toString()));

      sb = new StringBuilder(String.format("%.2f", c.getFloat(7))).append("°C");
      temperatureView = findViewById(R.id.temperatureView);
      temperatureView.setText(sb.toString());

      sb = new StringBuilder(String.format("%.2f", c.getFloat(9))).append(" km/h");
      paceView = findViewById(R.id.paceView);
      paceView.setText(sb.toString());
    });

  }

  private String capitalizeFirstLetter(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }

}
