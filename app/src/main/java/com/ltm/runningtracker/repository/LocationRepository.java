package com.ltm.runningtracker.repository;

import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import com.ltm.runningtracker.User;
import com.ltm.runningtracker.listener.TrackerLocationListener;
import android.location.Location;

public class LocationRepository {

  private Location location;
  private LocationManager locationManager;


  public LocationRepository(Context context) {
    locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    locationManager =
        (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    try {
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
          3, // minimum time interval between updates
          1, // minimum distance between updates, in metres
          new TrackerLocationListener(this));
    } catch (SecurityException e) {
      Log.d("g53mdp", e.toString());
    }
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
}
