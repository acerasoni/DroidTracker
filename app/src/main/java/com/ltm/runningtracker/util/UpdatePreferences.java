package com.ltm.runningtracker.util;

import static com.ltm.runningtracker.util.Constants.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Taken and adapted from StackOverflow thread. Provides a layer of abstraction to set the update
 * timer for location tracking.
 *
 * @see <a href="https://stackoverflow.com/questions/17753800/android-default-values-for-shared-preferences">
 * StackOverflow thread</a>
 */
public class UpdatePreferences {

  public final static Integer DEFAULT_TIME_UPDATE = 2;

  private SharedPreferences sharedPreferences;
  private Editor editor;

  public UpdatePreferences(Context context) {
    this.sharedPreferences = context.getSharedPreferences(UPDATE_PREF, 0);
    this.editor = this.sharedPreferences.edit();
  }

  public void setMinTime(int value) {
    this.editor.putInt(TIME, value);
    this.editor.commit();
  }

  public Integer getMinTime() {
    return this.sharedPreferences.getInt(TIME, DEFAULT_TIME_UPDATE);
  }

  public Long getMinTimeMillis() {
    return (long) getMinTime() * 1000;
  }

  public void clear() {
    this.editor.clear();
    this.editor.commit();
  }

}