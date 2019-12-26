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
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceByDietViewModel;

public class PerformanceByDietFragment extends Fragment {

  private PerformanceByDietViewModel mViewModel;

  public static PerformanceByDietFragment newInstance() {
    return new PerformanceByDietFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.performance_by_diet_fragment, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(PerformanceByDietViewModel.class);
    // TODO: Use the ViewModel
  }

}
