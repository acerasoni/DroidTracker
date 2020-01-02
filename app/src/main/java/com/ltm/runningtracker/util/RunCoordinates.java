package com.ltm.runningtracker.util;

import android.util.Log;
import androidx.room.TypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Data structure containing run's start & end latitude and longitude key-value pair.
 * Serialized to byte[] to be passed into ContentValues, to be stored in DB as a complex entity
 */
public class RunCoordinates implements Serializable {

  private float startLat;
  private float startLon;
  private float endLat;
  private float endLon;

  public RunCoordinates(float startLat, float startLon, float endLat, float endLon) {
    this.startLat = startLat;
    this.startLon = startLon;
    this.endLat = endLat;
    this.endLon = endLon;
  }

  /**
   * Used by Room DB as TypeConverter and in RunActivity to serialize coordinates to be passed in
   * ContentValue object.
   * @param runCoordinates object
   * @return stream of bytes
   */
  @TypeConverter
  public static byte[] toByteArray(RunCoordinates runCoordinates) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(runCoordinates);
      oos.flush();
      return bos.toByteArray();
    } catch (IOException e) {
      Log.e("Run Coordinates: ", "Error serializing location. Will be set to null.");
      return null;
    }
  }

  /**
   * Used by Room DB as TypeConverter to deserialize a stream of bytes into a RunCoordinates object
   * @param bytes to be deserialized
   * @return RunCoordinates object
   */
  @TypeConverter
  public static RunCoordinates fromByteArray(byte[] bytes) {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);
      ObjectInputStream is = new ObjectInputStream(in);
      return (RunCoordinates) is.readObject();
    } catch (IOException e) {
      Log.e("Run Coordinates: ", "Error deserializing location. Will be set to null.");
      return null;
    } catch (ClassNotFoundException e) {
      Log.e("Run Coordinates:", "Class not found.");
      return null;
    }
  }

}
