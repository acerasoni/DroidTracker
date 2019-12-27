package com.ltm.runningtracker.database;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

  @Query("SELECT * FROM user")
  Cursor getUser();

  @Query("DELETE FROM user")
  void delete();

  @Insert
  long insert(User user);

}
