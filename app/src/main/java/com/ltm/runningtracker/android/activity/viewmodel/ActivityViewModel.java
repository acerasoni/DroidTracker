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
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.Serializer;
import com.ltm.runningtracker.util.annotations.Presenter;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import com.mapbox.android.core.location.LocationEngine;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.List;

/**
 * This coursework implements the Model-View-ViewModel pattern. This class acts as the View Model in
 * this pattern. It does so by exposing LiveData repositories which Activities can observe.
 * Activities can then react to changes made to the Model objects.
 *
 * This coursework implements the Model-View-Presenter pattern. In addition to being a ViewModel as
 * outlined above, this class also acts as the presenter in this pattern. Methods tagged with
 *
 * @observer are used by Activities to directly retrieve data. This tag requires to specify which
 * Activities access the method, and which repositories the methods depends on. The presenter
 * ultimately determines where the data comes from (cache or asynchronous call to the Database), and
 * in what form.
 *
 * There are three available use cases in this class.
 *
 * 1. Views observe LiveData objects and react to changes made to them. For example, a TextView can
 * observe the location's LiveData object and call .setText(location.getLatitude()). The
 * Model-View-ViewModel pattern is enough for this use case.
 *
 * 2. Views request data stored in the Model without the Model changing. Data is fetched from cache,
 * validated and returned. The Data retrieval on the UI thread is made synchronously and through
 * inspecting the cache. This use case requires the Model-View-Presenter.
 *
 * 3. Views request data stored in the Model without the Model changing. Data is not found in the
 * cache. An asynchronous call to the database is made in the background, and the value is cached.
 * The View is then setup to observe the cache and update it's UI when cache is populated. This use
 * case requires both Model-View-ViewModel, and Model-View-Presenter patterns.
 */
public class ActivityViewModel extends ViewModel {

  // Repositories
  private LocationRepository locationRepository;
  private RunRepository runRepository;
  private UserRepository userRepository;
  private WeatherRepository weatherRepository;

  // LiveData objects
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

  // <-- Model-View-ViewModel exposure of LiveData objects -->

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

  // <-- Implementation of Model-View-Presenter pattern -->

  @Presenter(usedBy = {RunActivity.class}, repositoriesAccessed = {
      LocationRepository.class})
  public LocationEngine getLocationEngine() {
    return locationRepository.getLocationEngine();
  }

  @Presenter(usedBy = {UserProfileActivity.class}, repositoriesAccessed = {RunRepository.class})
  public Float[] getUserAveragePaces(Context context) {
    return runRepository.calculatateAveragePaces(context);
  }

  /**
   * @param id of the requested run
   * @param weatherClassifier of the run, for optimisation purposes
   * @param context for the asynchronous call to the database if the cache was empty
   * @return Cursor with one row which contains the run requested.
   */
  @Presenter(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
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

  @Presenter(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public RunCoordinates getRunCoordinates(WeatherClassifier weatherClassifier, int id) {
    // Look in cache. Guaranteed to have it, because call is made from inside the BrowseRunDetailsActivity,
    // which means that run was previously located in cache by id.
    Cursor c = getCachedRunById(weatherClassifier, id);
    return Serializer.runCoordinatesFromByteArray(c.getBlob(8));
  }

  /**
   * @param context with which database query is made
   * @return Cursor containing all the runs of the given weatherClassifier type
   */
  @Presenter(usedBy = {PerformanceFragment.class}, repositoriesAccessed = {RunRepository.class})
  public Cursor getRunsByWeather(WeatherClassifier weatherClassifier, Context context) {
    return runRepository.getRunsAsync(context, weatherClassifier);
  }

  @Presenter(usedBy = {MainScreenActivity.class, SettingsActivity.class}, repositoriesAccessed = {
      UserRepository.class})
  public boolean doesUserExist() {
    return userRepository.getUser() != null;
  }

  @Presenter(usedBy = {MainScreenActivity.class,
      PerformanceFragment.class}, repositoriesAccessed = {RunRepository.class})
  public boolean doRunsExist(Context context) {
    return runRepository.doRunsExist(context);
  }

  /**
   * Queries the database to determine if at least one run of each given type exists. The returned
   * array is defined as follows [0] is true if runs in freezing weather exist [1] is true if runs
   * in cold weather exist [2] is true if runs in mild weather exist [3] is true if runs in warm
   * weather exist [4] is true if runs in hot weather exist [5] is true if runs of any type exist
   *
   * @param context with which database query is made
   * @return boolean[] containing result of the queries outlined above
   */
  @Presenter(usedBy = {SettingsActivity.class}, repositoriesAccessed = {
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

  @Presenter(usedBy = {UserProfileActivity.class}, repositoriesAccessed = {
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

  @Presenter(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public void updateTypeOfRun(int id, int pos, Context context) {
    runRepository.updateTypeOfRun(id, pos, context);
  }

  @Presenter(usedBy = {SettingsActivity.class}, repositoriesAccessed = {UserRepository.class})
  public void deleteUser(Context context) {
    userRepository.deleteUser(context);
  }

  @Presenter(usedBy = {SettingsActivity.class}, repositoriesAccessed = {RunRepository.class})
  public void deleteRuns(Context context) {
    runRepository.deleteRuns(context);
  }

  @Presenter(usedBy = {BrowseRunDetailsActivity.class}, repositoriesAccessed = {
      RunRepository.class})
  public void deleteRun(WeatherClassifier weatherClassifier, Context context, int id) {
    runRepository.deleteRun(weatherClassifier, context, id);
  }

  @Presenter(usedBy = {SettingsActivity.class}, repositoriesAccessed = {RunRepository.class})
  public void deleteRunsByType(Uri uri, Context context, WeatherClassifier weatherClassifier) {
    runRepository.deleteRunsByType(uri, context, weatherClassifier);
  }

  @Presenter(usedBy = {MainScreenActivity.class}, repositoriesAccessed = {
      LocationRepository.class, UserRepository.class, WeatherRepository.class, RunRepository.class})
  public void initRepos() {
    getPropertyManager();
    getLocationRepository();
    getUserRepository();
    getWeatherRepository();
    getRunRepository();
  }

  // <-- Util functions -->

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

  private Cursor getCachedRunById(WeatherClassifier weatherClassifier, int id) {
    Cursor c = runCursors.get(weatherClassifier.getValue()).getValue();
    c.moveToFirst();
    do {
      if (c.getInt(0) == id) {
        break;
      }
    } while (c.moveToNext());
    return c;
  }

}
