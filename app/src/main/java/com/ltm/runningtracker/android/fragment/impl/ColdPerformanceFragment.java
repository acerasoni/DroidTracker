package com.ltm.runningtracker.android.fragment.impl;

import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class ColdPerformanceFragment extends PerformanceFragment {

  public ColdPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.COLD;
  }

}
