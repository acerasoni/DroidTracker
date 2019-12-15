package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTracker.getUserRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.User;
import com.ltm.runningtracker.repository.UserRepository;

public class UserProfileViewModel extends ViewModel {

  private SavedStateHandle savedStateHandle;
  private UserRepository userRepository;
  protected LiveData<User> user;

  public UserProfileViewModel(SavedStateHandle savedStateHandle) {
    this.savedStateHandle = savedStateHandle;
    userRepository = getUserRepository();
    user = userRepository.getUser();
  }
}
