package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import com.ltm.runningtracker.listener.TrackerLocationListener;
import android.location.Location;

public class LocationRepository {

  private Location location;
  private LocationManager locationManager;

  public LocationRepository() {
    locationManager =
        (LocationManager) getAppContext().getSystemService(Context.LOCATION_SERVICE);

    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          getPropertyManager().getMinTime(), // minimum time interval between updates
          getPropertyManager().getMinDistance(), // minimum distance between updates, in metres
          new TrackerLocationListener(this));
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

}
