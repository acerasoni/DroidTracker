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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.util.Serializer;
import java.util.Objects;

/**
 * Repository responsible for exposing, storing and modifying the user's LiveData object which acts
 * as cache to the database.
 */
public class UserRepository {

  // Cache
  private MutableLiveData<User> user;

  public UserRepository() {
    user = new MutableLiveData<>();
    fetchUser();
  }

  public LiveData<User> getUserLiveData() {
    return user;
  }

  public User getUser() {
    return user.getValue();
  }


  /**
   * Allows setting a new user asynchronously from a background thread.
   *
   * @param user object to be set.
   */
  public void setUserAsync(User user) {
    this.user.postValue(user);
  }

  /**
   * Allows setting a new user synchronously from the UI thread.
   *
   * @param user object to be set.
   */
  public void setUser(User user) {
    this.user.setValue(user);
  }

  public void flushCache() {
    user.postValue(null);
  }

  /**
   * Will asynchronously delete the user. Can be safely called from a background thread.
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
   * the User object and assign it to the user repository (cache) and database (memory)
   */
  public void createUser(String name, int weight, int height, Context context) {
    // Build user
    User user = getParsedUserBuilder(name, weight, height).build();

    // We must set the cache here because because the Content Provider must not know
    // or have a dependency on previous levels of abstraction
    setUserAsync(user);

    // Serialize and send to Content Provider
    ContentValues contentValues = new ContentValues();
    contentValues.put(USER_ID, Serializer.toByteArray(user));

    AsyncTask.execute(
        () -> context.getContentResolver().insert(USER_URI, contentValues));
  }

  /**
   * Will asynchronously create a new user object and insert it into the underlying Room database.
   * This method is called from the UI thread.
   */
  public void updateUser(String name, int weight, int height, Context context) {
    // Update cache
    user.getValue().name = name;
    user.getValue().weight = weight;
    user.getValue().height = height;

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
   * This method retrieves the user cursor from the database, if exists, and builds a user cache
   * from memory. {@link #buildUserFromMemory(Cursor)}
   */
  @SuppressLint("Recycle")
  public void fetchUser() {
    AsyncTask.execute(() -> {
      final Cursor[] c = new Cursor[1];
      c[0] = getAppContext().getContentResolver()
          .query(USER_URI, null, null, null, null, null);
      if (c[0] != null && c[0].getCount() > 0) {
        setUserAsync(buildUserFromMemory(c[0]));
      }
    });
  }

  /**
   * @return User object built from the Cursor via Builder pattern
   */
  private User buildUserFromMemory(Cursor cursor) {
    cursor.moveToFirst();
    String name = cursor.getString(1);
    int weight = cursor.getInt(2);
    int height = cursor.getInt(3);

    return new User.Builder(name).withWeight(weight)
        .withHeight(height).build();
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
