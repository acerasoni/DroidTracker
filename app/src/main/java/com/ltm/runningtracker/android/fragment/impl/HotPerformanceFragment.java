package com.ltm.runningtracker.android.fragment.impl;

import com.ltm.runningtracker.android.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.parser.WeatherParser.WeatherClassifier;

public class HotPerformanceFragment extends PerformanceFragment {

  public HotPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.HOT;
  }

}
