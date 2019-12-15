package com.ltm.runningtracker;

import android.app.Application;
import android.content.Context;

/**
 * https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
 */
public class RunningTracker extends Application {

  private static Context context;

  public void onCreate() {
    super.onCreate();
    RunningTracker.context = getApplicationContext();
  }

  public static Context getAppContext() {
    return RunningTracker.context;
  }
}
