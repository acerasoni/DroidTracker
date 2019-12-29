package com.ltm.runningtracker.database.model;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.util.WeatherParser.parseTemperatureToString;

import android.util.Log;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.WeatherParser;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Storing speed and distance can be considered redundant as this can be derived at runtime, though
 * it is a tradeoff between runtime resources vs having an additional column in the database. We
 * saved parsed temperature because it will allow a custom cursor on specific temperature types
 */
@Entity
public class Run {

  @PrimaryKey(autoGenerate = true)
  public int _id;

  // Store as milliseconds since 1970
  @ColumnInfo(name = "date")
  public long date;

  @ColumnInfo(name = "runType")
  public int runType;

  @ColumnInfo(name = "weatherType")
  public int weatherType;

  @ColumnInfo(name = "temperature")
  public float temperature;

  // Store as milliseconds
  @ColumnInfo(name = "duration")
  public long duration;

  @ColumnInfo(name = "runCoordinates")
  public RunCoordinates runCoordinates;

  @ColumnInfo(name = "distance")
  public double distance;

  // Pace is always < 100, can store as float
  @ColumnInfo(name = "average_speed")
  public float pace;

  // Builder pattern usually wants class constructor to be empty and private. However, this collides with Room
  // as it wants an empty public constructor.
  public Run() {
  }

  /**
   * We need a builder pattern as some values could be unavailable - Fetching weather/temperature -
   * IOException when serializing location - Run runType tagging
   */
  public static class Builder {

    private long date;
    private double distance;
    private long duration;
    private float pace;
    private RunCoordinates runCoordinates;
    private int runType;
    private float temperature;
    private int weatherType;

    /**
     * Minimum required information
     */
    public Builder(long date, double distance, long duration) {
      this.date = date;
      this.distance = distance;
      this.duration = duration;
      this.pace = LocationRepository.calculatePace(distance, duration);
    }

    /**
     * Fails if serialization of coordinate fails
     */
    public Builder withRunCoordinates(byte[] runCoordinates) {
      this.runCoordinates = RunCoordinates.fromByteArray(runCoordinates);
      return this;
    }

    public Builder withRunType(int runType) {
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

      return run;
    }
  }

}
