package com.ltm.runningtracker.android.activity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.ltm.runningtracker.R;

public class PerformanceTypeSelectorActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance_type_selector);
  }

  public void onSelectDiet(View v) {
    startActivity(new Intent(this, DietPerformanceActivity.class));
  }

  public void onSelectWeather(View v) {
    startActivity(new Intent(this, WeatherPerformanceActivity.class));
  }

}
