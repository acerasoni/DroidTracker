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
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;

public class PerformanceFragment extends Fragment {

  private PerformanceViewModel mViewModel;

  public static PerformanceFragment newInstance() {
    return new PerformanceFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.performance_fragment, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(PerformanceViewModel.class);
    // TODO: Use the ViewModel
  }

}
