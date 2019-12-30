package com.ltm.runningtracker.database;

import android.database.Cursor;
import android.net.Uri;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.ltm.runningtracker.database.model.Run;
import java.io.IOException;
import java.util.List;

@Dao
public interface RunDao {

    @Query("SELECT * FROM run")
    Cursor getAll();

    @Query("SELECT * FROM run where _id=:id")
    Cursor getById(int id);

    @Query("SELECT * FROM run WHERE weatherType=:type")
    Cursor getByWeather(int type);

    @Query("DELETE FROM run")
    int delete();

    @Query("DELETE FROM run WHERE weatherType=:type")
    int deleteByWeather(int type);

    @Insert
    long insert(Run run);

}
