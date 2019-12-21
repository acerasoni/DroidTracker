package com.ltm.runningtracker.database;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.util.WeatherParser.parseTemperatureToString;

import android.location.Location;
import android.location.LocationProvider;
import android.util.Log;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Storing speed and distance can be considered redundant as this can be derived at runtime, though
 * it is a tradeoff between runtime resources vs having an additional column in the database
 */
@Entity
public class Run implements Serializable {

  @PrimaryKey(autoGenerate = true)
  private int _id;

  @ColumnInfo(name = "date")
  private String date;

  @ColumnInfo(name = "weatherType")
  private String weatherType;

  @ColumnInfo(name = "weather")
  private String weather;

  @ColumnInfo(name = "duration")
  private String duration;

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

  public Run(String weather, String duration, double startLat, double startLon, double endLat,
      double endLon, double distance, double averageSpeed) {
    weatherType = parseTemperatureToString(weather);
    date = new SimpleDateFormat("dd/M/yyyy").format(new Date());
    this.weather = weather;
    this.duration = duration;
    this.startLat = startLat;
    this.startLon = startLon;
    this.endLat = endLat;
    this.endLon = endLon;
    this.distance = distance;
    this.averageSpeed = averageSpeed;
  }

  public void setId(int _id) {
    this._id = _id;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public void setWeatherType(String weatherType) {
    this.weatherType = weatherType;
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
    return _id;
  }

  public String getDate() {
    return date;
  }

  public String getWeatherType() {
    return weatherType;
  }

  public String getWeather() {
    return weather;
  }

  public String getDuration() {
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
