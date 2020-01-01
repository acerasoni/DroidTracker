package com.ltm.runningtracker.database;

import android.database.Cursor;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.ltm.runningtracker.database.model.Run;

@Dao
public interface RunDao {

    @Query("SELECT * FROM run")
    Cursor getAll();

    @Query("SELECT * FROM run where _id=:id")
    Cursor getById(int id);

    @Query("SELECT * FROM run WHERE weatherType=:type")
    Cursor getByWeather(int type);

    @Insert
    long insert(Run run);

    @Query("UPDATE run SET runType=:type WHERE _id=:id")
    void updateRunType(int id, String type);

    @Query("DELETE FROM run")
    int delete();

    @Query("DELETE FROM run WHERE _id=:id")
    int deleteById(int id);

    @Query("DELETE FROM run WHERE weatherType=:type")
    int deleteByWeather(int type);




}
