package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;

import android.app.PendingIntent;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import android.location.Location;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

public class LocationRepository implements LocationEngineCallback {

  private MutableLiveData<Location> location;
  private LocationEngine locationEngine;

  public LocationRepository() {
    location = new MutableLiveData<>();
    locationEngine = LocationEngineProvider.getBestLocationEngine(getAppContext());

    try {
      LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(0).build();
      locationEngine.requestLocationUpdates(locationEngineRequest, this, null);
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }

  }

  private void setLocation(Location location) {
    this.location.setValue(location);
  }

  public Location getLocation(){
    return location.getValue();
  }

  public LocationEngine getLocationEngine() {
    // To ensure single source of truth
    return locationEngine;
  }

  @Override
  public void onSuccess(Object result) {
    setLocation(((LocationEngineResult) result).getLastLocation());
  }

  @Override
  public void onFailure(@NonNull Exception exception) {

  }
}
