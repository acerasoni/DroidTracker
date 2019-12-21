package com.ltm.runningtracker.database;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.survivingwithandroid.weather.lib.model.Weather;

@Entity
public class User {

  @PrimaryKey
  private int uid;

  @ColumnInfo(name = "first_name")
  private String firstName;

  @ColumnInfo(name = "last_name")
  private String lastName;
}
