package com.ltm.runningtracker.android.activity.fragment.impl;

import com.ltm.runningtracker.android.activity.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
public class MildPerformanceFragment extends PerformanceFragment {

  public MildPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.MILD;
  }
}
