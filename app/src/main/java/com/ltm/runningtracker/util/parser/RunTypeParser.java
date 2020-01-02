package com.ltm.runningtracker.util.parser;

import java.util.HashMap;
import java.util.Map;

public class RunTypeParser {

  public enum RunTypeClassifier {
    UNTAGGED("untagged", 0), WALK("walk", 1), JOG("jog", 2), RUN("run", 3), SPRINT("sprint", 4);

    private String runTypeDefinition;
    private int value;
    private static Map map = new HashMap<>();

    RunTypeClassifier(String brand, int value) {
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
