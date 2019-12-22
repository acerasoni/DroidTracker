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

  public static final Uri FREEZING_RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs/freezing");

  public static final Uri COLD_RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs/cold");

  public static final Uri MILD_RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs/mild");

  public static final Uri WARM_RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs/warm");

  public static final Uri HOT_RUNS_URI =
      Uri.parse("content://"+AUTHORITY+"/runs/hot");

  public static final String RUN_ID = "runId";
  public static final String DATE = "date";
  public static final String WEATHER = "weather";
  public static final String DURATION = "duration";

  public static final UriMatcher URI_MATCHER;

  static {
    URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    URI_MATCHER.addURI(AUTHORITY, "runs", 0);
    URI_MATCHER.addURI(AUTHORITY, "runs/freezing", 1);
    URI_MATCHER.addURI(AUTHORITY, "runs/cold", 2);
    URI_MATCHER.addURI(AUTHORITY, "runs/mild", 3);
    URI_MATCHER.addURI(AUTHORITY, "runs/warm", 4);
    URI_MATCHER.addURI(AUTHORITY, "runs/hot", 5);
    URI_MATCHER.addURI(AUTHORITY, "runs/#", 6);
  }

}
