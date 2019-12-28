package com.ltm.runningtracker.repository;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.database.Cursor;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class RunRepository {

  public RunRepository() {
  }

  //TODO Fetch runs from content provider and populate list

  public Cursor getAllRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.RUNS_URI, null, null, null, null, null);
  }

  public Cursor getFreezingRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.FREEZING_RUNS_URI, null, null, null, null, null);
  }

  public Cursor getColdRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.COLD_RUNS_URI, null, null, null, null, null);
  }

  public Cursor getMildRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.MILD_RUNS_URI, null, null, null, null, null);
  }

  public Cursor getWarmRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.WARM_RUNS_URI, null, null, null, null, null);
  }

  public Cursor getHotRuns() {
    return getApplicationContext().getContentResolver()
        .query(ContentProviderContract.HOT_RUNS_URI, null, null, null, null, null);
  }

}
