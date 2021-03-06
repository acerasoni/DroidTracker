package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;
import static com.ltm.runningtracker.util.Constants.HEIGHT;
import static com.ltm.runningtracker.util.Constants.NAME;
import static com.ltm.runningtracker.util.Constants.USER_ID;
import static com.ltm.runningtracker.util.Constants.WEIGHT;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.util.Serializer;

/**
 * Repository responsible for exposing, storing and modifying the userCache's LiveData object which
 * acts as cache to the database.
 */
public class UserRepository {

  // Cache
  private MutableLiveData<User> userCache;

  public UserRepository() {
    userCache = new MutableLiveData<>();
    fetchUser();
  }

  public LiveData<User> getUserLiveData() {
    return userCache;
  }

  public User getUserCache() {
    return userCache.getValue();
  }

  /**
   * Allows setting a new userCache asynchronously from a background thread.
   *
   * @param user object to be set.
   */
  public synchronized void setUserAsync(User user) {
    this.userCache.postValue(user);
  }

  public synchronized void flushCache() {
    userCache.postValue(null);
  }

  /**
   * Will asynchronously delete the userCache. Can be safely called from a background thread.
   *
   * @param context from which to call the content provider from
   */
  public void deleteUser(Context context) {
    AsyncTask.execute(() -> {
      context.getContentResolver().delete(USER_URI, null, null);
      getUserRepository().flushCache();
    });
  }

  /**
   * This method is called from the UI thread. It will call the db asynchronously which will create
   * the User object and assign it to the userCache repository (cache) and database (memory)
   */
  public synchronized void createUser(String name, int weight, int height, Context context) {
    // Build userCache
    User user = getParsedUserBuilder(name, weight, height).build();

    /*
     We must set the cache here because because the Content Provider must not know
     or have a dependency on previous levels of abstraction
     */
    setUserAsync(user);

    // Serialize and send to Content Provider
    ContentValues contentValues = new ContentValues();
    contentValues.put(USER_ID, Serializer.toByteArray(user));

    AsyncTask.execute(
        () -> context.getContentResolver().insert(USER_URI, contentValues));
  }

  /**
   * Will asynchronously create a new userCache object and insert it into the underlying Room
   * database. This method is called from the UI thread.
   */
  public synchronized void updateUser(String name, int weight, int height, Context context) {
    // Update cache
    userCache.getValue().name = name;
    userCache.getValue().weight = weight;
    userCache.getValue().height = height;

    ContentValues contentValues = new ContentValues();
    contentValues.put(NAME, name);
    contentValues.put(WEIGHT, weight);
    contentValues.put(HEIGHT, height);

    // Asynchronously update DB
    AsyncTask.execute(
        () -> context.getContentResolver()
            .update(USER_URI, contentValues, null, null));
  }

  /**
   * This method retrieves the userCache cursor from the database, if exists, and builds a userCache
   * cache from memory. {@link #buildUserFromMemory(Cursor)}
   */
  @SuppressLint("Recycle")
  public synchronized void fetchUser() {
    AsyncTask.execute(() -> {
      try (Cursor c = getAppContext().getContentResolver()
          .query(USER_URI, null, null, null, null, null)) {
        if (c.getCount() > 0) {
          setUserAsync(buildUserFromMemory(c));
        }
      }
    });
  }

  /**
   * @return User object built from the Cursor via Builder pattern
   */
  private User buildUserFromMemory(Cursor cursor) {
    return User.fromCursorToUser(cursor, 0);
  }

  /**
   * Method's purpose is to build a User object from input Content Values, appropriately implemented
   * for the User Model.
   *
   * @return User object
   * @see com.ltm.runningtracker.android.contentprovider.DroidContentProvider
   */
  private User.Builder getParsedUserBuilder(String name, int weight, int height) {
    return new User.Builder(name)
        .withWeight(weight)
        .withHeight(height);
  }

}