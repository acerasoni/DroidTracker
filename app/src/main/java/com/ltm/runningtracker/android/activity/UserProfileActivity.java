package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.database.User;

public class UserProfileActivity extends AppCompatActivity {

  EditText t1, t2, t3, t4;
  boolean creatingUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    t1 = findViewById(R.id.nameField);
    t2 = findViewById(R.id.dietField);
    t3 = findViewById(R.id.weightField);
    t4 = findViewById(R.id.heightField);

    creatingUser =
        getIntent().getIntExtra("request", -1) == MainScreenActivity.USER_MODIFICATION_REQUEST;

    if (creatingUser) {
      populateViews();
    }

    Button button = findViewById(R.id.saveButton);
    button.setOnClickListener(v -> {
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
    });
  }

  public void populateViews() {
    User user = getUserRepository().getUser();
    t1.setText(user.getName());
    t2.setText(user.getDietName());
    t3.setText(user.getBmi() + "");
  }

}
