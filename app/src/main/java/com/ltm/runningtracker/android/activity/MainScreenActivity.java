package com.ltm.runningtracker.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProviders;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.MainActivityViewModel;

public class MainScreenActivity extends AppCompatActivity {

  private MainActivityViewModel mainActivityViewModel;
  private TextView weatherTextField;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_screen);

    weatherTextField = findViewById(R.id.weatherField);
    weatherTextField.setText("Fetching weather...");

    mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

    // observe location object
    mainActivityViewModel.getLocation().observe(this, location -> {
    });

    // observe weather object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      weatherTextField.setText(Float.toString(weather.temperature.getTemp()) + " °C");
    });

  }

  public void startRunActivity(View v) {
    startActivity(new Intent(this, RunActivity.class));
  }

  public void startPerformanceActivity(View v) {
    startActivity(new Intent(this, PerformanceTypeSelectorActivity.class));
  }
}
