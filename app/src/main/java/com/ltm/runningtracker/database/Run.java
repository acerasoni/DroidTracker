package com.ltm.runningtracker.database;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;

import android.location.Location;
import android.location.LocationProvider;
import android.util.Log;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * Storing speed and distance can be considered redundant as this can be derived at runtime, though
 * it is a tradeoff between runtime resources vs having an additional column in the database
 */
@Entity
public class Run implements Serializable {

  @PrimaryKey(autoGenerate = true)
  private int id;

  @ColumnInfo(name = "weather")
  private String weather;

  @ColumnInfo(name = "duration")
  private long duration;

  @ColumnInfo(name = "start_lat")
  private double startLat;

  @ColumnInfo(name = "start_lon")
  private double startLon;

  @ColumnInfo(name = "end_lat")
  private double endLat;

  @ColumnInfo(name = "end_lon")
  private double endLon;

  @ColumnInfo(name = "distance")
  private double distance;

  @ColumnInfo(name = "average_speed")
  private double averageSpeed;

  public Run(String weather, long duration, double startLat, double startLon, double endLat,
      double endLon, double distance, double averageSpeed) {
    this.weather = weather;
    this.duration = duration;
    this.startLat = startLat;
    this.startLon = startLon;
    this.endLat = endLat;
    this.endLon = endLon;
    this.duration = duration;
    this.distance = distance;
    this.averageSpeed = averageSpeed;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public void setWeather(String weather) {
    this.weather = weather;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public void setAverageSpeed(double averageSpeed) {
    this.averageSpeed = averageSpeed;
  }

  public void setStartLat(double startLat) {
    this.startLat = startLat;
  }

  public void setEndLat(double endLat) {
    this.endLat = endLat;
  }

  public void setStartLon(double startLon) {
    this.startLon = startLon;
  }

  public void setEndLon(double endLon) {
    this.endLon = endLon;
  }

  public int getId() {
    return id;
  }

  public String getWeather() {
    return weather;
  }

  public long getDuration() {
    return duration;
  }

  public double getStartLat() {
    return startLat;
  }

  public double getStartLon() {
    return startLon;
  }

  public double getEndLat() {
    return endLat;
  }

  public double getEndLon() {
    return endLon;
  }

  public double getDistance() {
    return distance;
  }

  public double getAverageSpeed() {
    return averageSpeed;
  }

}
