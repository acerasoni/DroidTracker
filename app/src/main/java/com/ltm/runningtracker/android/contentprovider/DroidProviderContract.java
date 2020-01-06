package com.ltm.runningtracker.android.contentprovider;

import android.net.Uri;

public class DroidProviderContract {

  public static final String AUTHORITY = "com.ltm.runningtracker.android.contentprovider.DroidContentProvider";
  public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/PsyagceProvider.data.text";
  public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/PsyagceProvider.data.text";

  public static final Uri USER_URI;
  public static final Uri RUNS_URI;

  static {
    StringBuilder sb = new StringBuilder("content://");
    sb.append(AUTHORITY);

    StringBuilder userBuilder = new StringBuilder(sb);
    StringBuilder runsBuilder = new StringBuilder(sb);

    USER_URI =
        Uri.parse(userBuilder.append("/user").toString());

    RUNS_URI =
        Uri.parse(runsBuilder.append("/runs").toString());
  }

  // <-- Static access variables for column names -->

  public static final String RUN_TYPE = "runType";

  // <-- Static access variables for column positions within the cursor -->

  // Shared
  public static final int ID_COL = 0;
  public static final int NAME_COL = 1;

  // User-specific
  public static final int WEIGHT_COL = 2;
  public static final int HEIGHT_COL = 3;

  // Run-specific
  public static final int DATE_COL = 2;
  public static final int TYPE_COL = 3;
  public static final int DISTANCE_COL = 4;
  public static final int DURATION_COL = 5;
  public static final int WEATHER_COL = 6;
  public static final int TEMPERATURE_COL = 7;
  public static final int COORDINATES_COL = 8;
  public static final int PACE_COL = 9;
}