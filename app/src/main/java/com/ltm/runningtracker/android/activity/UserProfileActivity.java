package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;

import android.app.Activity;
import android.content.Intent;
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
  TextView walkingPaceField, joggingPaceField, runningPaceField, bmiField;
  boolean creatingUser;
  private int weight = -1, height = -1;

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
    bmiField = findViewById(R.id.bmiField);

    setupBmiListeners();

    creatingUser =
        getIntent().getIntExtra("request", -1) != MainScreenActivity.USER_MODIFICATION_REQUEST;

    if (!creatingUser) {
      populateViews();
    } else {
      populatePaces();
    }
  }

  public void populatePaces() {

  }

  public void populateViews() {
    User user = getUserRepository().getUser();
    float bmi = calculateBMI(user.weight, user.weight, true);

    nameField.setText(user.name);
    weightField.setText(Integer.toString(user.weight));
    heightField.setText(Integer.toString(user.height));
    walkingPaceField.setText(parsePace(user.walkingPace, RunTypeClassifier.WALK));
    joggingPaceField.setText(parsePace(user.joggingPace, RunTypeClassifier.JOG));
    runningPaceField.setText(parsePace(user.runningPace, RunTypeClassifier.RUN));
    bmiField.setText(Float.toString(bmi));
  }

  public void onSaveButton(View v) {
    String name, height, weight;
    name = nameField.getText().toString();
    height = heightField.getText().toString();
    weight = weightField.getText().toString();

    if (creatingUser) {
      getUserRepository()
          .createUser(name.trim(), Integer.parseInt(weight.trim()), Integer.parseInt(height.trim()));
    } else {
      getUserRepository().updateUser(name.trim(), Integer.parseInt(weight.trim()), Integer.parseInt(height.trim()));
    }
    Intent returnIntent = new Intent();
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

  /**
   * https://www.cdc.gov/healthyweight/assessing/bmi/childrens_bmi/childrens_bmi_formula.html
   *
   * @param weight in kg for metric, lbs for imperial
   * @param height in cm for metric, in for imperial
   */
  private float calculateBMI(int weight, int height, boolean isMetric) {
    return isMetric ? calculateMetricBMI(weight, height) : calculateImperialBMI(weight, height);
  }

  private float calculateMetricBMI(int weight, int height) {
    float metres = height / 100;
    return (weight / (metres * metres));
  }

  private float calculateImperialBMI(int weight, int height) {
    return 703 * weight / (height * height);
  }

  private void setupBmiListeners() {
        weightField.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus) {
        String text = weightField.getText().toString();
        if (text != null && !text.equals("")) {
          weight = Integer.parseInt(text);
          if (height > 0) {
            bmiField.setText(Float.toString(calculateBMI(weight, height, true)));
          }
        }
      }
    });

    heightField.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus) {
        String text = heightField.getText().toString();
        if(text != null && !text.equals("")){
          height = Integer.parseInt(text);
          if(weight > 0) {
            bmiField.setText(Float.toString(calculateBMI(weight, height, true)));
          }
        }

      }
    });
  }

  private String parsePace(float pace, RunTypeClassifier runTypeClassifier) {
    if(pace != Float.MIN_VALUE) {
      return Float.toString(pace);
    } else {
      StringBuilder sb = new StringBuilder("Data unavailable for ");
      sb.append(runTypeClassifier.toString()).append(" activity");
      return sb.toString();
    }
  }

}
