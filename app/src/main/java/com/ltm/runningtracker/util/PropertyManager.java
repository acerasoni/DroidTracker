package com.ltm.runningtracker.util;

import static com.ltm.runningtracker.RunningTrackerApplication.getAppContext;

import android.content.res.AssetManager;
import android.util.Log;
import com.ltm.runningtracker.R;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to manage access to property files
 *
 * @see <a href=""https://stackoverflow.com/questions/23792029/where-to-put-own-properties-file-in-an-android-project-created-with-android-stud>Stackoverflow
 * Issue</a>
 */
public class PropertyManager {

  private Properties properties;

  public PropertyManager(String propertyPath) {
    properties = new Properties();
    properties = getProperties(propertyPath);
  }

  public long getMinTime() {
    return Long.parseLong(getProperty(getAppContext().getString(
        R.string.min_time)));
  }

  private String getProperty(String propertyKey) {
    return properties.getProperty(propertyKey);
  }

  private Properties getProperties(String file) {
    try {
      AssetManager assetManager = getAppContext().getAssets();
      InputStream inputStream = assetManager.open(file);
      properties.load(inputStream);
    } catch (Exception e) {
      Log.e("Exception in fetching properties. ", e.getMessage());
    }

    return properties;
  }

}
