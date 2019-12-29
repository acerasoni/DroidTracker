package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.USER_URI;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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

  public boolean doesUserExist() {
    return user.getValue() != null;
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


  /**
   * This method is called from the UI thread. It will call the db asynchronously which will create
   * the User object and assign it to the user repository (cache) and database (memory)
   */
  public void createUser(String name, int weight, int height) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("name", name);
    contentValues.put("weight", weight);
    contentValues.put("height", height);
    AsyncTask.execute(
        () -> getApplicationContext().getContentResolver().insert(USER_URI, contentValues));
  }

  public void updateUser(String name, int weight, int height) {
    // Update cache
    user.getValue().name = name;
    user.getValue().weight = weight;
    user.getValue().height = height;

    // Asynchronously update DB
    AsyncTask.execute(
        () -> getApplicationContext().getContentResolver().update(USER_URI, null, null, null));
  }

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
    float walkingPace = c.getFloat(4);
    float joggingPace = c.getFloat(5);
    float runningPace = c.getFloat(6);
    float sprintingPace = c.getFloat(7);

    return new User.Builder(name, weight, height).withWalkingPace(walkingPace)
        .withJoggingPace(joggingPace).withRunningPace(runningPace).withSprintingPace(sprintingPace).build();
  }


}
