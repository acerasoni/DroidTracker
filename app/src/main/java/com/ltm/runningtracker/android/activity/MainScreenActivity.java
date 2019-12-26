package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getUserRepository;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.RUNS_URI;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
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

public class MainScreenActivity extends AppCompatActivity {

  MainActivityViewModel mainActivityViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

    // observe location object
    mainActivityViewModel.getLocation().observe(this, location -> {
    });

    // observe weather object
    mainActivityViewModel.getWeather().observe(this, weather -> {
    });

  }

  public void startRunActivity(View v) {
    startActivity(new Intent(this, RunActivity.class));
  }

  public void startPerformanceActivity(View v) {
    startActivity(new Intent(this, PerformanceTypeSelectorActivity.class));
  }
}
