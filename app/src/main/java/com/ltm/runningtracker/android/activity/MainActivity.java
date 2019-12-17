package com.ltm.runningtracker.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.MainScreenViewModel;
import com.ltm.runningtracker.parcelable.User;
import com.survivingwithandroid.weather.lib.model.Weather;

public class MainActivity extends AppCompatActivity {

  MainScreenViewModel mainScreenViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mainScreenViewModel = ViewModelProviders.of(this).get(MainScreenViewModel.class);
    mainScreenViewModel.getLocation().observe(this, location -> {
      ((TextView) findViewById(R.id.latField)).setText("" + location.getLatitude());
      ((TextView) findViewById(R.id.lonField)).setText("" + location.getLongitude());
    });

    mainScreenViewModel.getWeather().observe(this, weather -> {
      ((TextView) findViewById(R.id.temperatureField)).setText("" + weather.temperature.getTemp());
    });
  }

  public void onClick(View v) {


//    // Create the observer which updates the UI.
//    final Observer<Weather> weatherObserver = weather -> {
//      // Update the UI, in this case, a TextView.
//      Log.d("Weather", Float.toString(weather.temperature.getTemp()));
//      ((TextView) findViewById(R.id.temperatureField)).setText(parseWeatherToClassifier(weather).toString());
//    };

  }

  public void stopService(View v) {

  }

  public void navigate(MenuItem m) {
    Navigation.findNavController(this,R.id.fragment2).navigate(R.id.performanceFragment);
  }

  public void startRun(MenuItem m) {
    startActivity(new Intent(this, RunActivity.class));
  }
}
