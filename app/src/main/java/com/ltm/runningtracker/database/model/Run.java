package com.ltm.runningtracker.database.model;

import static com.ltm.runningtracker.RunningTrackerApplication.getLocationRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.COORDINATES_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DATE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DISTANCE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.DURATION_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.ID_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.NAME_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.PACE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TEMPERATURE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.TYPE_COL;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.WEATHER_COL;
import static com.ltm.runningtracker.util.Constants.TIME_FORMAT;

import android.annotation.SuppressLint;
import android.database.Cursor;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.util.RunCoordinates;
import com.ltm.runningtracker.util.Serializer;
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.parser.WeatherParser;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Entity
public class Run implements Serializable {

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


  /**
   * Used by Room
   *
   * @see #fromCursorToRun(Cursor, int)
   */
  public Run(int _id, String location, String date, String runType, double distance,
      String duration, int weatherType, float temperature, RunCoordinates runCoordinates,
      float pace) {
    this._id = _id;
    this.location = location;
    this.date = date;
    this.runType = runType;
    this.distance = distance;
    this.duration = duration;
    this.weatherType = weatherType;
    this.temperature = temperature;
    this.runCoordinates = runCoordinates;
    this.pace = pace;
  }

  // Used by builder pattern - requires empty private constructor
  @Ignore
  private Run() {
  }

  /**
   * Builder pattern allows Model objects to be constructed in absence of some values, whilst at the
   * same time requiring the essential ones via contructor. Absent values could include _id, if the
   * run is being retrieved via query.
   */
  public static class Builder {

    private Integer _id = null;
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
    public Builder(String date, double distance, String duration, float pace) {
      this.date = date;
      this.distance = distance;
      this.pace = pace;

      //hh:mm:ss
      this.duration = duration;
      this.location = getLocationRepository().getCountyLiveData().getValue();
    }

    // If run is being retrieved, insert _id of Room record
    public Builder withId(int _id) {
      this._id = _id;
      return this;
    }

    /**
     * Fails if serialization of coordinate fails
     */
    public Builder withRunCoordinates(RunCoordinates runCoordinates) {
      this.runCoordinates = runCoordinates;
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
      if (this._id != null) {
        run._id = this._id;
      }
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

  /**
   * @return Formatted date in d MMM yyy
   */
  public static String getFormattedDate(long date) {
    Date obj = new Date(date);
    @SuppressLint("SimpleDateFormat") DateFormat dateFormatter = new SimpleDateFormat(
        "d MMM yyyy");
    return dateFormatter.format(obj);
  }

  /**
   * Type conversion utility method to return Model Object rather than Cursor Keeps database schema
   * changes from breaking Activity implementations.
   */
  @Ignore
  public static Run fromCursorToRun(Cursor cursor) {
      int id = cursor.getInt(ID_COL);
      String date = cursor.getString(DATE_COL);
      String runType = cursor.getString(TYPE_COL);
      double distance = cursor.getDouble(DISTANCE_COL);
      float pace = cursor.getFloat(PACE_COL);
      String duration = cursor.getString(DURATION_COL);
      float temperature = cursor.getFloat(TEMPERATURE_COL);
      byte[] runCoordinates = cursor.getBlob(COORDINATES_COL);

      return new Run.Builder(date, distance, duration, pace).withId(id)
          .withRunType(runType).withTemperature(temperature)
          .withRunCoordinates(Serializer.runCoordinatesFromByteArray(runCoordinates))
          .build();
    }

  /**
   * Need to override in order for the HashSet in RunRepository to correctly identify to Runs as the
   * same object given the same _id.
   */
  @Ignore
  @Override
  public boolean equals(Object obj) {
    Run run = (Run) obj;
    return this._id == run._id;
  }

}
