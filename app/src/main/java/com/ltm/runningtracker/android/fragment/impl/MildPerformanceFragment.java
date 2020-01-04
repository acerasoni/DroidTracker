package com.ltm.runningtracker.android.fragment.impl;

import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class MildPerformanceFragment extends PerformanceFragment {

  public MildPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.MILD;
  }
}
