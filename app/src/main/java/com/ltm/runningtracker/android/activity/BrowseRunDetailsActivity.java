package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DATE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DISTANCE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DURATION_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.NAME_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.PACE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TEMPERATURE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TYPE_COL;
import static com.ltm.runningtracker.util.Constants.REQUESTING_PERMISSION;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.RunCoordinates.Coordinate;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * This activity allows the user to browse details relating to a specific run. He can then do one of
 * the following:
 *
 * 1. Tag the run as a specific type (note that newly completed runs are 'untagged' by default) 2.
 * Modify the run's tag to another run type 3. Delete the run altogether
 */
public class BrowseRunDetailsActivity extends AppCompatActivity implements OnItemSelectedListener,
    OnMapReadyCallback, OnLocationClickListener, PermissionsListener,
    OnCameraTrackingChangedListener {

  // Views
  private TextView activityView;
  private TextView locationView;
  private TextView dateView;
  private TextView temperatureView;
  private TextView distanceView;
  private TextView durationView;
  private TextView paceView;
  private Spinner spinner;
  private ImageView temperatureImage;

  private MapboxMap mapboxMap;
  private MapView mapView;

  // Internal logic
  private boolean hasTagBeenModified;
  private int runId;
  private int newRunType;
  private WeatherClassifier weatherClassifier;
  private ActivityViewModel browseDetailsActivityViewModel;
  private RunCoordinates runCoordinates;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);
    initialiseViews();

    hasTagBeenModified = false;
    runId = getIntent().getIntExtra(getResources().getString(R.string.run_id),
        -1);
    int fromFragment = getIntent()
        .getIntExtra(getResources().getString(R.string.from_fragment), -1);
    weatherClassifier = WeatherClassifier.valueOf(fromFragment);
    runCoordinates = browseDetailsActivityViewModel.getRunCoordinates(weatherClassifier, runId);
    renderWeatherIcon();

    // If the synchronous inspection of cache fails, make another effort to observe short living cache
    // populated by the asynchronous database call
    browseDetailsActivityViewModel.getShortLivingCache().observe(this, run -> {
      if (run != null) {
        updateUI(run);
      }
      browseDetailsActivityViewModel.getShortLivingCache().removeObservers(this);
    });

    Run run = browseDetailsActivityViewModel
        .getRunById(runId, this);

    updateUI(run);
    setupMaxbox(savedInstanceState);
  }

  // <-- Activity lifecycle operations required by MapBox-->

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
    mapView.onStart();
    super.onStart();
  }

  @Override
  protected void onDestroy() {
    mapView.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }


  @Override
  public void onExplanationNeeded(List<String> permissionsToExplain) {
    Toast.makeText(this, REQUESTING_PERMISSION, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onPermissionResult(boolean granted) {

  }

  @Override
  public void onMapReady(@NonNull MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    setupStyle(runCoordinates);
  }

  @SuppressWarnings({"MissingPermission"})
  @Override
  public void onLocationComponentClick() {
  }

  @Override
  public void onCameraTrackingDismissed() {
  }

  @Override
  public void onCameraTrackingChanged(int currentMode) {
    // Empty on purpose
  }

  @SuppressWarnings({"MissingPermission"})
  private void enableLocationComponent(@NonNull Style loadedMapStyle) {
    drawLine(loadedMapStyle);
  }

  public void onSave(@Nullable View v) {
    if (hasTagBeenModified) {
      browseDetailsActivityViewModel.updateTypeOfRun(runId, newRunType, this);
      setResult(RESULT_OK);
    } else {
      setResult(RESULT_CANCELED);
    }

    finish();
  }

  public void onDelete(View v) {
    browseDetailsActivityViewModel.deleteRun(weatherClassifier, this, runId);
    onSave(null);
  }

  // Switch between tabs
  public void onItemSelected(AdapterView<?> parent, View view,
      int pos, long id) {
    newRunType = pos;
    // Will cause listView in previous activity has to refresh its UI state
    hasTagBeenModified = true;
  }

  public void onNothingSelected(AdapterView<?> parent) {
    // Another interface callback
  }

  /**
   * This method populates the UI with the details from the run in the Cursor.
   *
   * @param run
   */
  @SuppressLint({"SetTextI18n", "DefaultLocale"})
  public void updateUI(Run run) {
    runOnUiThread(() -> {
      StringBuilder sb;

      sb = new StringBuilder(getResources().getString(R.string.exercise)).append(" #")
          .append(runId);
      activityView.setText(sb.toString());
      locationView.setText(run.location);
      dateView.setText(run.date);

      String runType = run.runType.toUpperCase();
      spinner.setSelection(RunTypeClassifier.valueOf(runType).getValue());

      sb = new StringBuilder(Integer.toString((int) run.distance)).append(" ")
          .append(getResources().getString(R.string.metres));
      distanceView.setText(sb.toString());

      durationView.setText(run.duration);

      sb = new StringBuilder(
          String.format(getResources().getString(R.string.two_decimals_format), run.temperature))
          .append(getResources().getString(R.string.degrees_celsius));
      temperatureView.setText(sb.toString());

      float pace = run.pace;
      String paceString = String
          .format(getResources().getString(R.string.two_decimals_format), pace);
      sb = new StringBuilder(paceString).append(" ")
          .append(getResources().getString(R.string.kilometers_per_hour));
      paceView.setText(sb.toString());
    });

  }

  private void initialiseViews() {
    activityView = findViewById(R.id.activityView);
    locationView = findViewById(R.id.locationView);
    dateView = findViewById(R.id.dateView);
    distanceView = findViewById(R.id.distanceView);
    durationView = findViewById(R.id.durationView);
    temperatureView = findViewById(R.id.temperatureView);
    paceView = findViewById(R.id.paceView);
    temperatureImage = findViewById(R.id.temperatureImage);

    spinner = findViewById(R.id.activityList);
    browseDetailsActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.activity_types, R.layout.spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
  }

  private void setupMaxbox(Bundle savedInstanceState) {
    Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  private void drawLine(Style style) {
    List<Coordinate> rc = runCoordinates.getRunCoordinates();
    LineManager lineManager = new LineManager(mapView, mapboxMap, style);

    // Retrieve list of coordinates
    List<LatLng> coordinates = new ArrayList<>();
    for (Coordinate c : rc) {
      coordinates.add(c.toLatLng());
    }

    // Draw line
    LineOptions lineOptions = new LineOptions().withLatLngs(coordinates)
        .withLineWidth(6f).withLineColor("red");
    lineManager.create(lineOptions);

    // Bound the box
    LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(coordinates).build();
    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));


  }

  private void setupStyle(RunCoordinates runCoordinates) {
    List<Coordinate> rc = runCoordinates.getRunCoordinates();

    // Draw symbol layer on start and end
    Coordinate start = rc.get(0);
    Coordinate end = rc.get(rc.size() - 1);

    Feature startFeature = Feature.fromGeometry(
        Point.fromLngLat(start.getY(), start.getX()));

    Feature endFeature = Feature.fromGeometry(
        Point.fromLngLat(end.getY(), end.getX()));

    Style.Builder styleBuilder = new Style.Builder().fromUri(Style.OUTDOORS);

    // Add start location icon
    styleBuilder = styleBuilder
        .withImage("START_ICON_ID", BitmapFactory.decodeResource(
            getResources(), R.drawable.start_location_icon)).withSource(
            new GeoJsonSource("START_SOURCE_ID",
                FeatureCollection.fromFeature(startFeature)
            ));

    // Add start icon layer
    styleBuilder = styleBuilder.withLayer(new SymbolLayer("START_LAYER_ID", "START_SOURCE_ID")
        .withProperties(PropertyFactory.iconImage("START_ICON_ID"),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconOffset(new Float[]{0f, -16f}))
    );

    // Add end location icon
    styleBuilder = styleBuilder
        .withImage("END_ICON_ID", BitmapFactory.decodeResource(
            getResources(), R.drawable.end_location_icon)).withSource(
            new GeoJsonSource("END_SOURCE_ID",
                FeatureCollection.fromFeature(endFeature)
            ));

    // Add end icon layer
    styleBuilder = styleBuilder.withLayer(new SymbolLayer("END_LAYER_ID", "END_SOURCE_ID")
        .withProperties(PropertyFactory.iconImage("END_ICON_ID"),
            iconAllowOverlap(true),
            iconIgnorePlacement(true),
            iconOffset(new Float[]{0f, -20f}))
    );

    mapboxMap.setStyle(styleBuilder, this::enableLocationComponent);
  }

  private void renderWeatherIcon() {
    Drawable drawable = null;
    switch (weatherClassifier) {
      case FREEZING:
        drawable = getDrawable(R.drawable.freezing_icon);
        break;
      case COLD:
        drawable = getDrawable(R.drawable.cold_icon);
        break;
      case MILD:
        drawable = getDrawable(R.drawable.mild_icon);
        break;
      case WARM:
        drawable = getDrawable(R.drawable.warm_icon);
        break;
      case HOT:
        drawable = getDrawable(R.drawable.hot_icon);
        break;
    }

    temperatureImage.setImageDrawable(drawable);
  }

}