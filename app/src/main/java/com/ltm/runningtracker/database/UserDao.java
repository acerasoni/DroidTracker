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

  @Query("UPDATE user SET name=:name")
  void updateName(String name);

  @Query("UPDATE user SET weight=:weight")
  void updateWeight(int weight);

  @Query("UPDATE user SET height=:height")
  void updateHeight(int height);

  @Insert
  long insert(User user);
}
