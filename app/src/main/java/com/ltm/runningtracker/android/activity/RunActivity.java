package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.repository.LocationRepository.getCounty;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.android.service.LocationService;
import com.ltm.runningtracker.android.service.LocationService.LocationServiceBinder;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/LocationComponentOptionsActivity.java
 */
public class RunActivity extends AppCompatActivity implements
    OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
    OnCameraTrackingChangedListener {

  // Service-related
  private ActivityViewModel runActivityViewModel;
  private IntentFilter runUpdateFilter;
  private Boolean isRunning = null;
  boolean mBound;
  private RunUpdateReceiver runUpdateReceiver;
  Context activity = this;

  // Mapbox-related
  private MapboxMap mapboxMap;
  private LocationComponent locationComponent;
  private PermissionsManager permissionsManager;
  private boolean isInTrackingMode;

  // Views
  private TextView countyView;
  private TextView distanceView;
  private TextView temperatureView;
  private TextView durationView;
  private MapView mapView;
  private Button toggleRunButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_run);

    initialiseBroadcastReceiver();
    initialiseViews();
    setupMaxbox(savedInstanceState);
  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.LIGHT, style -> enableLocationComponent(style));
  }

  @SuppressWarnings({"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    // Check if permissions are enabled and if not request
    if (PermissionsManager.areLocationPermissionsGranted(this)) {

      // Create and customize the LocationComponent's options
      LocationComponentOptions customLocationComponentOptions = LocationComponentOptions
          .builder(this)
          .elevation(5)
          .accuracyAlpha(.6f)
          .accuracyColor(Color.RED)
          .build();

      // Get an instance of the component
      locationComponent = mapboxMap.getLocationComponent();

      LocationComponentActivationOptions locationComponentActivationOptions =
          LocationComponentActivationOptions.builder(this, loadedMapStyle)
              .locationComponentOptions(customLocationComponentOptions)
              // utilising the repository's location engine, hence no redundancy
              .locationEngine(runActivityViewModel.getLocationEngine())
              .build();

      // Activate with options
      locationComponent.activateLocationComponent(locationComponentActivationOptions);

      // Enable to make component visible
      locationComponent.setLocationComponentEnabled(true);

      // Set the component's camera mode
      locationComponent.setCameraMode(CameraMode.TRACKING, 3, 15.0, null, null, null);

      // Set the component's render mode
      locationComponent.setRenderMode(RenderMode.COMPASS);

      // Add the location icon click listener
      locationComponent.addOnLocationClickListener(this);

      // Add the camera tracking listener. Fires if the map camera is manually moved.
      locationComponent.addOnCameraTrackingChangedListener(this);

    } else {
      permissionsManager = new PermissionsManager(this);
      permissionsManager.requestLocationPermissions(this);
    }
  }

  @SuppressWarnings({"MissingPermission"})
  @Override
  public void onLocationComponentClick() {
    if (locationComponent.getLastKnownLocation() != null) {
      Toast.makeText(this, "Lat" +
          locationComponent.getLastKnownLocation().getLatitude() + "Lon"
          + locationComponent.getLastKnownLocation().getLongitude(), Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public void onCameraTrackingDismissed() {
    isInTrackingMode = false;
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    // Empty on purpose
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, "Requesting user permission", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      mapboxMap.getStyle(style -> enableLocationComponent(style));
    } else {
      Toast.makeText(this, "User permission not granted", Toast.LENGTH_LONG).show();
      finish();
    }
  }


  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(@NotNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onStart() {
    Intent intent = new Intent(this, LocationService.class);

    // Bind to location service
    bindService(intent, connection, Context.BIND_AUTO_CREATE);
    registerReceiver(runUpdateReceiver, runUpdateFilter);
    mapView.onStart();
    super.onStart();
  }

  @Override
  protected void onDestroy() {
    if (mBound) {
      unregisterReceiver(runUpdateReceiver);
      unbindService(connection);
    }
    mapView.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
        IBinder service) {
      Log.d("Bound", "b");
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      LocationServiceBinder binder = (LocationServiceBinder) service;

      // Determine if user is running
      isRunning = binder.isUserRunning();
      if (!isRunning) {
        durationView.setText("Begin run to display progress");
      } else {
        distanceView.setText(binder.getDistance() + " metres");
        durationView.setText("Fetching time...");
      }
      setButtonText(isRunning);

      toggleRunButton.setOnClickListener(v -> {
        isRunning = binder.toggleRun();
        setButtonText(isRunning);
        if (!isInTrackingMode) {
          isInTrackingMode = true;
          locationComponent.setCameraMode(CameraMode.TRACKING);
          locationComponent.zoomWhileTracking(16f);
          Toast.makeText(RunActivity.this, determineText(isRunning),
              Toast.LENGTH_SHORT).show();
        }
      });

      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }

    private String determineText(boolean isRunning) {
      return isRunning ? "Run started" : "Run ended";
    }

    private void setButtonText(boolean isRunning) {
      if (isRunning) {
        toggleRunButton.setText("End run");
      } else {
        toggleRunButton.setText("Start run");
      }
    }
  };

  private void setupMaxbox(Bundle savedInstanceState) {
    Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  private void initialiseViews() {
    runActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);
    countyView = findViewById(R.id.countyView);
    durationView = findViewById(R.id.timeView);
    temperatureView = findViewById(R.id.temperatureView);
    distanceView = findViewById(R.id.distanceView);
    toggleRunButton = findViewById(R.id.toggleRunButton);

    // Non run-related info can be observed straight away
    runActivityViewModel.getLocation().observe(this, location -> {
      countyView.setText(getCounty(location));
    });

    // Non run-related info can be observed straight away
    runActivityViewModel.getWeather().observe(this, weather -> {
      temperatureView.setText(weather.temperature.getTemp() + "°C");
    });

    toggleRunButton.setText("Fetching location...");
  }

  private void initialiseBroadcastReceiver() {
    runUpdateReceiver = new RunUpdateReceiver();

    runUpdateFilter = new IntentFilter();
    runUpdateFilter.addAction("com.ltm.runningtracker.DISTANCE_UPDATE");
    runUpdateFilter.addAction("com.ltm.runningtracker.TIME_UPDATE");
    runUpdateFilter.addAction("com.ltm.runningtracker.RUN_ENDED");
  }

  private class RunUpdateReceiver extends BroadcastReceiver {

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (Objects.requireNonNull(action)) {
        case "com.ltm.runningtracker.TIME_UPDATE":
          int time = intent.getIntExtra("time", -1);
          durationView.setText(String.format("%02d min, %02d sec",
              TimeUnit.SECONDS.toMinutes(time),
              TimeUnit.SECONDS.toSeconds(time) -
                  TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time))
          ));
          break;
        case "com.ltm.runningtracker.DISTANCE_UPDATE":
          double distance = intent.getDoubleExtra("distance", -1L);
          int formattedDistance = (int) distance;
          distanceView.setText(formattedDistance + " metres");
          break;
        case "com.ltm.runningtracker.RUN_ENDED":
          AppCompatActivity a = (AppCompatActivity) activity;
          a.setResult(RESULT_OK);
          a.finish();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(action));
      }
    }
  }

}