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
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.RunCoordinates;
import com.mapbox.android.core.location.LocationEngineRequest;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationService extends LifecycleService {

  // Binder given to clients
  private final IBinder binder = new LocationServiceBinder();
  private boolean isUserRunning;

  // Location variables
  private float startLat, startLon, endLat, endLon;
  private double distance;
  private Location currentLocation;

  // Weather
  private String temperature;

  // Time
  private int time;
  ScheduledExecutorService scheduledExecutorService;

  private ContentValues contentValues;


  @Override
  public void onCreate() {
    super.onCreate();
    isUserRunning = false;
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
    super.onBind(intent);
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
    super.onStartCommand(intent, flags, startId);
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
      if (isUserRunning) {
        onRunEnd();
      } else {
        onRunStart();
      }

      isUserRunning = !isUserRunning;
      return isUserRunning;
    }

  }

  public void onRunStart() {
    // Time elapsed since run started
    time = 0;
    distance = 0;
    startLat = (float) getLocationRepository().getLocation().getLatitude();
    startLon = (float) getLocationRepository().getLocation().getLongitude();

    currentLocation = getLocationRepository().getLocation();

    // Start time update thread
    Runnable timeUpdateTask = () -> {
      time++;
      Intent intent = new Intent();
      Bundle bundle = new Bundle();
      bundle.putInt("time", time);
      intent.putExtras(bundle);

      intent.setAction("com.ltm.runningtracker.TIME_UPDATE");
      sendBroadcast(intent);

    };

    // Begin execution of worker thread
    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledExecutorService
        .scheduleAtFixedRate(timeUpdateTask, 0, 1,
            TimeUnit.SECONDS);

    // Observe the location repo for changes
    getLocationRepository().getLocationLiveData()
        .observe(this, location -> {
          // Dynamically increases the distance covered rather than calculating distance between point A and point B
          distance += location.distanceTo(currentLocation);

          Intent intent = new Intent();
          Bundle bundle = new Bundle();
          bundle.putDouble("distance", distance);
          intent.putExtras(bundle);

          intent.setAction("com.ltm.runningtracker.DISTANCE_UPDATE");
          sendBroadcast(intent);

          currentLocation = location;
        });
  }

  public void onRunEnd() {
    // Stop time updates
    scheduledExecutorService.shutdownNow();

    // Stop distance updates
    getLocationRepository().getLocationLiveData().removeObservers
        (this);

    /**
     * https://github.com/probelalkhan/android-room-database-example/blob/master/app/src/main/java/net/simplifiedcoding/mytodo/AddTaskActivity.java
     */
    class SaveRun extends AsyncTask<Void, Void, Void> {

      @Override
      protected Void doInBackground(Void... voids) {
        temperature = getWeatherRepository().getTemperature();

        endLat = (float) getLocationRepository().getLocation().getLatitude();
        endLon = (float) getLocationRepository().getLocation().getLongitude();
        byte[] runCoordinates = RunCoordinates
            .toByteArray(new RunCoordinates(startLat, startLon, endLat, endLon));

        contentValues.put("runCoordinates", runCoordinates);
        contentValues.put("temperature", temperature);
        contentValues.put("duration", time);
        contentValues.put("distance", distance);
        contentValues.put("date", System.currentTimeMillis());
        getContentResolver().insert(DroidProviderContract.RUNS_URI, contentValues);
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
          Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_LONG).show();

          // Asynchronously tell activity that run has been saved
        Intent intent = new Intent();
        intent.setAction("com.ltm.runningtracker.RUN_ENDED");
        sendBroadcast(intent);
      }
    }

    // Save run
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
      getLocationRepository().getLocationEngine()
          .requestLocationUpdates(locationEngineRequest, getLocationRepository(), null);
    } catch (SecurityException e) {
      Log.d("Security exception: ", e.toString());
    }
  }

}
