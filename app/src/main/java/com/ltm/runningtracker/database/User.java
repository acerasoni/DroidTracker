package com.ltm.runningtracker.database;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Diet.class,
    parentColumns = "name",
    childColumns = "dietName",
    onDelete = CASCADE), indices = {
    @Index(name = "dietName_userIndex", value = {"dietName"})})
public class User {

  @PrimaryKey
  private int _id;

  @ColumnInfo(name = "first_name")
  private String name;

  @ColumnInfo(name = "dietName")
  private String dietName;

  @ColumnInfo
  private float bmi;

  public User(String name, String dietName, float bmi) {
    this.name = name;
    this.dietName = dietName;
    this.bmi = bmi;
  }

  public void set_id(int _id) {
    this._id = _id;
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

  public String getDietName() {
    return dietName;
  }

  public void setDietName(String dietName) {
    this.dietName = dietName;
  }

}
