package com.ltm.runningtracker.util;

import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import java.util.HashMap;
import java.util.Map;

public class RunTypeParser {

  public enum RunTypeClassifier {
    WALK("walk", 0), JOG("jog", 1), RUN("run", 2), SPRINT("sprint", 3);

    private String runTypeDefinition;
    private int value;
    private static Map map = new HashMap<>();

    private RunTypeClassifier(String brand, int value) {
      this.runTypeDefinition = brand;
      this.value = value;
    }

    static {
      for (RunTypeParser.RunTypeClassifier runTypeClassifier : RunTypeParser.RunTypeClassifier.values()) {
        map.put(runTypeClassifier.value, runTypeClassifier);
      }
    }

    public static RunTypeParser.RunTypeClassifier valueOf(int runTypeClassifier) {
      return (RunTypeParser.RunTypeClassifier) map.get(runTypeClassifier);
    }

    public int getValue() {
      return value;
    }

    @Override
    public String toString() {
      return runTypeDefinition;
    }
  }

}
