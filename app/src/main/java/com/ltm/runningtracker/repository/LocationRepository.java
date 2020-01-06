package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.util.Constants.NO_TERRITORY;
import static java.lang.String.format;

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
import java.util.Objects;

/**
 * The LocationRepository is responsible for storing and exposing Location data, and updating it by
 * listening to LocationEngineCallback events.
 *
 * Additionally, it provides the functionality of translating latitude and longitude coordinates
 * into a String representing the County, or as "No Man's Land." if no county is available for the
 * given location.
 *
 * Modification to the location object is synchronized with WeatherService's read of the same
 * location object. Although WeatherService does not update the value, we always want it to retrieve
 * the correct, most up-to-date location.
 */
public class LocationRepository implements LocationEngineCallback {

  private static Geocoder geocoder = new Geocoder(getAppContext(), Locale.getDefault());

  private MutableLiveData<Location> locationMutableLiveData;
  private MutableLiveData<String> countyMutableLiveData;

  /*
  Although we can define the method's signature as synchronized,
  synchronisation via lock object allows us to notify the WeatherRepository
  when location is first retrieved
  */
  private static final Object lock = new Object();

  /*
  Must be stored in repo because it is used by both the locationMutableLiveData service and passed
  to mapbox API in the run activity
  */
  private LocationEngine locationEngine;

  public LocationRepository() {
    locationMutableLiveData = new MutableLiveData<>();
    countyMutableLiveData = new MutableLiveData<>();
    locationEngine = LocationEngineProvider.getBestLocationEngine(getAppContext());
  }

  @Override
  public void onSuccess(Object result) {
    Location lastLocation = ((LocationEngineResult) result).getLastLocation();
    setLocation(lastLocation);
  }

  @Override
  public void onFailure(@NonNull Exception exception) {
  }

  /**
   * Expose locationMutableLiveData as livedata object to make it immutable from outside the class
   * Only the locationMutableLiveData engine change the value of locationMutableLiveData
   */
  public LiveData<Location> getLocationLiveData() {
    return locationMutableLiveData;
  }

  public LiveData<String> getCountyLiveData() {
    return countyMutableLiveData;
  }

  public synchronized Location getLocation() {
    return locationMutableLiveData.getValue();
  }

  public synchronized double getLatitude() {
    return Objects.requireNonNull(locationMutableLiveData.getValue()).getLatitude();
  }

  public synchronized double getLongitude() {
    return Objects.requireNonNull(locationMutableLiveData.getValue()).getLongitude();
  }

  public LocationEngine getLocationEngine() {
    // To ensure single source of truth
    return locationEngine;
  }

  /**
   * @param distance in metres
   * @param duration in milliseconds
   * @return km/h
   */
  public static float calculatePace(double distance, double duration) {
    float metersPerSecond = (float) distance / (float) (duration / 1000);
    float kmPerHour = (metersPerSecond * 3.6f) / 1000;
    return Float.parseFloat(format("%.2f", kmPerHour));
  }

  /**
   * https://developer.android.com/training/location/display-address
   */

  /**
   * @param location object
   * @return String representing County, or "No Man's Land" if no county available.
   * @see <a href="https://developer.android.com/training/location/display-address">Android
   * Documentation on displaying addresses</a>
   */
  public static String getCounty(Location location) {
    String city = null;

    try {
      List<Address> address = geocoder.getFromLocation(
          location.getLatitude(),
          location.getLongitude(),
          // In this sample, get just a single address.
          1);
      if ((address == null) || (address.size() == 0)) {
        city = NO_TERRITORY;
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

  private void setLocation(Location location) {
    /*
     If this is the first time we set locationMutableLiveData, we can notify the temperature update service to start
     fetching temperature updates
     */
    synchronized (lock) {
      this.locationMutableLiveData.setValue(location);

      String county = getCounty(location);
      if (county != null) {
        this.countyMutableLiveData.setValue(county);
      }
      lock.notify();
    }
  }

}