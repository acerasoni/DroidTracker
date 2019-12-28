package com.ltm.runningtracker.database;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.ltm.runningtracker.database.model.User;

@Dao
public interface UserDao {

  @Query("SELECT * FROM user")
  Cursor getUser();

  @Query("DELETE FROM user")
  void delete();

  @Insert
  long insert(User user);

}
