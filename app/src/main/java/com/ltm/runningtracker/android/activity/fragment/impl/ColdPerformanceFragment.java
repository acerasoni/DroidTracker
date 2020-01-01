package com.ltm.runningtracker.android.activity.fragment.impl;

import com.ltm.runningtracker.android.activity.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class ColdPerformanceFragment extends PerformanceFragment {

  public ColdPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.COLD;
  }

}
