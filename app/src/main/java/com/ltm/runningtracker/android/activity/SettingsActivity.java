package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Activity allows the user to complete the following operations:
 *
 * 1. Delete user & all runs associated 2. Delete all runs 3. Delete runs only by weather type
 */
public class SettingsActivity extends AppCompatActivity {

  private Map<Integer, Button> weatherClassifierToButton;

  // Views
  private Button userButton;
  private Button runsButton;
  private Button freezingButton;
  private Button coldButton;
  private Button mildButton;
  private Button warmButton;
  private Button hotButton;

  private ActivityViewModel settingsActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    initialiseViews();

    AsyncTask.execute(this::setupButtons);
  }

  private void setupButtons() {
    // If user exists, setup run buttons
    if (settingsActivityViewModel.doesUserExist()) {
      enableUserButton();
      enableRunButtonsIfAppropriate();
    } else {
      // Else disable all buttons
      disableAllButtons();
    }
  }

  private void enableUserButton() {
    userButton.setOnClickListener(v -> {
      userButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
      userButton.setOnClickListener(null);

      settingsActivityViewModel.deleteUser(this);
      settingsActivityViewModel.deleteRuns(this);

      // Observe user. When change occurs (cache is setup to null, aka deleted), finish
      settingsActivityViewModel.getUser().observe(this, user -> {
        setResult(RESULT_OK);
        finish();
      });
    });
  }

  // Only enable weather-specific button if runs of that type exist
  private void enableRunButtonsIfAppropriate() {
    boolean[] doRunsExist = new boolean[]{false, false, false, false, false};

    List<Run> runs = settingsActivityViewModel.getAllRuns(this);

    for (Run run : runs) {
      int weatherClassifier = run.weatherType;
      doRunsExist[weatherClassifier] = true;
    }

    if (runs.size() > 0) {
      enableRunsButton();
    } else {
      runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

    for (int i = 0; i < doRunsExist.length; i++) {
      if (doRunsExist[i]) {
        enableButton(weatherClassifierToButton.get(i),
            WeatherClassifier.valueOf(i),
            new StringBuilder("/").append(WeatherClassifier.valueOf(i)).toString());
      } else {
        disableButton(weatherClassifierToButton.get(i));
      }
    }

  }

  private void enableButton(Button button,
      @NonNull WeatherClassifier weatherClassifier,
      @NonNull String segment) {
    button.setOnClickListener(v -> {
      Uri uri = Uri
          .withAppendedPath(DroidProviderContract.RUNS_URI, segment);

      button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
      button.setOnClickListener(null);
      settingsActivityViewModel.deleteRunsByType(uri, this, weatherClassifier);
    });
  }

  private void disableButton(Button button) {
    button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
  }

  private void enableRunsButton() {
    runsButton.setOnClickListener(v -> {
      runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
      runsButton.setOnClickListener(null);

      // FLUSH CACHE
      getRunRepository().flushCache();

      AsyncTask.execute(() -> {
        // DELETE DB
        getContentResolver().delete(RUNS_URI, null, null);
        finish();
      });
    });
  }

  private void disableAllButtons() {
    userButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    freezingButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    coldButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    mildButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    warmButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    hotButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
  }

  private void initialiseViews() {
    settingsActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    userButton = findViewById(R.id.userButton);
    runsButton = findViewById(R.id.allRuns);
    freezingButton = findViewById(R.id.freezingRuns);
    coldButton = findViewById(R.id.coldRuns);
    mildButton = findViewById(R.id.mildRuns);
    warmButton = findViewById(R.id.warmRuns);
    hotButton = findViewById(R.id.hotRuns);

    weatherClassifierToButton = new HashMap<Integer, Button>() {
      {
        put(0, freezingButton);
        put(1, coldButton);
        put(2, mildButton);
        put(3, warmButton);
        put(4, hotButton);
      }
    };
  }

}