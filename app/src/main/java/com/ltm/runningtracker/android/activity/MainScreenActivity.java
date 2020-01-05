package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.util.Constants.APP_REQUIRES_ACCESS;
import static com.ltm.runningtracker.util.Constants.FETCHING_LOCATION;
import static com.ltm.runningtracker.util.Constants.FETCHING_TEMPERATURE;
import static com.ltm.runningtracker.util.Constants.PERMISSION_NOT_GRANTED;
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
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
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
 * It will prompt the user for FINE_ACCESS location permission if not previously granted. Then, it
 * handles the following scenarios and adjusts its UI accordingly:
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
 *
 * Once all activities unbind from the location service, this will be killed from the OS. However,
 * this only occurs if the user is not on a run, in which case it will keep running as a foreground
 * service.
 */
public class MainScreenActivity extends AppCompatActivity {

  public static final int USER_CREATION_REQUEST = 1;
  public static final int USER_MODIFICATION_REQUEST = 2;
  public static final int SETTINGS_MODIFICATION_REQUEST = 3;
  public static final int RUN_ACTIVITY_REQUEST = 4;

  private TextView weatherTextField;
  private TextView locationTextField;
  private Button runButton;
  private Button performanceButton;
  private Button userProfileButton;
  private Button settingsButton;

  private ServiceConnection serviceConnection;
  private Context context = this;
  private ActivityViewModel mainActivityViewModel;

  private LocationServiceBinder locationServiceBinder;
  private boolean mBound;
  private boolean isLocationAndWeatherAvailable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);

    initialiseViews();
    requestPermission();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mBound) {
      unbindService(serviceConnection);
    }
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
          if (!locationServiceBinder.userDeleted()) {
            Toast.makeText(this, USER_DELETED, Toast.LENGTH_LONG).show();
          }
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

  // Recompute UI state following onCreate event and when returning from an Activity
  @Override
  public void onStart() {
    super.onStart();
    setupButtons();
    setupPerformance();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NotNull String[] permissions, @NotNull int[] grantResults) {
    if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
      Toast.makeText(this, APP_REQUIRES_ACCESS, Toast.LENGTH_LONG).show();
    } else {
      setup();
    }
  }

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

  private void setup() {
    // Initialise repositories
    mainActivityViewModel.initRepos();

    // Start weather and location service
    // Acts as a callback
    Intent locationIntent = new Intent(this, LocationService.class);
    startService(locationIntent);
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mBound = true;
        locationServiceBinder = (LocationServiceBinder) service;
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        mBound = false;
        locationServiceBinder = null;
      }
    };

    bindService(locationIntent, serviceConnection, Context.BIND_AUTO_CREATE);

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

}
