package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.location.Location;
import androidx.room.Ignore;
import com.ltm.runningtracker.android.service.WeatherUpdateService;
import com.ltm.runningtracker.exception.InvalidLatitudeOrLongitudeException;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationRepository implements LocationEngineCallback {

  private static Geocoder geocoder = new Geocoder(getAppContext(), Locale.getDefault());
  ;
  private MutableLiveData<Location> location;
  private MutableLiveData<String> county;
  private LocationEngine locationEngine;

  public Object lock;

  public LocationRepository() {
    lock = new Object();

    location = new MutableLiveData<>();
    county = new MutableLiveData<>();
    locationEngine = LocationEngineProvider.getBestLocationEngine(getAppContext());

    try {
      LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(
          getPropertyManager().getMinTime()).build();
      locationEngine.requestLocationUpdates(locationEngineRequest, this, null);
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }

  }

  private void setLocation(Location location) {
    // If this is the first time we set location, we can notify the temperature update service to start
    // fetching temperature updates
    synchronized (lock) {
      this.location.setValue(location);
      lock.notify();
    }
  }

  private void setCounty(String county) {
    this.county.setValue(county);
  }

  // Expose location as livedata object to make it immutable from outside the class
  // Only the location engine change the value of location
  public LiveData<Location> getLocationLiveData() {
    return location;
  }

  public LiveData<String> getCountyLiveData() {
    return county;
  }

  public Location getLocation() {
    return location.getValue();
  }

  public double getLatitude() {
    return location.getValue().getLatitude();
  }

  public double getLongitude() {
    return location.getValue().getLongitude();
  }

  public LocationEngine getLocationEngine() {
    // To ensure single source of truth
    return locationEngine;
  }

  @Override
  public void onSuccess(Object result) {
    Location lastLocation = ((LocationEngineResult) result).getLastLocation();
    setLocation(lastLocation);

    String county = getCounty(lastLocation);
    if (county != null) {
      setCounty(county);
    }
  }

  @Override
  public void onFailure(@NonNull Exception exception) {

  }

  /**
   * https://developer.android.com/training/location/display-address
   */
  public static String getCounty(Location location) {
    String city = null;

    try {
      List<Address> address = geocoder.getFromLocation(
          location.getLatitude(),
          location.getLongitude(),
          // In this sample, get just a single address.
          1);
      city = address.get(0).getSubAdminArea();
    } catch (IOException ioException) {
      // Catch network or other I/O problems.
      Log.e("Location Repository ", "IOException occurred while fetching address", ioException);
    } catch (IllegalArgumentException illegalArgumentException) {
      // Catch invalid latitude or longitude values.
      throw new InvalidLatitudeOrLongitudeException(
          "Invalid values for Latitude = " + location.getLatitude() +
              ", Longitude = " + location.getLongitude());
    } catch (NullPointerException nullPointerException) {
      Log.e("Location Repository ", "Could not fetch location", nullPointerException);
    }
    return city;
  }

  public static double calculateDistance(double startLat, double startLon, double endLat,
      double endLon) {
    Location a = new Location("Location A");
    Location b = new Location("Location B");

    a.setLatitude(startLat);
    a.setLongitude(startLon);

    b.setLatitude(endLat);
    b.setLongitude(endLon);

    return (double) a.distanceTo(b);
  }

  /**
   *
   * @param distance in metres
   * @param duration in milliseconds
   * @return mph
   */
  public static float calculatePace(double distance, double duration) {
    double metersPerSecond = distance / (duration / 1000);
    double mph = metersPerSecond * 2.237;
    return (float) mph;
  }

}
