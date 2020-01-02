package com.ltm.runningtracker.android.fragment.impl;

import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class FreezingPerformanceFragment extends PerformanceFragment {

  public FreezingPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.FREEZING;
  }

}
