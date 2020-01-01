package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import com.mapbox.android.core.location.LocationEngine;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.List;

public class ActivityViewModel extends ViewModel {

  private LiveData<Location> locationLiveData;
  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;
  private LiveData<String> countyLiveData;
  private LiveData<Cursor> shortLivingCache;
  private List<LiveData<Cursor>> runCursors;

  public ActivityViewModel() {
    locationLiveData = getLocationRepository().getLocationLiveData();
    userLiveData = getUserRepository().getUserLiveData();
    weatherLiveData = getWeatherRepository().getLiveDataWeather();
    countyLiveData = getLocationRepository().getCountyLiveData();
    shortLivingCache = getRunRepository().getShortLivingCache();
    runCursors = getRunRepository().getRunCursorsLiveData();
  }

  public boolean doRunsExist(Context context) {
    Cursor c;

    // Check cache
    for (LiveData m : runCursors) {
      // Iterate all cached cursors to check there aren't any cached runs
      c = ((Cursor) m.getValue());
      if (c != null && c.moveToFirst()) {
        return true;
      }
    }

    // Check DB
    // Necessary as run cursors are cached only when looking at performance
    c = context.getContentResolver().query(RUNS_URI, null, null, null, null);
    if (c != null && c.moveToFirst()) {
      return true;
    }

    return false;
  }

  public boolean doesUserExist() {
    return userLiveData.getValue() != null;
  }

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

  public LiveData<String> getCounty() {
    return countyLiveData;
  }

  public LiveData<User> getUser() {
    return userLiveData;
  }

  public LiveData<Cursor> getShortLivingCache() {
    return shortLivingCache;
  }

  public LiveData<Cursor> getRunCursorByWeather(WeatherClassifier weatherClassifier) {
    return runCursors.get(weatherClassifier.getValue());
  }

  public LocationEngine getLocationEngine() {
    return getLocationRepository().getLocationEngine();
  }

  public void initRepos() {
    getPropertyManager();
    getLocationRepository();
    getUserRepository();
    getWeatherRepository();
    getRunRepository();
  }

  public void deleteUser(Context context) {
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(USER_URI, null, null);
      getRunRepository().flushCache();
      getUserRepository().flushCache();
    });
  }

  public void determineIfRunsExist(Context context, Boolean doRunsExist, Boolean doFreezing,
      Boolean doCold, Boolean doMild, Boolean doWarm, Boolean doHot) {
    doFreezing = getRunRepository().doRunsExistByWeather(context, WeatherClassifier.FREEZING);
    doCold = getRunRepository().doRunsExistByWeather(context, WeatherClassifier.COLD);
    doMild = getRunRepository().doRunsExistByWeather(context, WeatherClassifier.MILD);
    doWarm = getRunRepository().doRunsExistByWeather(context, WeatherClassifier.WARM);
    doHot = getRunRepository().doRunsExistByWeather(context, WeatherClassifier.HOT);
    doRunsExist = doFreezing || doCold || doMild || doWarm || doHot;
  }

  // Given an id, check cached cursor for weather type or DB
  // Return cursor moved to the correct position
  public Cursor requestRun(int id, WeatherClassifier weatherClassifier, Context context) {
    // Check if cached has the row we need
    // Start by getting all runs associated with the weather
    Cursor c = getRunRepository().getRunsSync(weatherClassifier);
    if (c != null) {
      // Search for our ID in cursor
      c.moveToFirst();
      for (int x = 0; x < c.getCount(); x++) {
        if (c.getInt(0) == id) {
          // Correct row has been found
          c.moveToPosition(x);
          getRunRepository().populateShortLivingCache(c);
        }
      }
    }

    // If cache is empty query DB asynchronously
    AsyncTask.execute(() -> fetchDataAsync(id, context));

    return null;
  }

  @SuppressLint("DefaultLocale")
  private void fetchDataAsync(int id, Context context) {
    Uri customUri = Uri.parse(RUNS_URI.toString() + "/" + id);
    Cursor c = context.getContentResolver().query(customUri, null, null, null, null);

    if (c != null) {
      // Move cursor to position
      c.moveToFirst();

      // Set short living cache to cursor
      getRunRepository().populateShortLivingCache(c);
    }

  }

  public void onDelete(WeatherClassifier weatherClassifier, Context context, int id) {
    // Flush cache
    getRunRepository().flushCacheByWeather(weatherClassifier);

    // DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + id);
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(uri, null, null);

      // Refresh cache
      getRunRepository().getRunsAsync(context, weatherClassifier);
    });
  }

  /**
   * No need to update cache as the column has changed but row stayed the same
   */
  public void updateTypeOfRun(int id, int pos, Context context) {
    //UPDATE DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + id);
    ContentValues contentValues = new ContentValues();
    String newType = capitalizeFirstLetter(RunTypeClassifier.valueOf(pos).toString());
    contentValues.put("type", newType);
    AsyncTask.execute(() -> context.getContentResolver().update(uri, contentValues, null, null));
  }


  public static String capitalizeFirstLetter(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }

  public void deleteAllRunsByType(Uri uri, Context context, WeatherClassifier weatherClassifier) {
    // Update cache
    getRunRepository().flushCacheByWeather(weatherClassifier);

    AsyncTask.execute(() -> {
      // Update DB
      context.getContentResolver().delete(uri, null, null);
      ((AppCompatActivity) context).finish();
    });
  }

}
