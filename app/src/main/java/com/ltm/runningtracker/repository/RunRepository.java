package com.ltm.runningtracker.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import java.util.ArrayList;
import java.util.List;

public class RunRepository {

  // Cursors associated with current run list returned by DB
  // Acts as short-living cache, as it is overritten by subsequent DB queries
  // Enum provides us with index of run types
  private List<MutableLiveData<Cursor>> runCursors;

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
  }

  // Called if cache exists
  public Cursor getRunsSync(WeatherClassifier weatherClassifier) {
    return runCursors.get(weatherClassifier.getValue()).getValue();
  }

  // Called if cache does not exist
  public Cursor getRunsAsync(Context context, WeatherClassifier weatherClassifier) {
    // ping the DB
    Uri uri = Uri
        .withAppendedPath(DroidProviderContract.RUNS_URI, "/" + weatherClassifier.toString());

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
