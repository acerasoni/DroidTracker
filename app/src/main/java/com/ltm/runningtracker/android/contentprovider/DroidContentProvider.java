package com.ltm.runningtracker.android.contentprovider;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUN_TYPE;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.COLD_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.FREEZING_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.HOT_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.MILD_RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.RUNS;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.RUN_BY_ID;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.URI_MATCHER;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.USER;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.WARM_RUNS;
import static com.ltm.runningtracker.util.Constants.DATE;
import static com.ltm.runningtracker.util.Constants.DISTANCE;
import static com.ltm.runningtracker.util.Constants.DURATION;
import static com.ltm.runningtracker.util.Constants.HEIGHT;
import static com.ltm.runningtracker.util.Constants.NAME;
import static com.ltm.runningtracker.util.Constants.RUN_COORDINATES;
import static com.ltm.runningtracker.util.Constants.TEMPERATURE;
import static com.ltm.runningtracker.util.Constants.UNEXPECTED_VALUE;
import static com.ltm.runningtracker.util.Constants.WEIGHT;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.database.AppDatabase;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.database.RunDao;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.database.UserDao;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;
import java.util.Objects;

/**
 * Content Provider which exposes access to the underlying Room database. It allows 1. Insertion of
 * new Runs and User objects 2. Updating of Run and User objects 3. Deletion of Run and User
 * objects
 *
 * It utilises a ContentProviderContract for its operations.
 *
 * @see DroidProviderContract
 */
public class DroidContentProvider extends ContentProvider {

  // Data access objects
  private RunDao runDao;
  private UserDao userDao;

  @Override
  public boolean onCreate() {
    AppDatabase appDatabase = AppDatabase.getInstance(getContext());
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
        return runDao.getById(Integer.parseInt(Objects.requireNonNull(uri.getLastPathSegment())));
      case FREEZING_RUNS:
      case COLD_RUNS:
      case MILD_RUNS:
      case WARM_RUNS:
      case HOT_RUNS:
        // All run types can be fetched in the same way by using the weatherType in the parameter uri
        c = runDao.getByWeather(
            WeatherClassifier
                .valueOf(Objects.requireNonNull(uri.getLastPathSegment()).toUpperCase())
                .getValue());
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
    Uri var;

    switch (URI_MATCHER.match(uri)) {
      case RUNS:
        long runId = runDao
            .insert(getParsedRunBuilder(Objects.requireNonNull(contentValues)).build());
        var = ContentUris.withAppendedId(uri, runId);
        break;
      case USER:
        User user = getParsedUserBuilder(Objects.requireNonNull(contentValues)).build();
        // Cache
        getUserRepository().setUserAsync(user);
        long userId = userDao.insert(user);
        var = ContentUris.withAppendedId(uri, userId);
        break;
      default:
        throw new IllegalStateException(UNEXPECTED_VALUE + URI_MATCHER.match(uri));
    }

    // Notify change
    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(var, null);

    return var;
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
    int numRowsDeleted = Integer.MIN_VALUE;

    switch (URI_MATCHER.match(uri)) {
      case RUNS:
        numRowsDeleted = runDao.delete();
        break;
      case RUN_BY_ID:
        numRowsDeleted = runDao.deleteById(Integer.parseInt(
            Objects.requireNonNull(uri.getLastPathSegment())));
        break;
      case FREEZING_RUNS:
      case COLD_RUNS:
      case MILD_RUNS:
      case WARM_RUNS:
      case HOT_RUNS:
        // Similarly to query, all run types can be deleted by using the weatherType in the uri
        numRowsDeleted = runDao.deleteByWeather(
            WeatherClassifier
                .valueOf(Objects.requireNonNull(uri.getLastPathSegment()).toUpperCase())
                .getValue());
        break;
      case USER:
        numRowsDeleted = userDao.delete();
        break;
    }

    // Notify change
    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

    return numRowsDeleted;
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
      @Nullable String[] strings) {
    switch (URI_MATCHER.match(uri)) {
      case USER:
        userDao.updateName(contentValues.getAsString(NAME));
        userDao.updateWeight(contentValues.getAsInteger(WEIGHT));
        userDao.updateHeight(contentValues.getAsInteger(HEIGHT));
        break;
      case RUN_BY_ID:
        // You can only update the run's tag, not any additional information
        String type = contentValues.getAsString(RUN_TYPE);
        runDao.updateRunType(Integer.parseInt(uri.getLastPathSegment()),
            type);
        break;
      default:
        throw new IllegalStateException(UNEXPECTED_VALUE + URI_MATCHER.match(uri));
    }

    // Number of rows updated
    return 1;
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
   * This method handles the absence of some values via builder pattern.
   *
   * @param contentValues containing Run data and meta-data
   * @return Run.Builder object able to construct a Run object on-the-fly
   */
  private Run.Builder getParsedRunBuilder(ContentValues contentValues) {
    Run.Builder builder = new Run.Builder(contentValues.getAsLong(DATE),
        contentValues.getAsDouble(DISTANCE), contentValues.getAsInteger(DURATION));

    if (contentValues.containsKey(TEMPERATURE)) {
      builder = builder.withTemperature(contentValues.getAsFloat(TEMPERATURE));
    }
    if (contentValues.containsKey(RUN_COORDINATES)) {
      builder = builder.withRunCoordinates(contentValues.getAsByteArray(RUN_COORDINATES));
    }

    int runTypeId;

    if (contentValues.containsKey(RUN_TYPE)) {
      runTypeId = contentValues.getAsInteger(RUN_TYPE);
    } else {
      runTypeId = 0;
    }

    String runType = ActivityViewModel
        .capitalizeFirstLetter(RunTypeClassifier.valueOf(runTypeId).toString());

    return builder.withRunType(runType);
  }

  /**
   * Method's purpose similar to above, appropriately implemented for the User Model. see {@link
   * #getParsedUserBuilder(ContentValues)}
   *
   * @param contentValues containing name, weight and height
   */
  private User.Builder getParsedUserBuilder(ContentValues contentValues) {
    return new User.Builder(contentValues.getAsString(NAME))
        .withWeight(contentValues.getAsInteger(WEIGHT))
        .withHeight(contentValues.getAsInteger(HEIGHT));
  }

}
