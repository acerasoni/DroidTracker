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

  public static final String ID = "_id";
  public static final String LOCATION = "location";
  public static final String DATE = "date";
  public static final String TYPE = "runType";
  public static final String PACE = "pace";

}
