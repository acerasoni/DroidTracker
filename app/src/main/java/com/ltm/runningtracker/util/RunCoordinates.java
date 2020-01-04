package com.ltm.runningtracker.util;

import com.mapbox.mapboxsdk.geometry.LatLng;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structure containing run's start & end latitude and longitude key-value pair. Serialized to
 * byte[] to be passed into ContentValues, to be stored in DB as a complex entity
 */
public class RunCoordinates implements Serializable {

  private List<Coordinate> runCoordinates;

  public RunCoordinates() {
    runCoordinates = new ArrayList<>();
  }

  public List<Coordinate> getRunCoordinates() {
    return runCoordinates;
  }

  public void addCoordinate(Coordinate coordinate) {
    this.runCoordinates.add(coordinate);
  }

  public static class Coordinate implements Serializable {

    private float x;
    private float y;

    public Coordinate(float x, float y) {
      this.x = x;
      this.y = y;
    }

    public float getX() {
      return x;
    }

    public float getY() {
      return y;
    }

    public LatLng toLatLng() {
      return new LatLng(x, y);
    }
  }

}
