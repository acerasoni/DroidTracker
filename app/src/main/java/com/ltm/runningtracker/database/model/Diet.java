package com.ltm.runningtracker.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(indices = {@Index(value = {"name"},
    unique = true)})
public class Diet implements Serializable {

  @PrimaryKey(autoGenerate = true)
  private int _id;

  @ColumnInfo(name = "name")
  private String name;

  public Diet(String name) {
    this.name = name;
  }

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}