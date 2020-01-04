package com.ltm.runningtracker.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
   * Builder pattern required as described in {@link Run}
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
