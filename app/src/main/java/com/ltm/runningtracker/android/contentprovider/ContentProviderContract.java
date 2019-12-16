package com.ltm.runningtracker.android.contentprovider;

import android.net.Uri;

public class ContentProviderContract {
  public static final String AUTHORITY = "com.ltm.runningtracker.android.contentprovider.RunningTrackerProvider";

  public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/PsyagceProvider.data.text";
  public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/PsyagceProvider.data.text";
  public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"/");

  public static final Uri RECIPES_URI =
      Uri.parse("content://"+AUTHORITY+"/recipes");
  public static final Uri INGREDIENTS_URI =
      Uri.parse("content://"+AUTHORITY+"/ingredients");
  public static final Uri RECIPES_INGREDIENTS_URI =
      Uri.parse("content://"+AUTHORITY+"/recipe_ingredients");


  public static final String _ID = "_id";
  public static final String NAME = "name";
  public static final String INSTRUCTIONS = "instructions";
  public static final String RATING = "rating";
}
