package com.ltm.runningtracker.android.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;

public class ContentProviderContract {
  public static final String AUTHORITY = "com.ltm.runningtracker.android.contentprovider.RunningTrackerProvider";
  public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/PsyagceProvider.data.text";
  public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/PsyagceProvider.data.text";
  public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

  public static final Uri RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs");

  public static final String _ID = "_id";
  public static final String WEATHER = "weather";
  public static final String DURATION = "duration";

  public static final UriMatcher URI_MATCHER;

  static {
    URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    URI_MATCHER.addURI(AUTHORITY, "runs", 0);
    URI_MATCHER.addURI(AUTHORITY, "runs/#", 1);
  }

}
