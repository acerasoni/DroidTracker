package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.util.Constants.APP_REQUIRES_ACCESS;
import static com.ltm.runningtracker.util.Constants.FETCHING_LOCATION;
import static com.ltm.runningtracker.util.Constants.FETCHING_TEMPERATURE;
import static com.ltm.runningtracker.util.Constants.RUN_FIRST;
import static com.ltm.runningtracker.util.Constants.SETUP_REQUIRED;
import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;
import static com.ltm.runningtracker.util.Constants.USER_CREATED;
import static com.ltm.runningtracker.util.Constants.USER_DELETED;

import android.Manifest.permission;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.android.service.LocationService;
import com.ltm.runningtracker.android.service.LocationService.LocationServiceBinder;
import org.jetbrains.annotations.NotNull;

/**
 * This Activity presents the user with four buttons which allow him to navigate to the respective
 * Run, Performance, User Profile and Settings Activities.
 *
 * It will prompt the user for FINE_ACCESS location permission if not previously granted. If denied,
 * it will request the user to relaunch the app.
 *
 * Then, it handles the following scenarios and adjusts its UI accordingly:
 *
 * - If no user has been setup, Run and Performance tabs are disabled - If user is setup, but no
 * runs exist, disable the Performance tab
 *
 * Additionally, when returning from an activity which modifies a record in the database, it will
 * recompute its UI logic according to the above scenarios. This is because the user could have been
 * deleted, or all runs could have been erased.
 *
 * It will start the Location service, which in turn starts the Weather service, and binds to them.
 * However, it does not utilise the binder to retrieve data; rather, it observes the LiveData
 * objects modified by the service itself.
 */
public class MainScreenActivity extends AppCompatActivity {

  public static final int USER_CREATION_REQUEST = 1;
  public static final int USER_MODIFICATION_REQUEST = 2;
  public static final int SETTINGS_MODIFICATION_REQUEST = 3;
  public static final int RUN_ACTIVITY_REQUEST = 4;
  public static final int TIME_CHANGED_RESULT_CODE = 5;

  // Views
  private TextView weatherTextField;
  private TextView locationTextField;
  private Button runButton;
  private Button performanceButton;
  private Button userProfileButton;
  private Button settingsButton;

  private Intent locationIntent;
  private ServiceConnection serviceConnection;
  private Context context = this;
  private ActivityViewModel mainActivityViewModel;

  private LocationServiceBinder locationServiceBinder;
  private boolean mBound;
  private boolean isLocationAndWeatherAvailable;
  private boolean requiresLocationRestart;
  private boolean requiresUserDeleted;

  @RequiresApi(api = VERSION_CODES.O)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    locationIntent = new Intent(this, LocationService.class);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);
    requiresLocationRestart = false;
    requiresUserDeleted = false;

    initialiseViews();
    requestPermission();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putBoolean("deviceRotated", true);
    super.onSaveInstanceState(outState);
  }

  /*
  Necessary to check if onCreate is upon rotation so that we can start the location service again
  Note: the services are killed when no activity is bound and no run is ongoing for resource efficiency
   */
  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    if (savedInstanceState.getBoolean("deviceRotated")) {
      startService(locationIntent);
      bindService(locationIntent, serviceConnection, 0);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case USER_CREATION_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          Toast.makeText(this, USER_CREATED, Toast.LENGTH_LONG).show();
        }
        break;
      case SETTINGS_MODIFICATION_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          requiresUserDeleted = true;
        } else if (resultCode == TIME_CHANGED_RESULT_CODE) {
          requiresLocationRestart = true;
        }
        break;
      case RUN_ACTIVITY_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          enablePerformance();
        }
        break;
      default:
        throw new IllegalStateException(
            UNEXPECTED_VALUE + requestCode);
    }
  }

  /**
   * Recompute UI state following onCreate event and when returning from an Activity.
   */
  @Override
  public void onStart() {
    bindService(locationIntent, serviceConnection, 0);
    setupButtons();
    setupPerformance();
    super.onStart();
  }

  @Override
  public void onStop() {
    if (mBound) {
      unbindService(serviceConnection);
      mBound = false;
    }
    super.onStop();
  }

  @RequiresApi(api = VERSION_CODES.O)
  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NotNull String[] permissions, @NotNull int[] grantResults) {
    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
      Toast.makeText(this, APP_REQUIRES_ACCESS, Toast.LENGTH_LONG).show();
    } else {
      setup();
    }
  }

  @RequiresApi(api = VERSION_CODES.O)
  private void requestPermission() {
    if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{permission.ACCESS_FINE_LOCATION},
          0);
    } else {
      setup();
    }
  }

  private void disableButtons() {
    View.OnClickListener sharedListener = v -> {
      Toast.makeText(getApplicationContext(), SETUP_REQUIRED,
          Toast.LENGTH_LONG).show();
    };
    runButton.setOnClickListener(sharedListener);
    performanceButton.setOnClickListener(sharedListener);
    // If user has not been setup before, we request activity for result
    userProfileButton.setOnClickListener(
        v -> startActivityForResult(new Intent(context, UserProfileActivity.class),
            USER_CREATION_REQUEST));

    runButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    performanceButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
  }

  private void enableButtons() {
    setupRun();
    setupPerformance();

    userProfileButton
        .setOnClickListener(v -> {
          Intent intent = new Intent(context, UserProfileActivity.class);
          intent.putExtra(getResources().getString(R.string.request), USER_MODIFICATION_REQUEST);
          startActivity(intent);
        });
  }

  private void setupRun() {
    if (isLocationAndWeatherAvailable) {
      enableRun();
    } else {
      View.OnClickListener runListener = v -> Toast
          .makeText(getApplicationContext(), FETCHING_LOCATION,
              Toast.LENGTH_LONG).show();
      runButton.setOnClickListener(runListener);
      performanceButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
  }

  private void setupPerformance() {
    AsyncTask.execute(() -> {
      if (mainActivityViewModel.doRunsExist(this)) {
        enablePerformance();
      } else {
        View.OnClickListener runListener = v -> Toast
            .makeText(getApplicationContext(), RUN_FIRST,
                Toast.LENGTH_LONG).show();
        performanceButton.setOnClickListener(runListener);
        performanceButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
      }
    });
  }

  private void enableRun() {
    runButton.getBackground().clearColorFilter();
    runButton.setOnClickListener(
        v -> startActivityForResult(new Intent(context, RunActivity.class), RUN_ACTIVITY_REQUEST));
  }

  private void enablePerformance() {
    performanceButton.getBackground().clearColorFilter();
    performanceButton.setOnClickListener(
        v -> startActivity(new Intent(context, PerformanceActivity.class)));
  }

  private void initialiseViews() {
    mainActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    runButton = findViewById(R.id.runButton);
    performanceButton = findViewById(R.id.performanceButton);
    settingsButton = findViewById(R.id.settingsButton);
    userProfileButton = findViewById(R.id.userProfileButton);
    weatherTextField = findViewById(R.id.weatherField);
    locationTextField = findViewById(R.id.locationField);

    weatherTextField.setText(FETCHING_TEMPERATURE);
    locationTextField.setText(FETCHING_LOCATION);

    settingsButton.setOnClickListener(v -> {
      startActivityForResult(new Intent(context, SettingsActivity.class),
          SETTINGS_MODIFICATION_REQUEST);
    });
  }

  private void setupButtons() {
    // If user does not exist in cache
    if (!mainActivityViewModel.doesUserExist()) {
      disableButtons();

      //If it is fetched at a later stage from DB, enable
      mainActivityViewModel.getUser().observe(this, user -> {
        if (user != null) {
          enableButtons();
        } else {
          disableButtons();
        }
      });
    } else {
      enableButtons();
    }
  }

  @RequiresApi(api = VERSION_CODES.O)
  private void setup() {
    // Initialise repositories
    mainActivityViewModel.initRepos();

    // Instantiate service connection
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mBound = true;
        locationServiceBinder = (LocationServiceBinder) service;

        if (requiresLocationRestart) {
          locationServiceBinder.restartLocationTracking();
          requiresLocationRestart = false;
        } else if (requiresUserDeleted) {
          if (!locationServiceBinder.userDeleted()) {
            Toast.makeText(context, USER_DELETED, Toast.LENGTH_LONG).show();
          }
          requiresUserDeleted = false;
        }
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        locationServiceBinder = null;
      }
    };

    /*
    Start location service

    We want to start and bind to the service here rather than in onStart() because we need
    to wait for the user to grant permission to FINE_ACCESS_LOCATION.

    There is no reason why we would start the service if the location cannot be retrieved.
    This would be a waste of resources.

    We keep the activity bound to the service even when it is stopped to avoid the service dying.
    RunActivity also uses the same service, so no waste of resources occurs.
    */
    startService(locationIntent);
    // No need for BIND_AUTO_CREATE, as we started the service explicitly already
    bindService(locationIntent, serviceConnection, 0);

    // Observe location object
    mainActivityViewModel.getCounty().observe(this, county -> locationTextField.setText(county));

    // Observe temperature object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      StringBuilder sb = new StringBuilder(Float.toString(weather.temperature.getTemp()))
          .append(" ").append(getResources().getString(R.string.degrees_celsius));
      weatherTextField.setText(sb.toString());
      // Location and weather now available
      isLocationAndWeatherAvailable = true;
      if (mainActivityViewModel.doesUserExist()) {
        enableRun();
      }
    });
  }

  /**
   * This utility method setups up the toolbar's action button
   *
   * @param id of the given toolbar
   * @see <a href="https://developer.android.com/training/appbar/up-action#declare-parent">Android
   * Documentation on setting up toolbars</a>
   */
  public static void setupToolbar(AppCompatActivity activity, int id) {
    // my_child_toolbar is defined in the layout file
    Toolbar myChildToolbar = activity.findViewById(id);
    activity.setSupportActionBar(myChildToolbar);

    // Get a support ActionBar corresponding to this toolbar
    ActionBar ab = activity.getSupportActionBar();

    // Enable the Up button
    ab.setDisplayHomeAsUpEnabled(true);
    ab.setDisplayShowTitleEnabled(false);
  }

}