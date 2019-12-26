package com.ltm.runningtracker.database;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface UserDao {

  @Query("SELECT * FROM user")
  Cursor getUser();

}
