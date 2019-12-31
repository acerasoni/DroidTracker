package com.ltm.runningtracker.util;

import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;
import java.util.HashMap;
import java.util.Map;

public class RunTypeParser {

  public enum RunTypeClassifier {
    UNTAGGED("untagged", 0), WALK("walk", 1), JOG("jog", 2), RUN("run", 3), SPRINT("sprint", 4);

    public static final String WALK_VALUE = "walk";
    public static final String JOG_VALUE = "jog";
    public static final String RUN_VALUE= "run";
    public static final String SPRINT_VALUE = "sprint";

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
