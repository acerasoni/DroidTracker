package com.ltm.runningtracker.database;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RunDao {

    @Query("SELECT * FROM run")
    Cursor getAll();

    @Query("SELECT * FROM run WHERE _id IN (:runIds)")
    List<Run> loadAllByIds(int[] runIds);

    @Insert
    void insertAll(Run... runs);

    @Insert
    long insert(Run run);

    @Delete
    void delete(Run run);

    @Update
    void update(Run run);

}
