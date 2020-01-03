package com.ltm.runningtracker.database.model;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.util.Constants.TIME_FORMAT;

import android.annotation.SuppressLint;
import android.util.Log;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.android.gms.common.util.ArrayUtils;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.parser.WeatherParser;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Entity
public class Run {

  @PrimaryKey(autoGenerate = true)
  public int _id;

  @ColumnInfo(name = "location")
  public String location;

  // Save as String rather than long (milliseconds) because we don't operate on this value
  @ColumnInfo(name = "date")
  public String date;

  @ColumnInfo(name = "runType")
  public String runType;

  @ColumnInfo(name = "distance")
  public double distance;

  // Save as String for the same reason as date
  @ColumnInfo(name = "duration")
  public String duration;

  @ColumnInfo(name = "weatherType")
  public int weatherType;

  @ColumnInfo(name = "temperature")
  public float temperature;

  // Data structure containing starting and ending latitude and longitude value pairs
  @ColumnInfo(name = "runCoordinates")
  public RunCoordinates runCoordinates;

  // Pace is always < Float.MAX_VALUE, can store as float
  @ColumnInfo(name = "pace")
  public float pace;

  // Builder pattern usually wants class constructor to be empty and private. However, this collides with Room
  // as it wants an empty public constructor.
  public Run() {
  }

  /**
   * Builder pattern allows Model objects to be constructed in absence of some values, whilst at the
   * same time requiring the essential ones via contructor.
   */
  public static class Builder {

    private String date;
    private double distance;
    private String location;
    private String duration;
    private float pace;
    private RunCoordinates runCoordinates;
    private String runType;
    private float temperature;
    private int weatherType;

    /**
     * Minimum required information
     */
    @SuppressLint("DefaultLocale")
    public Builder(long date, double distance, int duration) {
      Date obj = new Date(date);
      @SuppressLint("SimpleDateFormat") DateFormat dateFormatter = new SimpleDateFormat(
          "d MMM yyyy");
      this.date = dateFormatter.format(obj);

      this.distance = distance;
      this.pace = LocationRepository.calculatePace(distance, duration);

      //hh:mm:ss
      this.duration = getFormattedTime(duration);
      this.location = getLocationRepository().getCountyLiveData().getValue();
    }

    /**
     * Fails if serialization of coordinate fails
     */
    public Builder withRunCoordinates(byte[] runCoordinates) {
      this.runCoordinates = RunCoordinates.fromByteArray(runCoordinates);
      return this;
    }

    public Builder withRunType(String runType) {
      this.runType = runType;
      return this;
    }

    public Builder withTemperature(float temperature) {
      this.temperature = temperature;
      this.weatherType = WeatherParser.parseTemperatureToClassifier(temperature).getValue();
      return this;
    }

    public Run build() {
      Run run = new Run();
      run.date = this.date;
      run.distance = this.distance;
      run.duration = this.duration;
      run.pace = this.pace;
      run.runCoordinates = this.runCoordinates;
      run.runType = this.runType;
      run.temperature = this.temperature;
      run.weatherType = this.weatherType;
      run.location = this.location;

      return run;
    }
  }

  /**
   * @param seconds
   * @return Formatted time in hours:minutes:seconds
   */
  @SuppressLint("DefaultLocale")
  public static String getFormattedTime(int seconds) {
    final String format;
    format = String.format(TIME_FORMAT,
        TimeUnit.SECONDS.toHours(seconds),
        TimeUnit.SECONDS.toMinutes(seconds) -
            TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds)),
        seconds -
            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
    return format;
  }

}
