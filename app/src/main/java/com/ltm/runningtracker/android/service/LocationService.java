package com.ltm.runningtracker.android.service;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.util.Constants.CHANNEL_ID;
import static com.ltm.runningtracker.util.Constants.DISTANCE;
import static com.ltm.runningtracker.util.Constants.DISTANCE_UPDATE_ACTION;
import static com.ltm.runningtracker.util.Constants.RUN_END_ACTION;
import static com.ltm.runningtracker.util.Constants.RUN_ONGOING;
import static com.ltm.runningtracker.util.Constants.RUN_PAUSED;
import static com.ltm.runningtracker.util.Constants.TIME_UPDATE_ACTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.RunActivity;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.RunCoordinates.Coordinate;
import com.mapbox.android.core.location.LocationEngineRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * Location Service class, responsible for requesting weather updates and feeding them to the
 * LocationRepository. It will switch to foreground service when run occurs.
 *
 * @see RunActivity
 *
 * It will start the Weather Service and control it via its lifecycle. This is one of the reasons
 * why LocationService extends LifecycleService. An additional reason deciding to extend
 * LifecycleService is to allow the Service to observe LiveData objects, which requires the
 * observing class to be a LifecycleOwner.
 *
 * The run can be paused. This functionality is controlled by RunActivity via Binder interface. When
 * paused the time and distance will be frozen and no more coordinates are added to the
 * RunCoordinates container. The current state is rendered in the notification.
 *
 * Note that even when the run is paused, the map on and both location and temperature TextViews in
 * RunActivity will update. This has been done purposely.

 * Once all activities unbind from the location service, this will stopSelf(). However,
 * this only occurs if the user is not on a run, in which case it will keep running as a foreground
 * service.
 */
public class LocationService extends LifecycleService {

  private static int NOTIFICATION_ID = 1;

  // Binder given to clients
  private final IBinder binder = new LocationServiceBinder();
  private boolean isUserRunning;
  private boolean runPaused;

  // Location variables
  private Double distance;
  private Location currentLocation;
  private RunCoordinates runCoordinates;

  // Weather
  private float temperature;

  // Time
  private int time;
  private Runnable timeUpdateTask;
  private ScheduledExecutorService timeScheduledExecutorService;
  private ScheduledFuture<?> scheduledFuture;

  private ContentValues contentValues;

  @Override
  public void onCreate() {
    super.onCreate();
    isUserRunning = false;
    runPaused = false;
    contentValues = new ContentValues();

    /*
     Can make weather a started service, no need to bind as it's closely coupled
     to the lifecycle of the location service
     */
    startService(new Intent(this, WeatherService.class));
    startLocationThread();
  }

  @Override
  public void onDestroy() {
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
    super.onRebind(intent);
  }

  @Override
  public boolean onUnbind(Intent intent) {
    // Determine is run is ongoing
    if (!isUserRunning) {
      // Explicitly stop weather service
      stopService(new Intent(this, WeatherService.class));
      stopSelf();
    }
    return true;

  }

  @Override
  public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return Service.START_STICKY;
  }

  // Must stopSelf() when application is killed
  @Override
  public void onTaskRemoved(Intent intent) {
    stopSelf();
    super.onTaskRemoved(intent);
  }

  // RunActivity will directly call methods on the binder object to retrieve the run's status
  public class LocationServiceBinder extends Binder implements IInterface {

    @Override
    public IBinder asBinder() {
      return this;
    }

    public boolean isUserRunning() {
      return isUserRunning;
    }

    public boolean isRunPaused() {
      return runPaused;
    }

    public boolean userDeleted() {
      boolean runningState = isUserRunning;
      if (isUserRunning) {
        // Revert to background service
        stopForeground(true);
        onRunEnd(false);
        isUserRunning = false;
      }

      return runningState;
    }

    // Receive a toggle run command from RunActivity
    public boolean toggleRun() {
      if (isUserRunning) {
        // Revert to background service
        stopForeground(true);
        onRunEnd(true);
        isUserRunning = false;
      } else {
        // Convert to foreground service
        startForeground(NOTIFICATION_ID, generateNotification("Run ongoing"));
        onRunStart();
        isUserRunning = true;
      }

      return isUserRunning;
    }

    public boolean togglePause() {
      if (runPaused) {
        // Resume updating time
        scheduledFuture =
            timeScheduledExecutorService
                .scheduleAtFixedRate(timeUpdateTask, 0, 1L, TimeUnit.SECONDS);

        // Update notification
        updateNotification(RUN_ONGOING);

        // Resume observing location
        enableLocationObserver();
        runPaused = false;
      } else {
        // Cease updating time
        scheduledFuture.cancel(false);

        // Update notification
        updateNotification(RUN_PAUSED);

        // Cease observing location
        removeLocationObserver();
        runPaused = true;
      }

      return runPaused;
    }

    public int getDistance() {
      return distance.intValue();
    }

    public int getTime() {
      return time;
    }

  }

  private void onRunStart() {
    // Time elapsed since run started
    time = 0;
    distance = 0.0;

    runCoordinates = new RunCoordinates();
    float x1 = (float) getLocationRepository().getLocation().getLatitude();
    float y1 = (float) getLocationRepository().getLocation().getLongitude();

    runCoordinates.addCoordinate(new Coordinate(x1, y1));
    currentLocation = getLocationRepository().getLocation();

    // Start time update thread
    timeUpdateTask = () -> {
      time++;
      Intent intent = new Intent(TIME_UPDATE_ACTION);
      Bundle bundle = new Bundle();
      bundle.putInt(getResources().getString(R.string.time), time);
      intent.putExtras(bundle);

      sendUpdateBroadcast(intent);
    };

    // Begin execution of worker thread
    timeScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    scheduledFuture =
        timeScheduledExecutorService.scheduleAtFixedRate(timeUpdateTask, 0, 1L, TimeUnit.SECONDS);

    enableLocationObserver();
  }

  private void onRunEnd(boolean shouldSave) {
    // Stop time updates
    timeScheduledExecutorService.shutdownNow();

    // Stop distance updates
    getLocationRepository().getLocationLiveData().removeObservers
        (this);

    /**
     * https://github.com/probelalkhan/android-room-database-example/blob/master/app/src/main/java/net/simplifiedcoding/mytodo/AddTaskActivity.java
     */
    class SaveRun extends AsyncTask<Void, Void, Void> {

      @Override
      protected Void doInBackground(Void... voids) {
        if (shouldSave) {
          temperature = getWeatherRepository().getTemperature();
          long date = System.currentTimeMillis();

          getRunRepository().createRun(distance, time, date, temperature, runCoordinates);
        }

        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (shouldSave) {
          // Asynchronously tell activity that run has been saved
          Intent intent = new Intent(RUN_END_ACTION);
          sendUpdateBroadcast(intent);
        }

        // Flush local run coordinates object
        runCoordinates = null;
      }
    }

    // Save run
    new SaveRun().execute();
    if (shouldSave) {
      Toast.makeText(getApplicationContext(), "Run saved", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(getApplicationContext(), "User deleted - run aborted", Toast.LENGTH_LONG)
          .show();
    }

  }

  private void startLocationThread() {
    try {
      /*
       The following requestLocationUpdates call will spin up a background thread which
       we can listen to by implementing the LocationEngineCallback interface
       */
      LocationEngineRequest locationEngineRequest = new LocationEngineRequest.Builder(
          getPropertyManager().getMinTime()).build();
      /*
      Similar to weather service, we pass the location repository as listener and allow it
      to update itself when callback occurs
      */
      getLocationRepository().getLocationEngine()
          .requestLocationUpdates(locationEngineRequest, getLocationRepository(), null);
    } catch (SecurityException e) {
      Log.e("Security exception: ", e.toString());
    }
  }

  private void enableLocationObserver() {
    // Observe the location repo for changes
    getLocationRepository().getLocationLiveData()
        .observe(this, location -> {
          // Set new coordinate
          float x2 = (float) location.getLatitude();
          float y2 = (float) location.getLongitude();
          runCoordinates.addCoordinate(new Coordinate(x2, y2));

          // Dynamically increases the distance covered rather than calculating distance between point A and point B
          distance += location.distanceTo(currentLocation);

          Intent intent = new Intent(DISTANCE_UPDATE_ACTION);
          Bundle bundle = new Bundle();
          bundle.putDouble(DISTANCE, distance);
          intent.putExtras(bundle);

          sendUpdateBroadcast(intent);

          currentLocation = location;
        });
  }

  private void removeLocationObserver() {
    // Stop distance updates
    getLocationRepository().getLocationLiveData().removeObservers
        (this);
  }

  private Notification generateNotification(String message) {
    NotificationManager notificationManager = (NotificationManager) getSystemService(
        NOTIFICATION_SERVICE);
    /*
    Create the NotificationChannel, but only on API 26+ because
    the NotificationChannel class is new and not in the support library
    */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "Channel name";
      String description = "channel description";
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      /*
      Register the channel with the system; you can't change the importance
      or other notification behaviors after this
      */
      notificationManager.createNotificationChannel(channel);
    }

    Intent intent = new Intent(this, RunActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    Intent actionIntent = new Intent(this, RunActivity.class);
    PendingIntent pendingActionIntent = PendingIntent.getService(this, 0, actionIntent, 0);

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(message)
        .setContentText("Return to DroidTracker")
        .setContentIntent(pendingIntent)
        .addAction(R.drawable.ic_launcher_foreground, "Message Service", pendingActionIntent)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    return mBuilder.build();
  }

  public void updateNotification(String newMessage) {
    Notification notification = generateNotification(newMessage);

    NotificationManager mNotificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(NOTIFICATION_ID, notification);
  }

  private void sendUpdateBroadcast(Intent intent) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
  }

}