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

  @Ignore
  public User(String name, String dietName, int weight, int height, boolean isMetric) {
    this.name = name;
    this.dietName = dietName;
    this.bmi = calculateBMI(weight, height, isMetric);
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

  /**
   * https://www.cdc.gov/healthyweight/assessing/bmi/childrens_bmi/childrens_bmi_formula.html
   */
  @Ignore
  private float calculateBMI(int weight, int height, boolean isMetric) {
    return isMetric ? calculateMetricBMI(weight, height) : calculateImperialBMI(weight, height);
  }

  @Ignore
  private float calculateMetricBMI(int weight, int height) {
    return weight / (height * height);
  }

  @Ignore
  private float calculateImperialBMI(int weight, int height) {
    return 703 * weight / (height * height);
  }


}
