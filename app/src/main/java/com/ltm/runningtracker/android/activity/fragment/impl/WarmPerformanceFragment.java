package com.ltm.runningtracker.android.activity.fragment.impl;

import com.ltm.runningtracker.android.activity.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class WarmPerformanceFragment extends PerformanceFragment {

  public WarmPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.WARM;
  }

}
