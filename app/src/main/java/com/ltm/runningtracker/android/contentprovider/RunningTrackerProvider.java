package com.ltm.runningtracker.android.contentprovider;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.android.activity.MainActivity;
import com.ltm.runningtracker.database.AppDatabase;
import com.ltm.runningtracker.database.Run;
import com.ltm.runningtracker.database.RunDao;

public class RunningTrackerProvider extends ContentProvider {

  private AppDatabase appDatabase;
  private RunDao runDao;

  @Override
  public boolean onCreate() {
    appDatabase = AppDatabase.getInstance(getContext());
    this.runDao = appDatabase.runDao();
    return true;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
      @Nullable String[] strings1, @Nullable String s1) {

    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

    final long[] id = new long[1];

    id[0] = runDao
        .insert(
            new Run(contentValues.getAsString("weather"), contentValues.getAsLong("duration"),
                contentValues.getAsDouble("startLat"), contentValues.getAsDouble("startLon"),
                contentValues.getAsDouble("endLat"), contentValues.getAsDouble("endLon"),
                contentValues.getAsDouble("totalDistance"),
                contentValues.getAsDouble("averageSpeed"))
        );

    Uri nu = ContentUris.withAppendedId(uri, id[0]);
    Log.d("PsyagceProvider", "onInsert: " + nu.toString());
    getContext().getContentResolver().notifyChange(nu, null);

    return nu;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
      @Nullable String[] strings) {
    return 0;
  }

}
