package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getPropertyManager;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.android.activity.BrowseRunDetailsActivity;
import com.ltm.runningtracker.android.activity.MainScreenActivity;
import com.ltm.runningtracker.android.activity.RunActivity;
import com.ltm.runningtracker.android.activity.SettingsActivity;
import com.ltm.runningtracker.android.activity.UserProfileActivity;
import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.repository.RunRepository;
import com.ltm.runningtracker.repository.UserRepository;
import com.ltm.runningtracker.repository.WeatherRepository;
import com.ltm.runningtracker.util.annotations.Controller;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import com.mapbox.android.core.location.LocationEngine;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.List;

public class ActivityViewModel extends ViewModel {

  private LocationRepository locationRepository;
  private RunRepository runRepository;
  private UserRepository userRepository;
  private WeatherRepository weatherRepository;

  private LiveData<Location> locationLiveData;
  private LiveData<String> countyLiveData;
  private LiveData<Cursor> shortLivingCache;
  private List<LiveData<Cursor>> runCursors;
  private LiveData<User> userLiveData;
  private LiveData<Weather> weatherLiveData;

  public ActivityViewModel() {
    locationRepository = getLocationRepository();
    runRepository = getRunRepository();
    userRepository = getUserRepository();
    weatherRepository = getWeatherRepository();

    locationLiveData = locationRepository.getLocationLiveData();
    countyLiveData = locationRepository.getCountyLiveData();
    shortLivingCache = runRepository.getShortLivingCache();
    runCursors = runRepository.getRunCursorsLiveData();
    userLiveData = userRepository.getUserLiveData();
    weatherLiveData = weatherRepository.getLiveDataWeather();
  }

  /*
   * MVVM exposure of live data objects
   */

  public LiveData<Location> getLocation() {
    return locationLiveData;
  }

  public LiveData<String> getCounty() {
    return countyLiveData;
  }

  public LiveData<Cursor> getShortLivingCache() {
    return shortLivingCache;
  }

  public LiveData<Cursor> getRunCursorByWeather(WeatherClassifier weatherClassifier) {
    return runCursors.get(weatherClassifier.getValue());
  }

  public LiveData<User> getUser() {
    return userLiveData;
  }

  public LiveData<Weather> getWeather() {
    return weatherLiveData;
  }

  /*
   *        MVC
   */

  @Controller(usedBy = {RunActivity.class}, repositoriesAccessed = {
      LocationRepository.class})
  public LocationEngine getLocationEngine() {
    return locationRepository.getLocationEngine();
  }

  @Controller(usedBy = {UserProfileActivity.class}, repositoriesAccessed = {RunRepository.class})
  public Float[] getUserAveragePaces(Context context) {
    return runRepository.calculatateAveragePaces(context);
  }

  /**
   * Given an id, check cached cursor for weather type or DB Return cursor moved to the correct
   * position
   */
  @Controller(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public Cursor getRunById(int id, WeatherClassifier weatherClassifier, Context context) {
    // Check if cached has the row we need
    // Start by getting all runs associated with the weather
    Cursor c = getRunByWeather(weatherClassifier);
    if (c != null) {
      // Search for our ID in cursor
      c.moveToFirst();
      for (int x = 0; x < c.getCount(); x++) {
        if (c.getInt(0) == id) {
          // Correct row has been found
          c.moveToPosition(x);
          runRepository.populateShortLivingCache(c);
        }
      }
    }

    // If cache is empty query DB asynchronously
    AsyncTask.execute(() -> getRunById(id, context));

    return null;
  }

  @Controller(usedBy = {PerformanceFragment.class}, repositoriesAccessed = {RunRepository.class})
  public Cursor getRunsByWeather(WeatherClassifier weatherClassifier, Context context) {
    return runRepository.getRunsAsync(context, weatherClassifier);
  }

  @Controller(usedBy = {MainScreenActivity.class, SettingsActivity.class}, repositoriesAccessed = {
      UserRepository.class})
  public boolean doesUserExist() {
    return userRepository.getUser() != null;
  }

  @Controller(usedBy = {MainScreenActivity.class,
      PerformanceFragment.class}, repositoriesAccessed = {RunRepository.class})
  public boolean doRunsExist(Context context) {
    return runRepository.doRunsExist(context);
  }

  @Controller(usedBy = {SettingsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public boolean[] determineWhichRunTypesExist(Context context) {
    boolean[] runsExist = new boolean[6];
    runsExist[0] = runRepository.doRunsExistByWeather(context, WeatherClassifier.FREEZING);
    runsExist[1] = runRepository.doRunsExistByWeather(context, WeatherClassifier.COLD);
    runsExist[2] = runRepository.doRunsExistByWeather(context, WeatherClassifier.MILD);
    runsExist[3] = runRepository.doRunsExistByWeather(context, WeatherClassifier.WARM);
    runsExist[4] = runRepository.doRunsExistByWeather(context, WeatherClassifier.HOT);
    runsExist[5] = runsExist[0] || runsExist[1] || runsExist[2] || runsExist[3] || runsExist[4];
    return runsExist;
  }

  @Controller(usedBy = {UserProfileActivity.class}, repositoriesAccessed = {
      UserRepository.class})
  public void saveUser(Context context, boolean creatingUser, String name, String weight,
      String height) {
    if (creatingUser) {
      userRepository
          .createUser(name.trim(), Integer.parseInt(weight.trim()),
              Integer.parseInt(height.trim()), context);
    } else {
      userRepository.updateUser(name.trim(), Integer.parseInt(weight.trim()),
          Integer.parseInt(height.trim()), context);
    }
  }

  /**
   * No need to update cache as the column has changed but row stayed the same
   */
  @Controller(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public void updateTypeOfRun(int id, int pos, Context context) {
    runRepository.updateTypeOfRun(id, pos, context);
  }

  @Controller(usedBy = {SettingsActivity.class}, repositoriesAccessed = {UserRepository.class})
  public void deleteUser(Context context) {
    userRepository.deleteUser(context);
  }

  @Controller(usedBy = {SettingsActivity.class}, repositoriesAccessed = {RunRepository.class})
  public void deleteRuns(Context context) {
    runRepository.deleteRuns(context);
  }

  @Controller(usedBy = {SettingsActivity.class}, repositoriesAccessed = {RunRepository.class})
  public void deleteRunsByType(Uri uri, Context context, WeatherClassifier weatherClassifier) {
    runRepository.deleteRunsByType(uri, context, weatherClassifier);
  }

  @Controller(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public void deleteRun(WeatherClassifier weatherClassifier, Context context, int id) {
    runRepository.deleteRun(weatherClassifier, context, id);
  }

  @Controller(usedBy = {MainScreenActivity.class}, repositoriesAccessed = {
      LocationRepository.class, UserRepository.class, WeatherRepository.class, RunRepository.class})
  public void initRepos() {
    getPropertyManager();
    getLocationRepository();
    getUserRepository();
    getWeatherRepository();
    getRunRepository();
  }

  public static String capitalizeFirstLetter(String original) {
    if (original == null || original.length() == 0) {
      return original;
    }
    return original.substring(0, 1).toUpperCase() + original.substring(1);
  }

  private Cursor getRunByWeather(WeatherClassifier weatherClassifier) {
    return runRepository.getRunsSync(weatherClassifier);
  }

  private void getRunById(int id, Context context) {
    runRepository.fetchRunAsyncById(id, context);
  }

}
