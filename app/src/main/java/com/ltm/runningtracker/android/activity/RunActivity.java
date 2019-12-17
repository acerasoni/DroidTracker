package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ltm.runningtracker.R;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
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

/**
 * https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/LocationComponentOptionsActivity.java
 */
public class RunActivity extends AppCompatActivity implements
    OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
    OnCameraTrackingChangedListener {

  private PermissionsManager permissionsManager;

  private MapboxMap mapboxMap;
  private LocationComponent locationComponent;
  private boolean isInTrackingMode;
  private MapView mapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_run);

    Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this); }


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

    public void printout(View v){

  }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
      // Check if permissions are enabled and if not request
      if (PermissionsManager.areLocationPermissionsGranted(this)) {

        // Create and customize the LocationComponent's options
        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
            .elevation(5)
            .accuracyAlpha(.6f)
            .accuracyColor(Color.RED)
            .build();

        // Get an instance of the component
        locationComponent = mapboxMap.getLocationComponent();

        LocationComponentActivationOptions locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .locationEngine(getLocationRepository().getLocationEngine())
                .build();

        // Activate with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING,3, 15.0, null, null, null);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);

        // Add the location icon click listener
        locationComponent.addOnLocationClickListener(this);

        // Add the camera tracking listener. Fires if the map camera is manually moved.
        locationComponent.addOnCameraTrackingChangedListener(this);

        findViewById(R.id.back_to_camera_tracking_mode).setOnClickListener(new View.OnClickListener() {
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

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public void onLocationComponentClick() {
      if (locationComponent.getLastKnownLocation() != null) {
        Toast.makeText(this, "Lat" +
            locationComponent.getLastKnownLocation().getLatitude() + "Lon"
 +            locationComponent.getLastKnownLocation().getLongitude(), Toast.LENGTH_LONG).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    @SuppressWarnings( {"MissingPermission"})
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
  }