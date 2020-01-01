package com.ltm.runningtracker.android.activity.fragment.impl;

import com.ltm.runningtracker.android.activity.fragment.PerformanceFragment;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class HotPerformanceFragment extends PerformanceFragment {

  public HotPerformanceFragment() {
    this.weatherClassifierOfFragment = WeatherClassifier.HOT;
  }

}
