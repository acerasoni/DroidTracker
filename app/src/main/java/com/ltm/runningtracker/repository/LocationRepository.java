package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.location.Location;
import com.ltm.runningtracker.exception.InvalidLatitudeOrLongitudeException;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationRepository implements LocationEngineCallback {

  private static Geocoder geocoder = new Geocoder(getAppContext(), Locale.getDefault());

  private MutableLiveData<Location> locationMutableLiveData;
  private MutableLiveData<String> stringMutableLiveData;

  // Static because there should be only one instance of this to allow synchronized
  // read/write of locationMutableLiveData
  private static Object lock = new Object();

  // Must be stored in repo because it is used by both the locationMutableLiveData service and passed
  // to mapbox API in the run activity
  private LocationEngine locationEngine;

  public LocationRepository() {
    locationMutableLiveData = new MutableLiveData<>();
    stringMutableLiveData = new MutableLiveData<>();
    locationEngine = LocationEngineProvider.getBestLocationEngine(getAppContext());
  }

  @Override
  public void onSuccess(Object result) {
    Location lastLocation = ((LocationEngineResult) result).getLastLocation();
    Log.d("Locationrep: ", "onLocationRetrieved " + lastLocation.toString());
    setLocation(lastLocation);
  }

  @Override
  public void onFailure(@NonNull Exception exception) {
    Log.d("Location Repository: ", exception.getMessage());
  }

  // Expose locationMutableLiveData as livedata object to make it immutable from outside the class
  // Only the locationMutableLiveData engine change the value of locationMutableLiveData
  public LiveData<Location> getLocationLiveData() {
    return locationMutableLiveData;
  }

  public LiveData<String> getCountyLiveData() {
    return stringMutableLiveData;
  }

  public Location getLocation() {
    return locationMutableLiveData.getValue();
  }

  public double getLatitude() {
    return locationMutableLiveData.getValue().getLatitude();
  }

  public double getLongitude() {
    return locationMutableLiveData.getValue().getLongitude();
  }

  public LocationEngine getLocationEngine() {
    // To ensure single source of truth
    return locationEngine;
  }

  /**
   * @param distance in metres
   * @param duration in milliseconds
   * @return mph
   */
  public static float calculatePace(double distance, double duration) {
    float metersPerSecond = (float) distance / (float) (duration / 1000);
    float kmPerHour = (metersPerSecond * 3.6f) / 1000;
    return kmPerHour;
  }

  private void setLocation(Location location) {
    // If this is the first time we set locationMutableLiveData, we can notify the temperature update service to start
    // fetching temperature updates
    synchronized (lock) {
      this.locationMutableLiveData.setValue(location);

      String county = getCounty(location);
      if (county != null) {
        this.stringMutableLiveData.setValue(county);
      }
      lock.notify();
    }
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
      if (address == null || address.size() == 0) {
        city = "No man's land.";
      } else {
        city = address.get(0).getSubAdminArea();
      }
    } catch (IOException ioException) {
      // Catch network or other I/O problems.
      Log.e("Location Repository ", "IOException occurred while fetching address", ioException);
    } catch (IllegalArgumentException illegalArgumentException) {
      // Catch invalid latitude or longitude values.
      throw new InvalidLatitudeOrLongitudeException(
          "Invalid values for Latitude = " + location.getLatitude() +
              ", Longitude = " + location.getLongitude());
    } catch (NullPointerException nullPointerException) {
      Log.e("Location Repository ", "Could not fetch locationMutableLiveData",
          nullPointerException);
    }
    return city;
  }

  public static Object getLock() {
    return lock;
  }
}
