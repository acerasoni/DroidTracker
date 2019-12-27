package com.ltm.runningtracker.android.contentprovider;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.database.AppDatabase;
import com.ltm.runningtracker.database.Diet;
import com.ltm.runningtracker.database.DietDao;
import com.ltm.runningtracker.database.Run;
import com.ltm.runningtracker.database.RunDao;
import com.ltm.runningtracker.database.User;
import com.ltm.runningtracker.database.UserDao;

public class DroidContentProvider extends ContentProvider {

  private AppDatabase appDatabase;
  private RunDao runDao;
  private UserDao userDao;
  private DietDao dietDao;

  @Override
  public boolean onCreate() {
    appDatabase = AppDatabase.getInstance(getContext());
    this.runDao = appDatabase.runDao();
    this.userDao = appDatabase.userDao();
    this.dietDao = appDatabase.dietDao();
    return true;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
      @Nullable String[] strings1, @Nullable String s1) {
    Cursor c;
    switch (ContentProviderContract.URI_MATCHER.match(uri)) {
      case 1:
        c = runDao.getFreezingRuns();
        break;
      case 2:
        c = runDao.getColdRuns();
        break;
      case 3:
        c = runDao.getMildRuns();
        break;
      case 4:
        c = runDao.getWarmRuns();
        break;
      case 5:
        c = runDao.getHotRuns();
        break;
      case 7:
        c = userDao.getUser();
        break;
      default:
        c = runDao.getAllRuns();
    }

    return c;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
    long id = -1;

    switch (ContentProviderContract.URI_MATCHER.match(uri)) {
      case 0:
        id = runDao
            .insert(
                new Run(contentValues.getAsString("weather"), contentValues.getAsString("duration"),
                    contentValues.getAsDouble("startLat"), contentValues.getAsDouble("startLon"),
                    contentValues.getAsDouble("endLat"), contentValues.getAsDouble("endLon"),
                    contentValues.getAsDouble("totalDistance"),
                    contentValues.getAsDouble("averageSpeed"))
            );
        break;
      case 7:
        User user = new User(contentValues.getAsString("name"), contentValues.getAsString("dietName"),
            contentValues.getAsFloat("bmi"));

        // Cache
        getUserRepository().setUserAsync(user);

        // Insert
        dietDao.insert(new Diet(user.getDietName()));
        id = userDao.insert(user);
        break;
    }

    Uri nu = ContentUris.withAppendedId(uri, id);
    Log.d("DroidContentProvider ", "onInsert: " + nu.toString());
    getContext().getContentResolver().notifyChange(nu, null);

    return nu;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    switch (ContentProviderContract.URI_MATCHER.match(uri)) {
      case 7:
        userDao.delete();
        break;
    }
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
      @Nullable String[] strings) {
    return 0;
  }

}
