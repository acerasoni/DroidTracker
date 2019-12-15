package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.Navigation;
import com.ltm.runningtracker.R;
import com.survivingwithandroid.weather.lib.model.Weather;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onClick(View v) {
    LiveData<Weather> weatherLiveData = getWeatherRepository().getLiveDataWeather();

    // Create the observer which updates the UI.
    final Observer<Weather> weatherObserver = weather -> {
      // Update the UI, in this case, a TextView.
      ((TextView) findViewById(R.id.temperatureField)).setText(Float.toString(weather.temperature.getTemp()));
    };

    weatherLiveData.observe(this, weatherObserver);
    getWeatherRepository().requestWeatherUpdates(this);
  }

  public void stopService(View v) {
    getWeatherRepository().removeUpdates(this);
  }

  public void navigate(MenuItem m) {
    Navigation.findNavController(this,R.id.fragment2).navigate(R.id.performanceFragment);
  }
}
