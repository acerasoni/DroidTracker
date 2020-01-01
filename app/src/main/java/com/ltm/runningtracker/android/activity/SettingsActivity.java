package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;

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
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

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
    settingsActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    AsyncTask.execute(this::setupButtons);
  }

  public void setupButtons() {
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

      // Observe user. When change occurs (aka it gets deleted), finish
      settingsActivityViewModel.getUser().observe(this, user -> {
        setResult(RESULT_OK);
        finish();
      });
    });
  }


  private void enableRunButtonsIfAppropriate() {
    Boolean doRunsExist = false;
    Boolean doFreezing = false;
    Boolean doCold = false;
    Boolean doMild = false;
    Boolean doWarm = false;
    Boolean doHot = false;

    settingsActivityViewModel
        .determineIfRunsExist(this, doRunsExist, doFreezing, doCold, doMild, doWarm, doHot);

    if (doRunsExist) {
      enableRunsButton();
    } else {
      runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

    enableButton(doFreezing, freezingButton, WeatherClassifier.FREEZING, "/freezing");
    enableButton(doCold, coldButton, WeatherClassifier.COLD, "/cold");
    enableButton(doMild, mildButton, WeatherClassifier.MILD, "/mild");
    enableButton(doWarm, warmButton, WeatherClassifier.WARM, "/warm");
    enableButton(doHot, hotButton, WeatherClassifier.HOT, "/hot");
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
        settingsActivityViewModel.deleteAllRunsByType(uri, this, weatherClassifier);
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
    userButton = findViewById(R.id.userButton);
    runsButton = findViewById(R.id.allRuns);
    freezingButton = findViewById(R.id.freezingRuns);
    coldButton = findViewById(R.id.coldRuns);
    mildButton = findViewById(R.id.mildRuns);
    warmButton = findViewById(R.id.warmRuns);
    hotButton = findViewById(R.id.hotRuns);
  }


}
