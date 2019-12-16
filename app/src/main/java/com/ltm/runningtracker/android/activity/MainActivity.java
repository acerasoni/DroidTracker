package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getWeatherRepository;
import static com.ltm.runningtracker.util.WeatherParser.parseWeatherToClassifier;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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
    mainScreenViewModel.getUser().observe(this, user -> {
      updateTemp(user.getLol());
    });
  }

  private void updateTemp(float f) {
    ((TextView) findViewById(R.id.temperatureField)).setText("" + f);
  }

  public void onClick(View v) {


    mainScreenViewModel.setUser(new User());


//    // Create the observer which updates the UI.
//    final Observer<Weather> weatherObserver = weather -> {
//      // Update the UI, in this case, a TextView.
//      Log.d("Weather", Float.toString(weather.temperature.getTemp()));
//      ((TextView) findViewById(R.id.temperatureField)).setText(parseWeatherToClassifier(weather).toString());
//    };

  }

  public void stopService(View v) {
    getWeatherRepository().removeUpdates(this);
  }

  public void navigate(MenuItem m) {
    Navigation.findNavController(this,R.id.fragment2).navigate(R.id.performanceFragment);
  }

  public void startRun(MenuItem m) {
    startActivity(new Intent(this, RunActivity.class));
  }
}
