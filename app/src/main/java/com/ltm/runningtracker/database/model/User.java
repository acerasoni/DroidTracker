package com.ltm.runningtracker.database.model;

import android.util.Log;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.ltm.runningtracker.database.model.Run.Builder;
import com.ltm.runningtracker.repository.LocationRepository;
import com.ltm.runningtracker.util.RunCoordinates;
import java.io.IOException;

@Entity
public class User {

  @PrimaryKey
  public int _id;

  @ColumnInfo(name = "name")
  public String name;

  @ColumnInfo(name = "weight")
  public int weight;

  @ColumnInfo(name = "height")
  public int height;

  // Values of Float.MIN_VALUE for paces mean no runs of that type were recorded hence no data available
  // to compute pace
  @ColumnInfo(name = "walkingPace")
  public float walkingPace;

  @ColumnInfo(name = "joggingPace")
  public float joggingPace;

  @ColumnInfo(name = "runningPace")
  public float runningPace;

  public User() {
  }

  /**
   * We need a builder pattern as some values could be unavailable - Pace information
   * User will not have pace relating to walks, for instance, if he never completed a walk.
   * However, he could have a pace related to jogging and running.
   */
  public static class Builder {

    private String name;
    private int weight;
    private int height;
    private float walkingPace;
    private float joggingPace;
    private float runningPace;

    /**
     * Minimum required information
     */
    public Builder(String name, int weight, int height) {
      this.name = name;
      this.weight = weight;
      this.height = height;
    }

    /**
     * Fails if serialization of coordinate fails
     */
    public User.Builder withWalkingPace(float walkingPace) {
      this.walkingPace = walkingPace;
      return this;
    }

    public User.Builder withJoggingPace(float joggingPace) {
      this.joggingPace = joggingPace;
      return this;
    }

    public User.Builder withRunningPace(float runningPace) {
      this.runningPace = runningPace;
      return this;
    }

    public User build() {
      User user = new User();
      user.name = this.name;
      user.weight = this.weight;
      user.height = this.height;
      user.walkingPace = this.walkingPace;
      user.joggingPace = this.joggingPace;
      user.runningPace = this.runningPace;

      return user;
    }
  }

}
