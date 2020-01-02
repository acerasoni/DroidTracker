package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;
import static com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel.capitalizeFirstLetter;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TYPE;
import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import java.util.ArrayList;
import java.util.List;

public class RunRepository {

  // Associated with retrieving runs
  // Cursors associated with current run list returned by DB
  // Acts as short-living cache, as it is overritten by subsequent DB queries
  // Enum provides us with index of run types
  private List<MutableLiveData<Cursor>> runCursors;
  private MutableLiveData<Cursor> shortLivingCache;

  public RunRepository() {
    // Cache empty
    runCursors = new ArrayList<MutableLiveData<Cursor>>() {
      {
        // Initialise list with number of cursors equivalent to the number of weather types
        for (int x = 0; x < WeatherClassifier.getNum(); x++) {
          add(new MutableLiveData<>());
        }
      }
    };

    shortLivingCache = new MutableLiveData<>();
  }

  public List<LiveData<Cursor>> getRunCursorsLiveData() {
    // Cast to list of LiveData cursors
    List<LiveData<Cursor>> list = new ArrayList<>(runCursors.size());
    list.add(runCursors.get(0));
    list.add(runCursors.get(1));
    list.add(runCursors.get(2));
    list.add(runCursors.get(3));
    list.add(runCursors.get(4));
    return list;
  }

  public LiveData<Cursor> getShortLivingCache() {
    return shortLivingCache;
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

  public void deleteRuns(Context context) {
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(RUNS_URI, null, null);
      getRunRepository().flushCache();
    });
  }

  public boolean doRunsExistByWeather(Context context, WeatherClassifier weatherClassifier) {
    Cursor c = getRunsSync(weatherClassifier);

    if (c != null && c.moveToFirst()) {
      return true;
    } else {
      c = getRunsAsync(context, weatherClassifier);
      if (c != null && c.moveToFirst()) {
        return true;
      } else {
        return false;
      }
    }
  }

  public void populateShortLivingCache(Cursor c) {
    shortLivingCache.postValue(c);
  }

  public void flushCacheByWeather(WeatherClassifier weatherClassifier) {
    runCursors.get(weatherClassifier.getValue()).postValue(null);
    if (shortLivingCache != null) {
      shortLivingCache.postValue(null);
    }
  }

  public void flushCache() {
    for (WeatherClassifier wc : WeatherClassifier.values()) {
      flushCacheByWeather(wc);
    }
  }

  // Called if cache exists
  public Cursor getRunsSync(WeatherClassifier weatherClassifier) {
    return runCursors.get(weatherClassifier.getValue()).getValue();
  }

  // Called if cache does not exist
  public Cursor getRunsAsync(Context context, WeatherClassifier weatherClassifier) {
    // ping the DB
    Uri uri = Uri
        .withAppendedPath(RUNS_URI, "/" + weatherClassifier.toString());

    Cursor c;
    // Post value rather than set value -> this method is called asynchronously from fragments
    c = context.getContentResolver()
        .query(uri, null, null, null, null, null);

    // Cache async
    runCursors.get(weatherClassifier.getValue()).postValue(c);

    // Return
    return c;
  }

  @SuppressLint("DefaultLocale")
  public void fetchRunAsyncById(int id, Context context) {
    Uri customUri = Uri.parse(RUNS_URI.toString() + "/" + id);
    Cursor c = context.getContentResolver().query(customUri, null, null, null, null);

    if (c != null) {
      // Move cursor to position
      c.moveToFirst();

      // Set short living cache to cursor
      getRunRepository().populateShortLivingCache(c);
    }

  }

  public void deleteRun(WeatherClassifier weatherClassifier, Context context, int id) {
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

  public void updateTypeOfRun(int id, int pos, Context context) {
    //UPDATE DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + id);
    ContentValues contentValues = new ContentValues();
    String newType = capitalizeFirstLetter(RunTypeClassifier.valueOf(pos).toString());
    contentValues.put(TYPE, newType);
    AsyncTask.execute(() -> context.getContentResolver().update(uri, contentValues, null, null));
  }

  public void deleteRunsByType(Uri uri, Context context, WeatherClassifier weatherClassifier) {
    // Update cache
    getRunRepository().flushCacheByWeather(weatherClassifier);

    AsyncTask.execute(() -> {
      // Update DB
      context.getContentResolver().delete(uri, null, null);
      ((AppCompatActivity) context).finish();
    });
  }

  public Float[] calculatateAveragePaces(Context context) {
    Float walkingPace = null;
    Float joggingPace = null;
    Float runningPace = null;
    Float sprintingPace = null;

    Cursor c = context.getContentResolver().query(RUNS_URI, null, null, null, null);
    if (c != null && c.moveToFirst()) {
      do {
        switch (RunTypeClassifier.valueOf(c.getString(3).toUpperCase())) {
          case UNTAGGED:
            break;
          case WALK:
            if (walkingPace == null) {
              walkingPace = c.getFloat(9);
            }
            walkingPace = calculateAverage(walkingPace, c.getFloat(9));
            break;
          case JOG:
            if (joggingPace == null) {
              joggingPace = c.getFloat(9);
            }
            joggingPace = calculateAverage(joggingPace, c.getFloat(9));
            break;
          case RUN:
            if (runningPace == null) {
              runningPace = c.getFloat(9);
            }
            runningPace = calculateAverage(runningPace, c.getFloat(9));
            break;
          case SPRINT:
            if (sprintingPace == null) {
              sprintingPace = c.getFloat(9);
            }
            sprintingPace = calculateAverage(sprintingPace, c.getFloat(9));
            break;

          default:
            throw new IllegalStateException(
                UNEXPECTED_VALUE + RunTypeClassifier.valueOf(c.getString(3).toUpperCase()));
        }
      } while (c.moveToNext());
    }

    return new Float[]{walkingPace, joggingPace, runningPace, sprintingPace};
  }

  private Float calculateAverage(float a, float b) {
    return (a + b) / 2;
  }

}
