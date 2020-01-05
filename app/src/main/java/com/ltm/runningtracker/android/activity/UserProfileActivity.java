package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.util.Constants.ALL_FIELDS_INVALID;
import static com.ltm.runningtracker.util.Constants.NAME_INVALID_ERROR;
import static com.ltm.runningtracker.util.Constants.WEIGHT_HEIGHT_INVALID_ERROR;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel;
import com.ltm.runningtracker.database.model.User;

/**
 * This Activity allows the user to:
 *
 * 1. Create a new user profile if one does not exist 2. Modify its user profile
 *
 * Additionally, it displays the average pace across all runs of any given type. As newly completed
 * runs are 'untagged', they don't influence the average pace of any run type. However, once they
 * are tagged in the BrowseRunDetailsActivity, they will be accounted for when computing averages.
 *
 * An additional text field will display the BMI computed from the weight and height inserted.
 */
public class UserProfileActivity extends AppCompatActivity {

  // Views
  private EditText nameField;
  private EditText weightField;
  private EditText heightField;
  private TextView walkingPaceField;
  private TextView joggingPaceField;
  private TextView runningPaceField;
  private TextView sprintingPaceField;
  private TextView bmiField;

  // User-related
  private boolean creatingUser;
  private int weight = -1;
  private int height = -1;

  private ActivityViewModel userProfileActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    initialiseViews();
    setupBmiListeners();

    creatingUser =
        getIntent().getIntExtra(getResources().getString(R.string.request), -1)
            != MainScreenActivity.USER_MODIFICATION_REQUEST;

    if (!creatingUser) {
      populateViews();
      AsyncTask.execute(this::calculatePaces);
    }
  }

  public void onSaveButton(View v) {
    boolean weightAndHeightInvalid = false;
    boolean nameInvalid = false;

    try {
      height = Integer.parseInt(heightField.getText().toString());
      weight = Integer.parseInt(weightField.getText().toString());
    }// I height or weight blank show error and return from method - allowing the user to try again
    catch (NumberFormatException e) {
      weightAndHeightInvalid = true;
    }

    // If name blank show error and return from method - allowing the user to try again
    String name = nameField.getText().toString();
    if (name.equals("")) {
      nameInvalid = true;
    }

    if (weightAndHeightInvalid && nameInvalid) {
      Toast.makeText(this, ALL_FIELDS_INVALID, Toast.LENGTH_LONG).show();
      return;
    } else if (weightAndHeightInvalid) {
      Toast.makeText(this, WEIGHT_HEIGHT_INVALID_ERROR, Toast.LENGTH_LONG).show();
      return;
    } else if (nameInvalid) {
      Toast.makeText(this, NAME_INVALID_ERROR, Toast.LENGTH_LONG).show();
      return;
    }

    userProfileActivityViewModel.saveUser(this, creatingUser, name, weight, height);

    Intent returnIntent = new Intent();
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

  /**
   * Will populate the average pace fields.
   */
  private void calculatePaces() {
    Float[] paces = userProfileActivityViewModel.getUserAveragePaces(this);

    StringBuilder sb = new StringBuilder();
    populatePace(walkingPaceField, paces[0], sb);
    populatePace(joggingPaceField, paces[1], sb);
    populatePace(runningPaceField, paces[2], sb);
    populatePace(sprintingPaceField, paces[3], sb);
  }

  private void populatePace(TextView textView, Float pace, StringBuilder sb) {
    if (pace == null) {
      textView.setText(getResources().getString(R.string.data_unavailable));
    } else {
      sb = new StringBuilder(String.format(getResources().
          getString(R.string.two_decimals_format), pace)).
          append(" ").append(getResources().getString(R.string.kilometers_per_hour));
      textView.setText(sb.toString());
    }
  }

  private void populateViews() {
    User user = userProfileActivityViewModel.getUser().getValue();
    float bmi = calculateBMI(user.weight, user.height);

    nameField.setText(user.name);
    weightField.setText(Integer.toString(user.weight));
    heightField.setText(Integer.toString(user.height));
    bmiField.setText(Float.toString(bmi));
  }

  /**
   * This method will setup listeners to determine when the user clicks away from weight or height
   * fields. It will then read the values off both fields, compute the BMI and display it below.
   */
  private void setupBmiListeners() {
    weightField.setOnFocusChangeListener((v, hasFocus) -> {
      if (!hasFocus) {
        String text = weightField.getText().toString();
        if (!text.equals("")) {
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
        if (!text.equals("")) {
          height = Integer.parseInt(text);
          if (weight > 0) {
            bmiField.setText(Float.toString(calculateBMI(weight, height)));
          }
        }

      }
    });
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

  private void initialiseViews() {
    userProfileActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    nameField = findViewById(R.id.nameField);
    weightField = findViewById(R.id.weightField);
    heightField = findViewById(R.id.heightField);
    walkingPaceField = findViewById(R.id.walkingPaceField);
    joggingPaceField = findViewById(R.id.joggingPaceField);
    runningPaceField = findViewById(R.id.runningPaceField);
    sprintingPaceField = findViewById(R.id.sprintingPaceField);
    bmiField = findViewById(R.id.bmiField);
  }
}
