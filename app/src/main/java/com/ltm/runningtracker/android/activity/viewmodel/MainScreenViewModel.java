package com.ltm.runningtracker.android.activity.viewmodel;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.ltm.runningtracker.parcelable.User;
import com.ltm.runningtracker.repository.UserRepository;

public class MainScreenViewModel extends ViewModel {

  private SavedStateHandle savedStateHandle;
  private UserRepository userRepository;
  private MutableLiveData<User> user;

  public MainScreenViewModel() {
    userRepository = getUserRepository();
    user = userRepository.getUser();
  }
  public MainScreenViewModel(SavedStateHandle savedStateHandle) {
    this.savedStateHandle = savedStateHandle;
    userRepository = getUserRepository();
    user = userRepository.getUser();
  }

  public LiveData<User> getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user.setValue(user);
  }
}
