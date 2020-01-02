package com.ltm.runningtracker.util.parser;

import static com.ltm.runningtracker.util.Constants.JOG_STRING;
import static com.ltm.runningtracker.util.Constants.RUN_STRING;
import static com.ltm.runningtracker.util.Constants.SPRINT_STRING;
import static com.ltm.runningtracker.util.Constants.UNTAGGED_STRING;
import static com.ltm.runningtracker.util.Constants.WALK_STRING;

import java.util.HashMap;
import java.util.Map;

public class RunTypeParser {

  public enum RunTypeClassifier {
    UNTAGGED(UNTAGGED_STRING, 0), WALK(WALK_STRING, 1), JOG(JOG_STRING, 2),
    RUN(RUN_STRING, 3), SPRINT(SPRINT_STRING, 4);

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
