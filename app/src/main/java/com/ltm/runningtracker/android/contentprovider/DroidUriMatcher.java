package com.ltm.runningtracker.android.contentprovider;

import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.AUTHORITY;

import android.content.UriMatcher;

class DroidUriMatcher {

  // <-- Purposely package private. Avoids other modules having access to the logic with which
  // the content provider matches URI's, and access to the UriMatcher object -->

  static final UriMatcher URI_MATCHER;

  static final int RUNS = 0;
  static final int FREEZING_RUNS = 1;
  static final int COLD_RUNS = 2;
  static final int MILD_RUNS = 3;
  static final int WARM_RUNS = 4;
  static final int HOT_RUNS = 5;
  static final int RUN_BY_ID = 6;
  static final int USER = 7;

  static {
    URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    URI_MATCHER.addURI(AUTHORITY, "runs", RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/freezing", FREEZING_RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/cold", COLD_RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/mild", MILD_RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/warm", WARM_RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/hot", HOT_RUNS);
    URI_MATCHER.addURI(AUTHORITY, "runs/#", RUN_BY_ID);
    URI_MATCHER.addURI(AUTHORITY, "user", USER);
  }

}
