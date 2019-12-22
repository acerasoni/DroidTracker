package com.ltm.runningtracker.database;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface DietDao {

  @Insert 
  long insert(Diet diet);

}
