package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.util.Constants.COLD_STRING;
import static com.ltm.runningtracker.util.Constants.FREEZING_STRING;
import static com.ltm.runningtracker.util.Constants.HOT_STRING;
import static com.ltm.runningtracker.util.Constants.MILD_STRING;
import static com.ltm.runningtracker.util.Constants.WARM_STRING;

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
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class SettingsActivity extends AppCompatActivity {

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

      // Observe user. When change occurs (aka it gets deleted), finish
      settingsActivityViewModel.getUser().observe(this, user -> {
        setResult(RESULT_OK);
        finish();
      });
    });
  }

  private void enableRunButtonsIfAppropriate() {
    boolean[] runsExist = settingsActivityViewModel
        .determineWhichRunTypesExist(this);
    boolean doRunsExist = runsExist[5];

    boolean doFreezing = runsExist[0];
    boolean doCold = runsExist[1];
    boolean doMild = runsExist[2];
    boolean doWarm = runsExist[3];
    boolean doHot = runsExist[4];

    if (doRunsExist) {
      enableRunsButton();
    } else {
      runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

    enableButton(doFreezing, freezingButton, WeatherClassifier.FREEZING, new StringBuilder("/").append(FREEZING_STRING).toString());
    enableButton(doCold, coldButton, WeatherClassifier.COLD, new StringBuilder("/").append(COLD_STRING).toString());
    enableButton(doMild, mildButton, WeatherClassifier.MILD, new StringBuilder("/").append(MILD_STRING).toString());
    enableButton(doWarm, warmButton, WeatherClassifier.WARM, new StringBuilder("/").append(WARM_STRING).toString());
    enableButton(doHot, hotButton, WeatherClassifier.HOT, new StringBuilder("/").append(HOT_STRING).toString());
  }

  private void enableButton(boolean doRun, Button button,
      @NonNull WeatherClassifier weatherClassifier,
      @NonNull String segment) {
    if (doRun) {
      button.setOnClickListener(v -> {
        Uri uri = Uri
            .withAppendedPath(DroidProviderContract.RUNS_URI, segment);

        button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        button.setOnClickListener(null);
        settingsActivityViewModel.deleteRunsByType(uri, this, weatherClassifier);
      });
    } else {
      button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

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
  }

}
