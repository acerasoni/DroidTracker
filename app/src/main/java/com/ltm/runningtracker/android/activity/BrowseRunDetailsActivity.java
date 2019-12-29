package com.ltm.runningtracker.android.activity;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.ltm.runningtracker.R;

/**
 * The purpose of this activity is to browse details relating to a specific run, and tag the associated diet
 * Details shown are start location, end location ... //TODO finish
 */
public class BrowseRunDetailsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_browse_run_details);
  }

}
