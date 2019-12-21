package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.RUNS_URI;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.activity.viewmodel.MainActivityViewModel;
import com.ltm.runningtracker.android.activity.viewmodel.PerformanceViewModel;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;
import com.ltm.runningtracker.database.Run;

public class MainActivity extends AppCompatActivity {

  MainActivityViewModel mainActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

    // observe location object
    mainActivityViewModel.getLocation().observe(this, location -> {
      ((TextView) findViewById(R.id.latField)).setText("" + location.getLatitude());
      ((TextView) findViewById(R.id.lonField)).setText("" + location.getLongitude());
    });

    // observe weather object
    mainActivityViewModel.getWeather().observe(this, weather -> {
      ((TextView) findViewById(R.id.temperatureField)).setText("" + weather.temperature.getTemp());
    });


    Run run =  new Run("lol", 0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0);
    Log.d("iddd", "" + run.getId());
    run =  new Run("lol", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    Log.d("iddd", "" + run.getId());
    run =  new Run("lol", 0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0);
    Log.d("iddd", "" + run.getId());
    run =  new Run("lol", 0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0);
    Log.d("iddd", "" + run.getId());
    run =  new Run("lol", 0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0);
    Log.d("iddd", "" + run.getId());
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

    startActivity(new Intent(this, PerformanceActivity.class));
   // Navigation.findNavController(this,R.id.fragment2).navigate(R.id.performanceFragment);
  }

  public void startRun(MenuItem m) {
    startActivity(new Intent(this, RunActivity.class));
  }

}
