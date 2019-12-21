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
    Cursor getAllRuns();

    @Query("SELECT * FROM run WHERE weatherType='Freezing'")
    Cursor getFreezingRuns();

    @Query("SELECT * FROM run WHERE weatherType='Cold'")
    Cursor getColdRuns();

    @Query("SELECT * FROM run WHERE weatherType='Mild'")
    Cursor getMildRuns();

    @Query("SELECT * FROM run WHERE weatherType='Warm'")
    Cursor getWarmRuns();

    @Query("SELECT * FROM run WHERE weatherType='Hot'")
    Cursor getHotRuns();

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
