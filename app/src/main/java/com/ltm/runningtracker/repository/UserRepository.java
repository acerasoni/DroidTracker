package com.ltm.runningtracker.repository;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.database.Cursor;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;
import com.ltm.runningtracker.database.User;

public class UserRepository {

  private MutableLiveData<User> user;

  public UserRepository() {
    user = new MutableLiveData<>();
  }

  public LiveData<User> getUser() {
    return user;
  }

  public void setUser(User user) {
    if (this.user == null) {
      this.user = new MutableLiveData<User>();
    }
    this.user.setValue(user);
  }

  public boolean isUserSetup() {
    boolean result;

    final Cursor[] c = new Cursor[1];
    AsyncTask.execute(() -> {
      c[0] = getApplicationContext().getContentResolver()
          .query(ContentProviderContract.USER_URI, null, null, null, null, null);

    });

    if (c[0] == null || c[0].getCount() == 0) {
      result = false;
    } else {
      user.setValue(buildUser(c[0]));
      result = true;
    }
    return result;
  }

  private User buildUser(Cursor c) {
    c.moveToFirst();
    String name = c.getString(0);
    String dietName = c.getString(1);
    int bmi = c.getInt(2);
    return new User(name, dietName, bmi);
  }
}
