package com.ltm.runningtracker.android.fragment.impl;

import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class WarmPerformanceFragment extends PerformanceFragment {

  public WarmPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.WARM;
  }

}
