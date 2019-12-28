package com.ltm.runningtracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import com.ltm.runningtracker.database.model.Diet;

@Dao
public interface DietDao {

  @Insert 
  long insert(Diet diet);

}
