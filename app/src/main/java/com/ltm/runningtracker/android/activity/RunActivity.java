package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.URI_MATCHER;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.RunActivityViewModel;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;
import com.ltm.runningtracker.exception.WeatherNotAvailableException;
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
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/LocationComponentOptionsActivity.java
 */
public class RunActivity extends AppCompatActivity implements
    OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
    OnCameraTrackingChangedListener {

  private PermissionsManager permissionsManager;

  private static boolean isRunning = false;
  private MapboxMap mapboxMap;
  private LocationComponent locationComponent;
  private boolean isInTrackingMode;
  private MapView mapView;
  private Button toggleRunButton;
  private double totalDistance, startLat, startLon, endLat, endLon;
  private long startTime;
  private RunActivityViewModel runActivityViewModel;
  private Location currentLocation;
  private ContentValues contentValues;
  private String temperature;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_run);

    totalDistance = 0;
    runActivityViewModel = ViewModelProviders.of(this).get(RunActivityViewModel.class);
    contentValues = new ContentValues();
    toggleRunButton = findViewById(R.id.toggleRunButton);
    toggleRunButton.setText("Start run");

    Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
    getContentResolver().registerContentObserver(
        ContentProviderContract.ALL_URI, true, new ChangeObserver(new Handler()));

  }

  public void toggleRun(View v) {
    if(!isRunning) {
      toggleRunButton.setText("End run");
      onRunStart();
      isRunning = true;
    } else {
      toggleRunButton.setText("Start run");
      onRunEnd();
      isRunning = false;
    }
  }

  public void onRunStart() {
    startTime = Calendar.getInstance().getTime().getTime();
    startLat = runActivityViewModel.getLocation().getValue().getLatitude();
    startLon = runActivityViewModel.getLocation().getValue().getLongitude();
    currentLocation = runActivityViewModel.getLocation().getValue();

    runActivityViewModel.getLocation().observe(this, location -> {
      // Dynamically increases the distance covered rather than calculating distance between point A and point B
      totalDistance += location.distanceTo(currentLocation);
      currentLocation = location;
      Log.d("Current distance", "" + totalDistance);
    });
  }

  public void onRunEnd() {

    /**
     * https://github.com/probelalkhan/android-room-database-example/blob/master/app/src/main/java/net/simplifiedcoding/mytodo/AddTaskActivity.java
     */
    class SaveRun extends AsyncTask<Void, Void, Void> {

      @Override
      protected Void doInBackground(Void... voids) {
        try {
          temperature = getWeatherRepository().getTemperature();
        } catch (NullPointerException e) {
          temperature = "Unavailable";
        //  throw new WeatherNotAvailableException("Weather unavailable");
        }

        endLat = runActivityViewModel.getLocation().getValue().getLatitude();
        endLon = runActivityViewModel.getLocation().getValue().getLongitude();

        long durationTime = Calendar.getInstance().getTime().getTime() - startTime;
        @SuppressLint("DefaultLocale") String duration = String.format("%02d min, %02d sec",
            TimeUnit.MILLISECONDS.toMinutes(durationTime),
            TimeUnit.MILLISECONDS.toSeconds(durationTime) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationTime))
        );

        double averageSpeed = getLocationRepository()
            .calculateAverageSpeed(totalDistance, durationTime);

        contentValues.put("weather", temperature);
        contentValues.put("duration", duration);
        contentValues.put("startLat", startLat);
        contentValues.put("startLon", startLon);
        contentValues.put("endLat", endLat);
        contentValues.put("endLon", endLon);
        contentValues.put("totalDistance", totalDistance);
        contentValues.put("averageSpeed", averageSpeed);

        Uri uri = getContentResolver().insert(ContentProviderContract.RUNS_URI, contentValues);
        Log.d("URI", uri.toString());
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        finish();
        if (temperature.equals("Unavailable")) {
          Toast.makeText(getApplicationContext(), "Weather unavailable - run saved",
              Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_LONG).show();
        }
      }
    }

    new SaveRun().execute();

  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        enableLocationComponent(style);
      }
    });
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
              .locationEngine(getLocationRepository().getLocationEngine())
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

      findViewById(R.id.back_to_camera_tracking_mode)
          .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (!isInTrackingMode) {
                isInTrackingMode = true;
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.zoomWhileTracking(16f);
                Toast.makeText(RunActivity.this, "Tracking mode re-enabled",
                    Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(RunActivity.this, "Tracking mode already enabled, move map",
                    Toast.LENGTH_SHORT).show();
              }
            }
          });

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
      mapboxMap.getStyle(new Style.OnStyleLoaded() {
        @Override
        public void onStyleLoaded(@NonNull Style style) {
          enableLocationComponent(style);
        }
      });
    } else {
      Toast.makeText(this, "User permission not granted", Toast.LENGTH_LONG).show();
      finish();
    }
  }

  @SuppressWarnings({"MissingPermission"})
  protected void onStart() {
    super.onStart();
    mapView.onStart();
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  class ChangeObserver extends ContentObserver {

    public ChangeObserver(Handler handler) {
      super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
      this.onChange(selfChange, null);
    }

    // When data in the database changes
    @Override
    public void onChange(boolean selfChange, Uri uri) {
      switch (URI_MATCHER.match(uri)) {
        case 0:

          break;
        case 1:
          break;
      }
    }
  }

}