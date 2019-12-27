package com.ltm.runningtracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * https://medium.com/mindorks/using-room-database-android-jetpack-675a89a0e942
 */
// Database instantiated in the app's constructor above
@Database(entities = {Diet.class, Run.class, User.class}, version = 180, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static final String DB_NAME = "tracker_db";

  private static AppDatabase instance;

  public static synchronized AppDatabase getInstance(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
          .fallbackToDestructiveMigration().build();
    }
    return instance;
  }

  public abstract RunDao runDao();
  public abstract DietDao dietDao();
  public abstract UserDao userDao();
}