package com.ltm.runningtracker.android.contentprovider;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.COLD_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.FREEZING_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.HOT_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.MILD_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.RUN_BY_ID;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.URI_MATCHER;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.USER;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.WARM_RUNS;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;
import com.ltm.runningtracker.database.AppDatabase;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.database.RunDao;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.database.UserDao;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class DroidContentProvider extends ContentProvider {

  private AppDatabase appDatabase;
  private RunDao runDao;
  private UserDao userDao;

  @Override
  public boolean onCreate() {
    appDatabase = AppDatabase.getInstance(getContext());
    this.runDao = appDatabase.runDao();
    this.userDao = appDatabase.userDao();
    return true;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
      @Nullable String[] strings1, @Nullable String s1) {
    Cursor c = null;
    switch (URI_MATCHER.match(uri)) {
      case RUNS:
        return runDao.getAll();
      case RUN_BY_ID:
        return runDao.getById(Integer.parseInt(uri.getLastPathSegment()));
      case FREEZING_RUNS:
      case COLD_RUNS:
      case MILD_RUNS:
      case WARM_RUNS:
      case HOT_RUNS:
        c = runDao.getByWeather(
            WeatherClassifier.valueOf(uri.getLastPathSegment().toUpperCase()).getValue());
        break;
      case USER:
        c = userDao.getUser();
        break;
    }

    return c;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
    Uri var = null;

    switch (URI_MATCHER.match(uri)) {
      case RUNS:
        long runId = runDao.insert(getParsedRunBuilder(contentValues).build());
        var = ContentUris.withAppendedId(uri, runId);
        break;
      case USER:
        User user = getParsedUserBuilder(contentValues).build();
        // Cache
        getUserRepository().setUserAsync(user);
        long userId = userDao.insert(user);
        var = ContentUris.withAppendedId(uri, userId);
        break;
    }

    Log.d("DroidContentProvider ", "onInsert: " + var.toString());

    // Notify change
    getContext().getContentResolver().notifyChange(var, null);

    return var;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    int numRowsDeleted = Integer.MIN_VALUE;

    switch (URI_MATCHER.match(uri)) {
      case RUNS:
        numRowsDeleted = runDao.delete();
        break;
      case FREEZING_RUNS:
      case COLD_RUNS:
      case MILD_RUNS:
      case WARM_RUNS:
      case HOT_RUNS:
        numRowsDeleted = runDao.deleteByWeather(
            WeatherClassifier.valueOf(uri.getLastPathSegment().toUpperCase()).getValue());
        break;
      case USER:
        numRowsDeleted = userDao.delete();
        break;
    }

    // Notify change
    getContext().getContentResolver().notifyChange(uri, null);

    return numRowsDeleted;
  }

  /**
   * This method updates records in the database. As runs are immutable, we will only ever need to
   * update the user, hence URI matching is not required.
   *
   * Another advantage of caching the user is that we can use it to update the DB without having to
   * pass individual values in the ContentValues;
   *
   * @return record _id
   */
  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
      @Nullable String[] strings) {

    // Notify cache user has changed
    getContext().getContentResolver().notifyChange(uri, null);

    return userDao.update(getUserRepository().getUser());
  }


  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    String contentType;

    if (uri.getLastPathSegment() == null) {
      contentType = DroidProviderContract.CONTENT_TYPE_MULTIPLE;
    } else {
      contentType = DroidProviderContract.CONTENT_TYPE_SINGLE;
    }

    return contentType;
  }

  /**
   * This method handles the absence of some values via builder pattern. This can be caused by
   * network issues (couldn't retrieve weather information), or lack of user input.
   */
  @Ignore
  private Run.Builder getParsedRunBuilder(ContentValues contentValues) {
    Run.Builder builder = new Run.Builder(contentValues.getAsLong("date"),
        contentValues.getAsDouble("distance"), contentValues.getAsLong("duration"));

    if (contentValues.containsKey("temperature")) {
      builder = builder.withTemperature(contentValues.getAsFloat("temperature"));
    }
    if (contentValues.containsKey("runCoordinates")) {
      builder = builder.withRunCoordinates(contentValues.getAsByteArray("runCoordinates"));
    }

    if (contentValues.containsKey("runType")) {
      builder = builder.withRunType(contentValues.getAsInteger("runType"));
    }

    return builder;
  }

  @Ignore
  private User.Builder getParsedUserBuilder(ContentValues contentValues) {
    User.Builder builder = new User.Builder(contentValues.getAsString("name"),
        contentValues.getAsInteger("weight"),
        contentValues.getAsInteger("height")).withWalkingPace(Float.MIN_VALUE)
        .withJoggingPace(Float.MIN_VALUE).withRunningPace(Float.MIN_VALUE).withSprintingPace(Float.MIN_VALUE);

    return builder;
  }

}
