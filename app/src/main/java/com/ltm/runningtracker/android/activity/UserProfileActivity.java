package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.android.activity.MainScreenActivity.setupToolbar;
import static com.ltm.runningtracker.util.Constants.ALL_FIELDS_INVALID;
import static com.ltm.runningtracker.util.Constants.NAME_INVALID_ERROR;
import static com.ltm.runningtracker.util.Constants.WEIGHT_HEIGHT_INVALID_ERROR;
import static com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier.*;

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
import com.ltm.runningtracker.util.parser.RunTypeParser.RunTypeClassifier;
import java.util.EnumMap;

/**
 * This Activity allows the user to:
 *
 * 1. Create a new user profile if one does not exist 2. Modify its user profile
 *
 * Additionally, it displays the average pace across all runs of any given type. As newly completed
 * runs are 'untagged', they don't influence the average pace of any run type. However, once they
 * are tagged in the BrowseRunDetailsActivity, they will be accounted for when computing averages.
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
    EnumMap<RunTypeClassifier, Float> paces = userProfileActivityViewModel
        .getUserAveragePaces(this);

    StringBuilder sb = new StringBuilder();
    populatePace(walkingPaceField, paces.get(WALK), sb);
    populatePace(joggingPaceField, paces.get(JOG), sb);
    populatePace(runningPaceField, paces.get(RUN), sb);
    populatePace(sprintingPaceField, paces.get(SPRINT), sb);
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

    nameField.setText(user.name);
    weightField.setText(Integer.toString(user.weight));
    heightField.setText(Integer.toString(user.height));
  }

  private void initialiseViews() {
    setupToolbar(this, R.id.toolbar5);
    userProfileActivityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);

    nameField = findViewById(R.id.nameField);
    weightField = findViewById(R.id.weightField);
    heightField = findViewById(R.id.heightField);
    walkingPaceField = findViewById(R.id.walkingPaceField);
    joggingPaceField = findViewById(R.id.joggingPaceField);
    runningPaceField = findViewById(R.id.runningPaceField);
    sprintingPaceField = findViewById(R.id.sprintingPaceField);
  }
}