package com.ltm.runningtracker.database;

import android.database.Cursor;
import android.net.Uri;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.ltm.runningtracker.database.model.User;

@Dao
public interface UserDao {

  @Query("SELECT * FROM user")
  Cursor getUser();

  @Query("DELETE FROM user")
  int delete();

  @Insert
  long insert(User user);

  @Update
  int update(User user);
}
