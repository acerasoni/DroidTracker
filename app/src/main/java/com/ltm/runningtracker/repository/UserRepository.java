package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;
import static com.ltm.runningtracker.util.Constants.HEIGHT;
import static com.ltm.runningtracker.util.Constants.NAME;
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
   * Async because called from background thread
   */
  public void setUserAsync(User user) {
    this.user.postValue(user);
  }

  public void setUser(User user) {
    this.user.setValue(user);
  }

  public void flushCache() {
    user.postValue(null);
  }

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
    ContentValues contentValues = new ContentValues();
    contentValues.put(NAME, name);
    contentValues.put(WEIGHT, weight);
    contentValues.put(HEIGHT, height);
    AsyncTask.execute(
        () -> context.getContentResolver().insert(USER_URI, contentValues));
  }

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

  @SuppressLint("Recycle")
  public void fetchUser() {
    AsyncTask.execute(() -> {
      final Cursor[] c = new Cursor[1];
      c[0] = getAppContext().getContentResolver()
          .query(USER_URI, null, null, null, null, null);
      if (c[0] != null && c[0].getCount() > 0) {
        Log.d("User", "found");
        setUserAsync(buildUserFromMemory(c[0]));
      } else {
        Log.d("User", "not found");
      }
    });
  }

  private User buildUserFromMemory(Cursor c) {
    c.moveToFirst();
    String name = c.getString(1);
    int weight = c.getInt(2);
    int height = c.getInt(3);

    return new User.Builder(name).withWeight(weight)
        .withHeight(height).build();
  }

}
