package com.ltm.runningtracker.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.MainScreenActivityViewModel;

public class MainScreenActivity extends AppCompatActivity {

  private MainScreenActivityViewModel mainActivityViewModel;
  private TextView weatherTextField, locationTextField;
  private Button runButton, performanceButton, settingsButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);
    mainActivityViewModel = ViewModelProviders.of(this).get(MainScreenActivityViewModel.class);

    runButton = findViewById(R.id.runButton);
    performanceButton = findViewById(R.id.performanceButton);
    settingsButton = findViewById(R.id.settingsButton);

    checkUserExists();

    weatherTextField = findViewById(R.id.weatherField);
    locationTextField = findViewById(R.id.locationField);

    weatherTextField.setText("Fetching weather...");
    locationTextField.setText("Fetching location...");

    // observe location object
    mainActivityViewModel.getCounty().observe(this, county -> {
      locationTextField.setText(county);
    });

    // observe weather object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      weatherTextField.setText(weather.temperature.getTemp() + " °C");
    });

  }

  public void startRunActivity(View v) {
    startActivity(new Intent(this, RunActivity.class));
  }

  public void startPerformanceActivity(View v) {
    startActivity(new Intent(this, PerformanceTypeSelectorActivity.class));
  }

  private void checkUserExists() {
    if (!mainActivityViewModel.doesUserExist()) {
      View.OnClickListener sharedListener = v -> {
        Toast.makeText(getApplicationContext(), "User setup required",
            Toast.LENGTH_LONG).show();
      };
      disableButton(runButton);
      disableButton(performanceButton);
      disableButton(settingsButton);
      runButton.setOnClickListener(sharedListener);
      performanceButton.setOnClickListener(sharedListener);
      settingsButton.setOnClickListener(sharedListener);
    } else {
      Context context = this;
      runButton.setOnClickListener(v -> startActivity(new Intent(context, RunActivity.class)));
      performanceButton.setOnClickListener(
          v -> startActivity(new Intent(context, PerformanceTypeSelectorActivity.class)));
      settingsButton.setOnClickListener(v -> {
        startActivity(new Intent(context, UserProfileActivity.class));
      });
    }
  }

  private static void disableButton(Button button) {
    button.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
  }

  private static void enableButton(Button button) {

  }
}
