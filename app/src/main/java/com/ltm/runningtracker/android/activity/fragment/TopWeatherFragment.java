package com.ltm.runningtracker.android.activity.fragment;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.TopWeatherViewModel;

public class TopWeatherFragment extends Fragment {

  private TopWeatherViewModel mViewModel;

  public static TopWeatherFragment newInstance() {
    return new TopWeatherFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.top_weather_fragment, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(TopWeatherViewModel.class);
    // TODO: Use the ViewModel
  }

}
