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
import androidx.room.Ignore;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;
import com.ltm.runningtracker.database.User;
import java.net.URI;

public class UserRepository {

  private Uri userURI;
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
  public void createUser(String name, String dietName, int weight, int height) {
    // Cache on main UI thread - allows for consistency and does not slow down thread
    ContentValues contentValues = new ContentValues();
    float bmi = calculateBMI(weight, height, true);
    Log.d("BMI", Float.toString(bmi));
    contentValues.put("name", name);
    contentValues.put("dietName", dietName);
    contentValues.put("bmi", bmi);

    AsyncTask.execute(() -> {
      // Flush the DB
      getApplicationContext().getContentResolver().delete(USER_URI, null, null);
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
    String dietName = c.getString(1);
    float bmi = c.getInt(2);
    return new User(name, dietName, bmi);
  }

  /**
   * https://www.cdc.gov/healthyweight/assessing/bmi/childrens_bmi/childrens_bmi_formula.html
   *
   * @param weight in kg for metric, lbs for imperial
   * @param height in cm for metric, in for imperial
   */
  private float calculateBMI(int weight, int height, boolean isMetric) {
    return isMetric ? calculateMetricBMI(weight, height) : calculateImperialBMI(weight, height);
  }

  private float calculateMetricBMI(int weight, int height) {
    float metres = height / 100;
    return (weight / (metres * metres));
  }

  private float calculateImperialBMI(int weight, int height) {
    return 703 * weight / (height * height);
  }

}
