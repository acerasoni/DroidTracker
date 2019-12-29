package com.ltm.runningtracker.android.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;

public class DroidProviderContract {
  public static final String AUTHORITY = "com.ltm.runningtracker.android.contentprovider.DroidContentProvider";
  public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/PsyagceProvider.data.text";
  public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/PsyagceProvider.data.text";
  public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

  public static final Uri USER_URI =
      Uri.parse("content://"+AUTHORITY+"/user");

  public static final Uri RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs");

  public static final String RUN_ID = "runId";
  public static final String DATE = "date";
  public static final String WEATHER = "temperature";
  public static final String DURATION = "duration";

}
