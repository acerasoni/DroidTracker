package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel.capitalizeFirstLetter;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.PACE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUN_TYPE;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TYPE_COL;
import static com.ltm.runningtracker.database.model.Run.getFormattedDate;
import static com.ltm.runningtracker.database.model.Run.getFormattedTime;
import static com.ltm.runningtracker.repository.LocationRepository.calculatePace;
import static com.ltm.runningtracker.util.Constants.RUN_ID;
import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;
import static com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier.*;
import static java.util.stream.Collectors.*;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.Serializer;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Repository responsible for storing and exposing a local cache of run objects. Additionally, it is
 * responsible of keeping its cache up-to-date and to flush/refresh it when and where appropriate.
 *
 * It does a best-effort attempt to retrieve and validate runsCache requested by ID and weather by
 * the ViewModel. 1. It checks the cache 2. If not found, it pings to database and utilises a short
 * living cache to communicate asynchronously with the views.
 */
public class RunRepository {

  /*
   Cache of runs. We use a Sorted Map which maps id -> Run because we want to
   retrieve runs by id in O(logn).
   */
  private SortedMap<Integer, Run> runsCache;
  private MutableLiveData<Run> shortLivingRunCache;

  @RequiresApi(api = VERSION_CODES.O)
  public RunRepository() {

    /*
    Instantiate map as TreeMap (implementation of SortedSet)
    Maintain the TreeSet sorted by ascending order of key (_id)
    "If multiple threads access a tree map concurrently, and at least one of the threads modifies the map, it must be synchronized externally."
    https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html
     */
    runsCache = Collections.synchronizedNavigableMap(new TreeMap<>((Object o1, Object o2) ->
        ((int) o1) - ((int) o2)));

    // Initialize the short living observable cache
    shortLivingRunCache = new MutableLiveData<>();
  }

  public LiveData<Run> getShortLivingRunCache() {
    return shortLivingRunCache;
  }

  /**
   * Synchronised read of cache run and subsequent query to database
   *
   * @return true if runs exist
   */
  public boolean doRunsExist(Context context) {
    if (runsCache.size() > 0) {
      return true;
    }

    /*
     We now have to check DB the db.
     Necessary as run cursors are cached only when looking at performance - could be empty

     We can use try-with-resource to automatically close the cursor.
     We return true if runs exist but there was an exception while reading cursor data.
     */
    boolean runsExist = false;
    try (Cursor c = context.getContentResolver().
        query(RUNS_URI, null, null, null, null)) {
      if (c.moveToFirst()) {
        runsExist = true;
        do {
          // Cache all runs returned for future reference
          Run run = Run.fromCursorToRun(c);
          runsCache.put(run._id, run);
        } while (c.moveToNext());
      }
    } finally {
      return runsExist;
    }
  }


  /**
   * Called asynchronously from a background thread in Location Service
   *
   * @see com.ltm.runningtracker.android.service.LocationService
   */
  public void createRun(Double distance, int time, long date, float temperature,
      RunCoordinates runCoordinates) {
    // Build run
    Run run = getParsedRunBuilder(distance, time, date, temperature, runCoordinates).build();

    // Serialize and send to Content Provider
    ContentValues contentValues = new ContentValues();
    contentValues.put(RUN_ID, Serializer.toByteArray(run));

    getAppContext().getContentResolver().insert(DroidProviderContract.RUNS_URI, contentValues);

    // Flush cache
    flushCache();
  }

  /**
   * Asynchronously delete all runs in the database
   */
  @RequiresApi(api = VERSION_CODES.O)
  public void deleteRuns(Context context) {
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(RUNS_URI, null, null);
      getRunRepository().flushCache();
    });
  }

  public synchronized void populateShortLivingCache(Run run) {
    shortLivingRunCache.postValue(run);
  }

  /**
   * Resets the long-living cache on all weather-specific cached cursors.
   */
  public synchronized void flushCache() {
    // delete long-living cache
    runsCache.clear();

    // delete short-living cache
    shortLivingRunCache.postValue(null);
  }

  public List<Run> getAllRuns(Context context) {
    return getRunsAsync(context, RUNS_URI);
  }

  /**
   * Gets all runs stored in the database. Must be called asynchronously.
   *
   * @return List<Run> of all runs in the database
   */
  public synchronized List<Run> getRunsAsync(Context context, Uri uri) {
    List<Run> returnedList = new ArrayList<>();

    /*
    Try-with-resource allows to return a partially populated list if an exception occurs
    while reading cursor data. It also allows to guarantee closing the cursor.
     */
    try (Cursor c = context.getContentResolver()
        .query(uri, null, null, null, null, null)) {
      if (c.moveToFirst()) {
        do {
          Run run = Run.fromCursorToRun(c);
          returnedList.add(run);
          // Cache run. Set will ignore duplicates.
          runsCache.put(run._id, run);
        } while (c.moveToNext());
      }
    } finally {
      return returnedList;
    }
  }

  /*
   Called if cache exists
   Only useful to check if runs of certain weather type exist
   Return as List to allow for more graceful iteration in ViewModel
   */
  public List<Run> getRunsSyncByWeather(WeatherClassifier weatherClassifier) {
    Predicate<Run> byWeatherType = run -> WeatherClassifier
        .valueOf(run.weatherType).equals(weatherClassifier);

    List<Run> result = runsCache.values().stream().filter(byWeatherType)
        .collect(toList());

    return result;
  }

  /*
   Called if cache does not exist
   List because needs to be an ordered for custom ListViewAdapter
   */
  public List<Run> getRunsAsyncByWeather(Context context, WeatherClassifier weatherClassifier) {
    // Ping the DB
    Uri uri = Uri
        .withAppendedPath(RUNS_URI, weatherClassifier.toString());
    return getRunsAsync(context, uri);
  }

  /**
   * Gets run by id. If not found, makes async call to DB which updates short living cache.
   *
   * @return run if retrieved, null if cache empty
   */
  @RequiresApi(api = VERSION_CODES.O)
  public Run getRunById(int id, Context context) {
    /*
     Check cache
     Operation is O(logn)
     */
    if (runsCache.containsKey(id)) {
      return runsCache.get(id);
    }

    // If not found in cache, ping DB async
    getRunByIdAsync(id, context);
    return null;
  }

  /**
   * Fetches run by id asynchronously. If found, populates the short living cache.
   *
   * @param id of run
   * @param context with which to make database call
   */
  @RequiresApi(api = VERSION_CODES.O)
  @SuppressLint("DefaultLocale")
  public void getRunByIdAsync(int id, Context context) {
    Uri customUri = Uri.parse(RUNS_URI.toString() + "/" + id);
    try (Cursor c = context.getContentResolver()
        .query(customUri, null, null, null, null)) {
      if (c.moveToFirst()) {
        Run run = Run.fromCursorToRun(c);

        // Cache
        runsCache.put(run._id, run);

        // Set short living cache to cursor
        getRunRepository().populateShortLivingCache(run);
      }
    }
  }

  public void deleteRun(WeatherClassifier weatherClassifier, Context context, int id) {
    // Flush cache
    flushCache();

    // DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + id);
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(uri, null, null);

      // Refresh cache
      getRunsAsync(context, RUNS_URI);
    });
  }

  public void updateTypeOfRun(int id, int pos, Context context) {
    // Flush cache
    flushCache();

    // Update db
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + id);
    ContentValues contentValues = new ContentValues();
    String newType = capitalizeFirstLetter(valueOf(pos).toString());
    contentValues.put(RUN_TYPE, newType);
    AsyncTask.execute(() -> context.getContentResolver().update(uri, contentValues, null, null));
  }

  /**
   * @param uri in the form of RUNS_URI/weatherClassifier
   * @param context with which to query DB
   */
  public void deleteRunsByType(Uri uri, Context context) {
    // Flush cache
    flushCache();

    AsyncTask.execute(() -> {
      // Update DB
      context.getContentResolver().delete(uri, null, null);
      ((AppCompatActivity) context).finish();
    });
  }

  /**
   * Calculates the average pace for all tagged runsCache Must request all runs from the DB as cache
   * might be empty (populated only when visiting the Performance tab
   *
   * @param context with which to query the database.
   * @return EnumMap mapping RunTypeClassifier -> Average Pace
   */
  public EnumMap<RunTypeClassifier, Float> calculatateAveragePaces(Context context) {
    Float walkingPace = null;
    Float joggingPace = null;
    Float runningPace = null;
    Float sprintingPace = null;

    // Retrieve all runs
    try (Cursor c = context.getContentResolver()
        .query(RUNS_URI, null, null, null, null)) {
      if (c.moveToFirst()) {
        do {
          switch (valueOf(c.getString(TYPE_COL).toUpperCase())) {
            case UNTAGGED:
              break;
            case WALK:
              if (walkingPace == null) {
                walkingPace = c.getFloat(PACE_COL);
              }
              walkingPace = calculateAverage(walkingPace, c.getFloat(PACE_COL));
              break;
            case JOG:
              if (joggingPace == null) {
                joggingPace = c.getFloat(PACE_COL);
              }
              joggingPace = calculateAverage(joggingPace, c.getFloat(PACE_COL));
              break;
            case RUN:
              if (runningPace == null) {
                runningPace = c.getFloat(PACE_COL);
              }
              runningPace = calculateAverage(runningPace, c.getFloat(PACE_COL));
              break;
            case SPRINT:
              if (sprintingPace == null) {
                sprintingPace = c.getFloat(PACE_COL);
              }
              sprintingPace = calculateAverage(sprintingPace, c.getFloat(PACE_COL));
              break;

            default:
              throw new IllegalStateException(
                  UNEXPECTED_VALUE + valueOf(c.getString(TYPE_COL).toUpperCase()));
          }
        } while (c.moveToNext());
      }
    }

  /*
  Enumerator map more appropriate for enum-type keys. Enum maps are internally
  represented as arrays, and are extremely compact and efficient.
  */
  EnumMap<RunTypeClassifier, Float> returnedMap = new EnumMap<>(RunTypeClassifier.class);
    returnedMap.put(WALK,walkingPace);
    returnedMap.put(JOG,joggingPace);
    returnedMap.put(RUN,runningPace);
    returnedMap.put(SPRINT,sprintingPace);

    return returnedMap;
}

  private Float calculateAverage(float a, float b) {
    return (a + b) / 2;
  }

  /**
   * This method handles the absence of some values via builder pattern. Specifically, it will
   * utilise the builder pattern to insert a new Run. By not specifying the _id to the builder
   * pattern, the new object will have an autogenerated id, which is what we need.
   *
   * @return Run.Builder object able to construct a Run object on-the-fly
   */
  private Run.Builder getParsedRunBuilder(Double distance, int time, long date, float temperature,
      RunCoordinates runCoordinates) {
    return new Run.Builder(getFormattedDate(date), distance, getFormattedTime(time),
        calculatePace(distance, time))
        .withTemperature(temperature).withRunCoordinates(runCoordinates)
        .withRunType(ActivityViewModel
            .capitalizeFirstLetter(UNTAGGED.toString()));
  }

}