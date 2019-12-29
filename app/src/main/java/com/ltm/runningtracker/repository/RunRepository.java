package com.ltm.runningtracker.repository;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.database.Cursor;
import android.net.Uri;
import com.ltm.runningtracker.android.contentprovider.DroidProviderContract;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class RunRepository {

  public RunRepository() {
  }

  //TODO Fetch runs from content provider and populate list

  public Cursor getRuns(WeatherClassifier weatherClassifier) {
    Uri uri = Uri.withAppendedPath(DroidProviderContract.RUNS_URI, "/" + weatherClassifier.toString());
    return getApplicationContext().getContentResolver()
        .query(uri, null, null, null, null, null);
  }

}
