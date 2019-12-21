package com.ltm.runningtracker.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ltm.runningtracker.database.User;

public class UserRepository {

  private MutableLiveData<User> user;

  public UserRepository() {
    user = new MutableLiveData<>(new User());
  }

  public LiveData<User> getUser() {
      return user;
  }


}
