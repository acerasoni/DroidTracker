package com.ltm.runningtracker.database.model;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;

import android.annotation.SuppressLint;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.WeatherParser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Storing speed and distance can be considered redundant as this can be derived at runtime, though
 * it is a tradeoff between runtime resources vs having an additional column in the database. We
 * saved parsed temperature because it will allow a custom cursor on specific temperature types
 */
@Entity
public class Run {

  @PrimaryKey(autoGenerate = true)
  public int _id;

  @ColumnInfo(name = "location")
  public String location;

  @ColumnInfo(name = "date")
  public String date;

  @ColumnInfo(name = "runType")
  public String runType;

  @ColumnInfo(name = "distance")
  public double distance;

  @ColumnInfo(name = "duration")
  public String duration;

  @ColumnInfo(name = "weatherType")
  public int weatherType;

  @ColumnInfo(name = "temperature")
  public float temperature;

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
   * We need a builder pattern as some values could be unavailable - Fetching weather/temperature -
   * IOException when serializing location - Run runType tagging
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
      @SuppressLint("SimpleDateFormat") DateFormat dateFormatter = new SimpleDateFormat("d MMM yyyy");
      this.date = dateFormatter.format(obj);

      this.distance = distance;
      this.pace = LocationRepository.calculatePace(distance, duration);
      this.duration = String.format("%02d min, %02d sec",
          TimeUnit.SECONDS.toMinutes(duration),
          TimeUnit.SECONDS.toSeconds(duration) -
              TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration))
      );

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

}
