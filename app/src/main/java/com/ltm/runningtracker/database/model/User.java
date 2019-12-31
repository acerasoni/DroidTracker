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

    /**
     * Minimum required information
     */
    public Builder(String name) {
      this.name = name;
    }

    public User.Builder withWeight(int weight) {
      this.weight = weight;
      return this;
    }

    public User.Builder withHeight(int height) {
      this.height = height;
      return this;
    }

    public User build() {
      User user = new User();
      user.name = this.name;
      user.weight = this.weight;
      user.height = this.height;

      return user;
    }
  }

}
