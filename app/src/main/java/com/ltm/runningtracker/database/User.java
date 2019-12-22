package com.ltm.runningtracker.database;

import static androidx.room.ForeignKey.CASCADE;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import com.survivingwithandroid.weather.lib.model.Weather;

@Entity
public class User {

  @PrimaryKey
  private int _id;

  @ColumnInfo(name = "dietId")
  private int dietId;

  @ColumnInfo(name = "first_name")
  private String firstName;

  @ColumnInfo(name = "last_name")
  private String lastName;

  public void setDietId(int dietId) {
    this.dietId = dietId;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

}
