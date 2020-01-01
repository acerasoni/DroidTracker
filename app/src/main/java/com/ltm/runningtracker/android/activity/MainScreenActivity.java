package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

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
import org.jetbrains.annotations.NotNull;

public class MainScreenActivity extends AppCompatActivity {

  public static final int USER_CREATION_REQUEST = 1;
  public static final int USER_MODIFICATION_REQUEST = 2;
  public static final int SETTINGS_MODIFICATION_REQUEST = 3;
  public static final int RUN_ACTIVITY_REQUEST = 4;

  private TextView weatherTextField, locationTextField;
  private Button runButton;
  private Button performanceButton;
  private Button userProfileButton;

  private ServiceConnection serviceConnection;
  private Context context = this;
  private ActivityViewModel mainActivityViewModel;

  private boolean mBound;
  private boolean isLocationAndWeatherAvailable;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);
    mainActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    initialiseViews();
    requestPermission();
  }


  private void setup() {
    // Initialise repositories
    getPropertyManager();
    getLocationRepository();
    getUserRepository();
    getWeatherRepository();
    getRunRepository();

    // Start weather and location service
    // No need to bind because no communication is required - just observing objects
    // Acts as a callback
    Intent locationIntent = new Intent(this, LocationService.class);
    startService(locationIntent);
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mBound = true;
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        mBound = false;
      }
    };

    bindService(locationIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    // observe location object
    mainActivityViewModel.getCounty().observe(this, county -> locationTextField.setText(county));

    // observe temperature object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      weatherTextField.setText(weather.temperature.getTemp() + " °C");
      // Location and weather now available
      isLocationAndWeatherAvailable = true;
      if (getUserRepository().doesUserExist()) {
        enableRun();
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mBound) {
      unbindService(serviceConnection);
    }
  }

  private void setupButtons() {
    // If user does not exist in cache
    if (!mainActivityViewModel.doesUserExist()) {
      disableButtons();

      //If it is fetched at a later stage from DB, enable
      mainActivityViewModel.getUser().observe(this, user -> {
        if(user != null) enableButtons();
        else disableButtons();
      });
    } else {
      enableButtons();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case USER_CREATION_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          Toast.makeText(this, "User created", Toast.LENGTH_LONG).show();
        }
        break;
      case SETTINGS_MODIFICATION_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          Toast.makeText(this, "User deleted", Toast.LENGTH_LONG).show();
        }
        break;
      case RUN_ACTIVITY_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          enablePerformance();
        }
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + requestCode);
    }

  }

  @Override
  public void onStart() {
    super.onStart();
    setupButtons();
    setupPerformance();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      @NotNull String[] permissions, @NotNull int[] grantResults) {
    setup();
  }

  private void disableButtons() {
    View.OnClickListener sharedListener = v -> {
      Toast.makeText(getApplicationContext(), "User setup required",
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
          intent.putExtra("request", USER_MODIFICATION_REQUEST);
          startActivity(intent);
        });
  }

  private void setupRun() {
    if (isLocationAndWeatherAvailable) {
      enableRun();
    } else {
      View.OnClickListener runListener = v -> Toast
          .makeText(getApplicationContext(), "Please wait - fetching location",
              Toast.LENGTH_LONG).show();
      runButton.setOnClickListener(runListener);
      performanceButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }
  }

  private void setupPerformance() {
    AsyncTask.execute(() -> {
      if (getRunRepository().doRunsExist(this)) {
        enablePerformance();
      } else {
        View.OnClickListener runListener = v -> Toast
            .makeText(getApplicationContext(), "Go for a run first",
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
    runButton = findViewById(R.id.runButton);
    performanceButton = findViewById(R.id.performanceButton);
    Button settingsButton = findViewById(R.id.settingsButton);
    userProfileButton = findViewById(R.id.userProfileButton);
    weatherTextField = findViewById(R.id.weatherField);
    locationTextField = findViewById(R.id.locationField);

    weatherTextField.setText("Fetching temperature...");
    locationTextField.setText("Fetching location...");
    settingsButton.setOnClickListener(v -> {
      startActivityForResult(new Intent(context, SettingsActivity.class),
          SETTINGS_MODIFICATION_REQUEST);
    });
  }

  public void requestPermission() {
    if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{permission.ACCESS_FINE_LOCATION},
          0);
    } else {
      setup();
    }
  }

}
