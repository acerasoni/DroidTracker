package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
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

}
