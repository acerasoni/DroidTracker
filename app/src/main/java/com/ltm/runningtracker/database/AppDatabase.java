package com.ltm.runningtracker.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.ltm.runningtracker.database.model.Run;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.util.RunCoordinates;

/**
 * Database class as defined in the Android Jetpack architectural standards
 *
 * @see <a href="https://medium.com/mindorks/using-room-database-android-jetpack-675a89a0e942">Android
 * Jetpack documentation</a>
 */
// Database instantiated in the app's constructor above
@Database(entities = {Run.class, User.class}, version = 200, exportSchema = false)
@TypeConverters(RunCoordinates.class)
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

  public abstract UserDao userDao();
}