package com.ltm.runningtracker.repository;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.USER_URI;
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

  private Uri userURI;

  // Cache
  private MutableLiveData<User> user;

  public UserRepository() {
    user = new MutableLiveData<>();
    fetchUser();
  }

  public LiveData<User> getUserLiveData() {
    return user;
  }

  public void setUser(User user) {
    this.user.setValue(user);
  }

  public boolean doesUserExist() {
    return user.getValue() != null;
  }

  /**
   * Async because called from background thread
   */
  public void setUserAsync(User user) {
    this.user.postValue(user);
  }

  public User getUser() {
    return user.getValue();
  }

  /**
   * This method is called from the UI thread. It will call the db asynchronously which will create
   * the User object and assign it to the user repository (cache) and database (memory)
   */
  public void createUser(String name, int weight, int height, boolean creatingUser) {
    float walkingPace, joggingPace, runningPace;
    if (creatingUser) {
      walkingPace = 0f;
      joggingPace = 0f;
      runningPace = 0f;

    } else {
      walkingPace = getUser().getWalkingPace();
      joggingPace = getUser().getJoggingPace();
      runningPace = getUser().getRunningPace();
    }

    ContentValues contentValues = new ContentValues();
    contentValues.put("name", name);
    contentValues.put("weight", weight);
    contentValues.put("height", height);
    contentValues.put("walkingPace", walkingPace);
    contentValues.put("joggingPace", joggingPace);
    contentValues.put("runningPace", runningPace);

    AsyncTask.execute(() -> {
      // Flush the DB
      getApplicationContext().getContentResolver().delete(USER_URI, null, null);

      // Insert in DB
      userURI = getApplicationContext().getContentResolver().insert(USER_URI, contentValues);
    });
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
    String name = c.getString(0);
    int weight = c.getInt(1);
    int height = c.getInt(2);
    float walkingPace = c.getFloat(3);
    float joggingPace = c.getFloat(4);
    float runningPace = c.getFloat(5);
    return new User(name, weight, height, walkingPace, joggingPace, runningPace);
  }


}
