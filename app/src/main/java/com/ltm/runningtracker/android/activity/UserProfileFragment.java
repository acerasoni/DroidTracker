package com.ltm.runningtracker.android.activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateVMFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.User;

public class UserProfileFragment extends Fragment {

  private UserProfileViewModel mViewModel;

  public static UserProfileFragment newInstance() {
    return new UserProfileFragment();
  }

  private UserProfileViewModel userProfileViewModel;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    // Get the view model
    userProfileViewModel = new ViewModelProvider(this, new SavedStateVMFactory(this))
        .get(UserProfileViewModel.class);

    // Define the observer which updates UI
    // Create the observer which updates the UI.
    final Observer<User> dataObserver = new Observer<User>() {
      @Override
      public void onChanged(@Nullable final User newName) {
        //TODO
      }
    };

    userProfileViewModel.user.observe(this, dataObserver);
    return inflater.inflate(R.layout.user_profile_fragment, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
    // TODO: Use the ViewModel
  }

}
