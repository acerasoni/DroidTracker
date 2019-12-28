package com.ltm.runningtracker.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

  @PrimaryKey
  private int _id;

  @ColumnInfo(name = "name")
  private String name;

  @ColumnInfo(name = "weight")
  private int weight;

  @ColumnInfo(name = "height")
  private int height;

  @ColumnInfo(name = "walkingPace")
  private float walkingPace;

  @ColumnInfo(name = "joggingPace")
  private float joggingPace;

  @ColumnInfo(name = "runningPace")
  private float runningPace;

  public User(String name, int weight, int height, float walkingPace, float joggingPace,
      float runningPace) {
    this.name = name;
    this.weight = weight;
    this.height = height;
    this.walkingPace = walkingPace;
    this.joggingPace = joggingPace;
    this.runningPace = runningPace;
  }

  public void setJoggingPace(float joggingPace) {
    this.joggingPace = joggingPace;
  }

  public void setRunningPace(float runningPace) {
    this.runningPace = runningPace;
  }

  public float getJoggingPace() {
    return joggingPace;
  }

  public float getRunningPace() {
    return runningPace;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public int getHeight() {
    return height;
  }

  public float getWalkingPace() {
    return walkingPace;
  }

  public void setWalkingPace(float walkingPace) {
    this.walkingPace = walkingPace;
  }

  public int getWeight() {
    return weight;
  }

  public int get_id() {
    return _id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
