package com.ltm.runningtracker.repository;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;
import com.ltm.runningtracker.database.RunDao;
import com.ltm.runningtracker.exception.RunNotFoundException;
import com.ltm.runningtracker.database.Run;
import java.util.ArrayList;
import java.util.List;

public class RunRepository {

  // Runs are immutable - no need to store them in LiveData, as there are no changes to be observed in the Run objects
  private List<Run> runs;
  private RunDao runDao;
  private Cursor runCursor;

  public RunRepository() {
    runs = new ArrayList<>();

    AsyncTask.execute( new Runnable() {
      @Override
      public void run() {
        runCursor = getApplicationContext().getContentResolver()
            .query(ContentProviderContract.RUNS_URI, null, null, null, null, null);


//        if (c.moveToFirst()) {
//          do {
//            Log.d("Type", "" + c.getType(0));
//
//          } while (c.moveToNext());
//
//        }
      }
    });

  }

  //TODO Fetch runs from content provider and populate list


  public Cursor getRunCursor() {
    return runCursor;
  }
  public List<Run> getRuns() {
    return new ArrayList<>(runs);
  }

  public Run getRun(int index) throws RunNotFoundException {
    try {
      return runs.get(index);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new RunNotFoundException("Run not found.");
    }
  }
}
