package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.database.model.Run.getFormattedTime;
import static com.ltm.runningtracker.repository.LocationRepository.getCounty;
import static com.ltm.runningtracker.util.Constants.BEGIN_RUN_TO_DISPLAY;
import static com.ltm.runningtracker.util.Constants.DISTANCE;
import static com.ltm.runningtracker.util.Constants.DISTANCE_UPDATE_ACTION;
import static com.ltm.runningtracker.util.Constants.END_RUN;
import static com.ltm.runningtracker.util.Constants.FETCHING_LOCATION;
import static com.ltm.runningtracker.util.Constants.PERMISSION_NOT_GRANTED;
import static com.ltm.runningtracker.util.Constants.REQUESTING_PERMISSION;
import static com.ltm.runningtracker.util.Constants.RUN_ENDED;
import static com.ltm.runningtracker.util.Constants.RUN_END_ACTION;
import static com.ltm.runningtracker.util.Constants.RUN_STARTED;
import static com.ltm.runningtracker.util.Constants.START_RUN;
import static com.ltm.runningtracker.util.Constants.TIME_UPDATE_ACTION;
import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
 * This Activity allows the user to see his location on a map, alongside the current weather. He can
 * then choose to begin a run. The map will continue tracking his location throughout the run. When
 * he decides to end the run, the activity is destroyed and the run saved to the database.
 *
 * There is no need for the user to manually insert any information. The location, weather, and all
 * other data associated to the run are either fetched from third-party API's or computed
 * on-the-fly, and inserted in the database as columns of the run's row.
 *
 * When the run begins, the Location Service is turned into a foreground service as its purpose now
 * changes. Previously, it was required by the UI to display current location and weather. Now, it
 * needs to track location in the absence of activities. When the run ends, it will be reverted into
 * a background service. The reason for this design decision to avoid having two different location
 * services.
 *
 * This Activity will bind to the service and determine if a run is occurring, and update its UI
 * state accordingly. To avoid wastage of resources, the service is The Mapbox API has been setup
 * following the official documentation guide.
 *
 * @see <a href="https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/LocationComponentOptionsActivity.java">
 * MapBox documentation</a>
 *
 * This Activity utilises an internal BroadcastReceiver to receive updates on the run progress.
 * However, its view still observe the repository LiveData objects and update accordingly.
 */
public class RunActivity extends AppCompatActivity implements
    OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
    OnCameraTrackingChangedListener {

  // Service-related
  private ActivityViewModel runActivityViewModel;
  private IntentFilter runUpdateFilter;
  private Boolean isRunning = null;
  private Boolean isPaused = null;
  boolean mBound;
  private RunUpdateReceiver runUpdateReceiver;
  Context activity = this;

  // Mapbox-related
  private MapboxMap mapboxMap;
  private MapView mapView;
  private LocationComponent locationComponent;
  private PermissionsManager permissionsManager;
  private boolean isInTrackingMode;

  // Views
  private TextView countyView;
  private TextView distanceView;
  private TextView temperatureView;
  private TextView durationView;
  private Button pauseResumeButton;
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
    mapboxMap.setStyle(Style.OUTDOORS, this::enableLocationComponent);
  }

  @SuppressWarnings({"MissingPermission"})
  @Override
  public void onLocationComponentClick() {
    if (locationComponent.getLastKnownLocation() != null) {
      Toast.makeText(this, getResources().getString(R.string.lat) +
          locationComponent.getLastKnownLocation().getLatitude() + getResources()
          .getString(R.string.lon)
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
    Toast.makeText(this, REQUESTING_PERMISSION, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {
    if (granted) {
      mapboxMap.getStyle(style -> enableLocationComponent(style));
    } else {
      Toast.makeText(this, PERMISSION_NOT_GRANTED, Toast.LENGTH_LONG).show();
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

    LocalBroadcastManager.getInstance(this).registerReceiver(runUpdateReceiver, runUpdateFilter);
    mapView.onStart();
    super.onStart();
  }

  @Override
  protected void onDestroy() {
    if (mBound) {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(runUpdateReceiver);
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
    pauseResumeButton = findViewById(R.id.pauseResumeButton);
    toggleRunButton = findViewById(R.id.toggleRunButton);

    // Non run-related info can be observed straight away
    runActivityViewModel.getLocation().observe(this, location -> {
      countyView.setText(getCounty(location));
    });

    // Non run-related info can be observed straight away
    runActivityViewModel.getWeather().observe(this, weather -> {
      StringBuilder sb = new StringBuilder(Float.toString(weather.temperature.getTemp()))
          .append(getResources().getString(R.string.degrees_celsius));
      temperatureView.setText(sb.toString());
    });

    toggleRunButton.setText(FETCHING_LOCATION);
  }

  private void initialiseBroadcastReceiver() {
    runUpdateReceiver = new RunUpdateReceiver();

    runUpdateFilter = new IntentFilter();
    runUpdateFilter.addAction(DISTANCE_UPDATE_ACTION);
    runUpdateFilter.addAction(TIME_UPDATE_ACTION);
    runUpdateFilter.addAction(RUN_END_ACTION);
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

  /**
   * This implementation of a custom BroadcastReceiver allows the activity to react to time,
   * distance and runEnd actions broadcasts sent by the Location service.
   */
  private class RunUpdateReceiver extends BroadcastReceiver {

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      switch (Objects.requireNonNull(action)) {
        case TIME_UPDATE_ACTION:
          // In seconds
          int time = intent.getIntExtra(getResources().getString(R.string.time), -1);
          String formattedTime = getFormattedTime(time);
          durationView.setText(formattedTime);
          break;
        case DISTANCE_UPDATE_ACTION:
          double distance = intent.getDoubleExtra(DISTANCE, -1L);
          int formattedDistance = (int) distance;
          StringBuilder sb = new StringBuilder(Integer.toString(formattedDistance)).append(" ")
              .append(getResources().getString(R.string.metres));
          distanceView.setText(sb.toString());
          break;
        case RUN_END_ACTION:
          AppCompatActivity a = (AppCompatActivity) activity;
          a.setResult(RESULT_OK);
          a.finish();
          break;
        default:
          throw new IllegalStateException(UNEXPECTED_VALUE + Objects.requireNonNull(action));
      }
    }
  }

  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
        IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      LocationServiceBinder binder = (LocationServiceBinder) service;

      // Determine if user is running
      isRunning = binder.isUserRunning();
      setButtonText(isRunning);
      if (!isRunning) {
        durationView.setText(BEGIN_RUN_TO_DISPLAY);
        pauseResumeButton.setVisibility(View.INVISIBLE);
      } else {
        durationView.setText(getFormattedTime(binder.getTime()));
        StringBuilder sb = new StringBuilder().append(binder.getDistance()).append(" ")
            .append(getResources().getString(R.string.metres));
        distanceView.setText(sb.toString());
        pauseResumeButton.setVisibility(View.VISIBLE);

        // If running, determine if run is paused
        isPaused = binder.isRunPaused();
        renderPauseButtonText(isPaused);
      }

      toggleRunButton.setOnClickListener(v -> {
        isRunning = binder.toggleRun();
        renderPauseButtonText(false);
        setButtonText(isRunning);
        pauseResumeButton.setVisibility(View.VISIBLE);
        if (!isInTrackingMode) {
          isInTrackingMode = true;
          locationComponent.setCameraMode(CameraMode.TRACKING);
          locationComponent.zoomWhileTracking(16f);
          Toast.makeText(RunActivity.this, determineText(isRunning),
              Toast.LENGTH_SHORT).show();
        }
      });

      pauseResumeButton.setOnClickListener(v -> {
        isPaused = binder.togglePause();
        renderPauseButtonText(isPaused);
      });

      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }

    private String determineText(boolean isRunning) {
      return isRunning ? RUN_STARTED : RUN_ENDED;
    }

    private void setButtonText(boolean isRunning) {
      if (isRunning) {
        toggleRunButton.setText(END_RUN);
      } else {
        toggleRunButton.setText(START_RUN);
      }
    }
  };

  private void renderPauseButtonText(boolean isPaused) {
    if (isPaused) {
      pauseResumeButton.setText(R.string.resume);
    } else {
      pauseResumeButton.setText(R.string.pause);
    }
  }

}