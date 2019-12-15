package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTracker.getWeatherRepository;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.User;
import com.survivingwithandroid.weather.lib.model.Weather;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onClick(View v) {
    getWeatherRepository();
    LiveData<Weather> weatherLiveData = getWeatherRepository().getLiveDataWeather();

    // Create the observer which updates the UI.
    final Observer<Weather> weatherObserver = weather -> {
      // Update the UI, in this case, a TextView.
      ((TextView) findViewById(R.id.temperatureField)).setText(weather.temperature.toString());
    };

    weatherLiveData.observe(this, weatherObserver);
    getWeatherRepository().requestWeatherUpdates(this);
  }

  public void stopService(View v) {
    getWeatherRepository().weatherManager.removeUpdates();
  }

}
