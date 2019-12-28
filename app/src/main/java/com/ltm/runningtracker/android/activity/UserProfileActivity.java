package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.database.User;

public class UserProfileActivity extends AppCompatActivity {

  EditText nameField, weightField, heightField;
  TextView paceField;
  boolean creatingUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    nameField = findViewById(R.id.nameField);
    weightField = findViewById(R.id.weightField);
    heightField = findViewById(R.id.heightField);
    paceField = findViewById(R.id.paceField);

    creatingUser =
        getIntent().getIntExtra("request", -1) == MainScreenActivity.USER_MODIFICATION_REQUEST;

    if (creatingUser) {
      populateViews();
    }
  }

  public void populateViews() {
    User user = getUserRepository().getUser();
    nameField.setText(user.getName());
    weightField.setText(user.getWeight());
    heightField.setText(user.getHeight());
    paceField.setText(user.get);
  }

  public void onSaveButton(View v) {
    String s1, s2;
    int s3, s4;
    s1 = t1.getText().toString();
    s2 = t2.getText().toString();
    s3 = Integer.parseInt(t3.getText().toString());
    s4 = Integer.parseInt(t4.getText().toString());

    getUserRepository().createUser(s1, s2, s3, s4);
    Intent returnIntent = new Intent();
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

}
