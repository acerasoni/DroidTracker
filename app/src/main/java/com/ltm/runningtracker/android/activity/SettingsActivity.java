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
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.fragment.FreezingPerformanceFragment;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import org.w3c.dom.DOMErrorHandler;

public class SettingsActivity extends AppCompatActivity {

  Button userButton, runsButton, freezingButton, coldButton, mildButton, warmButton, hotButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    initialiseViews();

    AsyncTask.execute(
        () -> setupButtons());
  }

  public void setupButtons() {
    // If user exists, setup run buttons
    if (getUserRepository().doesUserExist()) {
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
      AsyncTask.execute(() -> {
        getContentResolver().delete(USER_URI, null, null);
        getRunRepository().flushCache();
        getUserRepository().flushCache();
        setResult(RESULT_OK);
        finish();
      });

    });
  }

  private void enableRunButtonsIfAppropriate() {
    boolean doRunsExist, doFreezing, doCold, doMild, doWarm, doHot;

    doFreezing = getRunRepository().doRunsExistByWeather(this, WeatherClassifier.FREEZING);
    doCold = getRunRepository().doRunsExistByWeather(this, WeatherClassifier.COLD);
    doMild = getRunRepository().doRunsExistByWeather(this, WeatherClassifier.MILD);
    doWarm = getRunRepository().doRunsExistByWeather(this, WeatherClassifier.WARM);
    doHot = getRunRepository().doRunsExistByWeather(this, WeatherClassifier.HOT);

    doRunsExist = doFreezing || doCold || doMild || doWarm || doHot;
    if (doRunsExist) {
      enableRunsButton();
    } else {
      runsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
    if (doFreezing) {
      enableButton(freezingButton, WeatherClassifier.FREEZING, "/freezing");
    } else {
      freezingButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
    if (doCold) {
      enableButton(coldButton, WeatherClassifier.COLD, "/cold");
    } else {
      coldButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
    if (doMild) {
      enableButton(mildButton, WeatherClassifier.MILD, "/mild");
    } else {
      mildButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
    if (doWarm) {
      enableButton(warmButton, WeatherClassifier.WARM, "/warm");
    } else {
      warmButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
    if (doHot) {
      enableButton(hotButton, WeatherClassifier.HOT, "/hot");
    } else {
      hotButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

  }

  private void enableButton(Button button, @Nullable WeatherClassifier weatherClassifier,
      @Nullable String segment) {
    button.setOnClickListener(v -> {
      Uri uri = Uri
          .withAppendedPath(DroidProviderContract.RUNS_URI, segment);

      button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
      button.setOnClickListener(null);

      // Update cache
      getRunRepository().flushCacheByWeather(weatherClassifier);

      AsyncTask.execute(() -> {
        // Update DB
        getContentResolver().delete(uri, null, null);
        setupButtons();
      });
    });
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
        setupButtons();
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
