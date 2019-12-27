package com.ltm.runningtracker.exception;

public class InvalidLatitudeOrLongitudeException extends RuntimeException{
  public InvalidLatitudeOrLongitudeException(String message) {
    super(message);
  }
}
