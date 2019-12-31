package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import com.ltm.runningtracker.android.activity.viewmodel.MainScreenActivityViewModel;
import com.ltm.runningtracker.android.service.LocationService;
import com.ltm.runningtracker.android.service.WeatherService;
import com.ltm.runningtracker.repository.LocationRepository;

public class MainScreenActivity extends AppCompatActivity {

  public static final int USER_CREATION_REQUEST = 1;
  public static final int USER_MODIFICATION_REQUEST = 2;

  private Context context = this;
  private MainScreenActivityViewModel mainActivityViewModel;
  private TextView weatherTextField, locationTextField;
  private Button runButton, performanceButton, settingsButton, userProfileButton;
  private boolean isLocationAndWeatherAvailable;
  private boolean isRunEnabled = false;
  private ServiceConnection serviceConnection;
  private boolean mBound;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);
    mainActivityViewModel = ViewModelProviders.of(this).get(MainScreenActivityViewModel.class);

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

    setupButtons();

    // observe location object
    mainActivityViewModel.getCounty().observe(this, county -> {
      locationTextField.setText(county);
    });

    // observe temperature object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      weatherTextField.setText(weather.temperature.getTemp() + " °C");
      // Location and weather now available
      isLocationAndWeatherAvailable = true;
      if (getUserRepository().doesUserExist()) {
        enableRun();
        isRunEnabled = true;
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
    if (!mainActivityViewModel.doesUserExist()) {
      mainActivityViewModel.getUser().observe(this, user -> {
        enableButtons();
      });
      // If user does not exist, observe the object (as the database is pinged asynchronously)
      // In other words, disable buttons temporarily, but re-enable them if user is ever fetched from the db
      disableButtons();
    } else {
      enableButtons();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1) {
      if (resultCode == Activity.RESULT_OK) {
        enableButtons();
      }
      if (resultCode == Activity.RESULT_CANCELED) {
        disableButtons();
      }
    }
  }//onActivityResult

  @Override
  public void onRequestPermissionsResult(int requestCode,
      String[] permissions, int[] grantResults) {
    setup();
  }

  private void disableButtons() {
    View.OnClickListener sharedListener = v -> {
      Toast.makeText(getApplicationContext(), "User setup required",
          Toast.LENGTH_LONG).show();
    };
    runButton.setOnClickListener(sharedListener);
    performanceButton.setOnClickListener(sharedListener);
    settingsButton.setOnClickListener(sharedListener);
    // If user has not been setup before, we request activity for result
    userProfileButton.setOnClickListener(
        v -> startActivityForResult(new Intent(context, UserProfileActivity.class),
            USER_CREATION_REQUEST));

    runButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    performanceButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    settingsButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
  }

  private void enableButtons() {
    if (isLocationAndWeatherAvailable) {
      enableRun();
      isRunEnabled = true;
    } else {
      View.OnClickListener runListener = v -> Toast
          .makeText(getApplicationContext(), "Please wait - fetching location",
              Toast.LENGTH_LONG).show();
      runButton.setOnClickListener(runListener);
    }

    performanceButton.getBackground().clearColorFilter();
    settingsButton.getBackground().clearColorFilter();

    performanceButton.setOnClickListener(
        v -> startActivity(new Intent(context, PerformanceActivity.class)));
    settingsButton.setOnClickListener(v -> {
      startActivity(new Intent(context, SettingsActivity.class));
    });
    userProfileButton
        .setOnClickListener(v -> {
          Intent intent = new Intent(context, UserProfileActivity.class);
          intent.putExtra("request", USER_MODIFICATION_REQUEST);
          startActivity(intent);
        });
  }

  private void enableRun() {
    runButton.getBackground().clearColorFilter();
    runButton.setOnClickListener(v -> startActivity(new Intent(context, RunActivity.class)));
  }

  private void initialiseViews() {
    runButton = findViewById(R.id.runButton);
    performanceButton = findViewById(R.id.performanceButton);
    settingsButton = findViewById(R.id.settingsButton);
    userProfileButton = findViewById(R.id.userProfileButton);
    weatherTextField = findViewById(R.id.weatherField);
    locationTextField = findViewById(R.id.locationField);

    weatherTextField.setText("Fetching temperature...");
    locationTextField.setText("Fetching location...");
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
