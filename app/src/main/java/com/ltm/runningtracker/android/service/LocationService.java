package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.RunCoordinates;
import com.mapbox.android.core.location.LocationEngineRequest;
import java.util.Calendar;

public class LocationService extends Service {

  // Binder given to clients
  private final IBinder binder = new LocationServiceBinder();
  private boolean isUserRunning;

  private double totalDistance;
  private float startLat, startLon, endLat, endLon;
  private long startTime;
  private Location currentLocation;
  private ContentValues contentValues;
  private String temperature;

  @Override
  public void onCreate() {
    super.onCreate();
    isUserRunning = false;
    totalDistance = 0;
    contentValues = new ContentValues();

    startLocationThread();
  }

  @Override
  public void onDestroy() {
    Log.d("Weather Service", "onDestroy");
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onRebind(Intent intent) {
    // TODO Auto-generated method stub
    Log.d("Weather Service", "onUnbind");
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    // TODO Auto-generated method stub
    Log.d("g53mdp", "service onUnbind");
    return super.onUnbind(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // TODO Auto-generated method stub
    Log.d("g53mdp", "service onStartCommand");
    return Service.START_STICKY;
  }

  // Must stopSelf() when application is killed
  @Override
  public void onTaskRemoved(Intent intent) {
    stopSelf();
    super.onTaskRemoved(intent);
  }

  public class LocationServiceBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

    public LocationService getService() {
      // Return this instance of LocalService so clients can call public methods
      return LocationService.this;
    }

    public boolean isUserRunning() {
      return isUserRunning;
    }

    public boolean toggleRun() {
      if(isUserRunning) {

      } else {
        onRunStart();
      }

      isUserRunning = !isUserRunning;
      return isUserRunning;
    }

  }

  public void onRunStart() {
    startTime = Calendar.getInstance().getTime().getTime();
    startLat = (float) getLocationRepository().getLocation().getLatitude();
    startLon = (float) getLocationRepository().getLocation().getLongitude();
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

        endLat = (float) runActivityViewModel.getLocation().getValue().getLatitude();
        endLon = (float) runActivityViewModel.getLocation().getValue().getLongitude();

        long durationTime = Calendar.getInstance().getTime().getTime() - startTime;
        byte[] runCoordinates = RunCoordinates
            .toByteArray(new RunCoordinates(startLat, startLon, endLat, endLon));
        contentValues.put("runCoordinates", runCoordinates);
        contentValues.put("temperature", temperature);
        contentValues.put("duration", durationTime);
        contentValues.put("distance", totalDistance);
        contentValues.put("date", System.currentTimeMillis());
        getContentResolver().insert(DroidProviderContract.RUNS_URI, contentValues);
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

  private void startLocationThread() {
    try {
      // The following requestLocationUpdates call will spin up a background thread which we can listen to
      // by implementing the LocationEngineCallback interface
      LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(
          getPropertyManager().getMinTime()).build();
      // Similar to weather service, we pass the location repository as listener and allow it
      // to update itself when callback occurs
      getLocationRepository().getLocationEngine().requestLocationUpdates(locationEngineRequest, getLocationRepository(), null);
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }
  }

}
