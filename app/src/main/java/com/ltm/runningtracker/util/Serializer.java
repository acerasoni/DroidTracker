package com.ltm.runningtracker.util;

import android.util.Log;
import androidx.room.TypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class contains methods required to serialize and deserialize objects.
 */
public class Serializer {

  /**
   * Used by Room DB as TypeConverter, in RunActivity to serialize coordinates to be passed in
   * ContentValue object. We required this wrapper method because Room cannot use unbound generics
   * in Type Converters.
   *
   * @return serialized byte[]
   */
  @TypeConverter
  public static byte[] runCoordinatesToByteArray(RunCoordinates runCoordinates) {
    return toByteArray(runCoordinates);
  }

  @TypeConverter
  public static RunCoordinates runCoordinatesFromByteArray(byte[] bytes) {
    RunCoordinates runCoordinates = fromByteArray(bytes);
    return runCoordinates;
  }

  /**
   * Used by type converters, and to serialize User objects to be passed to Content
   * Provider. This operation works with unbound generics.
   *
   * @param object to be serialized
   * @return a stream of bytes
   * @see <a href="https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array">
   * Serializing objects in Java</a>
   */
  public static <T> byte[] toByteArray(T object) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(object);
      return bos.toByteArray();
    } catch (IOException e) {
      Log.e("Object to byte[] conversion: ", "Error serializing. Will be set to null.");
      return null;
    } finally {
      // Close resource
      try {
        bos.close();
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }

  /**
   * Used by TypeConverter to deserialize a stream of bytes into a RunCoordinates object.
   * Used natively by the Content Provider to derive User objects from Content Values.
   *
   * @param bytes to be deserialized
   * @return RunCoordinates object
   */
  public static <T> T fromByteArray(byte[] bytes) {
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    ObjectInput is;
    try {
      is = new ObjectInputStream(in);
      return (T) is.readObject();
    } catch (IOException e) {
      Log.e("Run Coordinates: ", "Error deserializing location. Will be set to null.");
      return null;
    } catch (ClassNotFoundException e) {
      Log.e("Run Coordinates:", "Class not found.");
      return null;
    } finally {
      // Close resource
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }

}
