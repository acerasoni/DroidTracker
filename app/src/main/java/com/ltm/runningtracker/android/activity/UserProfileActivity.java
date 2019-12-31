package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.DroidProviderContract.RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.DroidUriMatcher.RUNS;
import static com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier.JOG_VALUE;
import static com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier.RUN_VALUE;
import static com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier.SPRINT_VALUE;
import static com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier.WALK;
import static com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier.WALK_VALUE;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.database.model.User;
import com.ltm.runningtracker.util.RunTypeParser.RunTypeClassifier;
import com.ltm.runningtracker.util.WeatherParser.WeatherClassifier;

public class UserProfileActivity extends AppCompatActivity {

  EditText nameField, weightField, heightField;
  TextView walkingPaceField, joggingPaceField, runningPaceField, sprintingPaceField, bmiField;
  boolean creatingUser;
  private int weight = -1, height = -1;
  private Float walkingPace, joggingPace, runningPace, sprintingPace;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    nameField = findViewById(R.id.nameField);
    weightField = findViewById(R.id.weightField);
    heightField = findViewById(R.id.heightField);
    walkingPaceField = findViewById(R.id.walkingPaceField);
    joggingPaceField = findViewById(R.id.joggingPaceField);
    runningPaceField = findViewById(R.id.runningPaceField);
    sprintingPaceField = findViewById(R.id.sprintingPaceField);
    bmiField = findViewById(R.id.bmiField);

    setupBmiListeners();

    creatingUser =
        getIntent().getIntExtra("request", -1) != MainScreenActivity.USER_MODIFICATION_REQUEST;

    if (!creatingUser) {
      populateViews();
      AsyncTask.execute(
          () -> calculatePaces());
    } else {
      populatePaces();
    }
  }

  public void calculatePaces() {
    Cursor c = getContentResolver().query(RUNS_URI, null, null, null, null);
    if (c.moveToFirst()) {
      do {
        switch (RunTypeClassifier.valueOf(c.getString(3).toUpperCase())) {
          case WALK:
            if (walkingPace == null) {
              walkingPace = c.getFloat(9);
            }
            walkingPace = calculateAverage(walkingPace, c.getFloat(9));
            break;
          case JOG:
            if (joggingPace == null) {
              joggingPace = c.getFloat(9);
            }
            joggingPace = calculateAverage(joggingPace, c.getFloat(9));
            break;
          case RUN:
            if (runningPace == null) {
              runningPace = c.getFloat(9);
            }
            runningPace = calculateAverage(runningPace, c.getFloat(9));
            break;
          case SPRINT:
            if (sprintingPace == null) {
              sprintingPace = c.getFloat(9);
            }
            sprintingPace = calculateAverage(sprintingPace, c.getFloat(9));
            break;

        }
      } while (c.moveToNext());
    }

    StringBuilder sb;

    if (walkingPace == null) {
      walkingPaceField.setText("Unavailable");
    } else {
      sb = new StringBuilder(String.format("%.2f", walkingPace)).append(" km/h");
      walkingPaceField.setText(sb.toString());
    }
    if (joggingPace == null) {
      joggingPaceField.setText("Unavailable");
    } else {
      sb = new StringBuilder(String.format("%.2f", joggingPace)).append(" km/h");
      joggingPaceField.setText(sb.toString());
    }
    if (runningPace == null) {
      runningPaceField.setText("Unavailable");
    } else {
      sb = new StringBuilder(String.format("%.2f", runningPace)).append(" km/h");
      runningPaceField.setText(sb.toString());
    }
    if (sprintingPace == null) {
      sprintingPaceField.setText("Unavailable");
    } else {
      sb = new StringBuilder(String.format("%.2f", sprintingPace)).append(" km/h");
      sprintingPaceField.setText(sb.toString());
    }

  }

  public void populatePaces() {

  }

  public void populateViews() {
    User user = getUserRepository().getUser();
    float bmi = calculateBMI(user.weight, user.height);

    nameField.setText(user.name);
    weightField.setText(Integer.toString(user.weight));
    heightField.setText(Integer.toString(user.height));
    bmiField.setText(Float.toString(bmi));
  }

  public void onSaveButton(View v) {
    String name, height, weight;
    name = nameField.getText().toString();
    height = heightField.getText().toString();
    weight = weightField.getText().toString();

    if (creatingUser) {
      getUserRepository()
          .createUser(name.trim(), Integer.parseInt(weight.trim()),
              Integer.parseInt(height.trim()));
    } else {
      getUserRepository().updateUser(name.trim(), Integer.parseInt(weight.trim()),
          Integer.parseInt(height.trim()));
    }
    Intent returnIntent = new Intent();
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

  /**
   * https://www.cdc.gov/healthyweight/assessing/bmi/childrens_bmi/childrens_bmi_formula.html
   *
   * @param weight in kg for
   * @param height in cm for
   */
  private float calculateBMI(int weight, int height) {
    float metres = height / 100;
    metres *= 2;
    return weight / metres;
  }

  private void setupBmiListeners() {
    weightField.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus) {
        String text = weightField.getText().toString();
        if (text != null && !text.equals("")) {
          weight = Integer.parseInt(text);
          if (height > 0) {
            bmiField.setText(Float.toString(calculateBMI(weight, height)));
          }
        }
      }
    });

    heightField.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus) {
        String text = heightField.getText().toString();
        if (text != null && !text.equals("")) {
          height = Integer.parseInt(text);
          if (weight > 0) {
            bmiField.setText(Float.toString(calculateBMI(weight, height)));
          }
        }

      }
    });
  }

  private Float calculateAverage(float a, float b) {
    return (a + b) / 2;
  }

}
