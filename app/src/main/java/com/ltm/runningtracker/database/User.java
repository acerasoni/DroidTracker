package com.ltm.runningtracker.database;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
public class User {

  @PrimaryKey
  private int _id;

  @ColumnInfo(name = "name")
  private String name;

  @ColumnInfo(name = "weight")
  private String weight;

  @ColumnInfo(name = "height")
  private String height;

  @ColumnInfo
  private float bmi;

  @ColumnInfo
  private float pace;

  public User(String name, float bmi) {
    this.name = name;
    this.bmi = bmi;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  public String getHeight() {
    return height;
  }

  public float getPace() {
    return pace;
  }

  public void setPace(float pace) {
    this.pace = pace;
  }

  public String getWeight() {
    return weight;
  }

  public int get_id() {
    return _id;
  }

  public float getBmi() {
    return bmi;
  }

  public void setBmi(float bmi) {
    this.bmi = bmi;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
