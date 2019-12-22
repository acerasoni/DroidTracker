package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.RunningTrackerApplication.getRunRepository;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class PerformanceActivity extends AppCompatActivity {

  TabLayout sortTabs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance);
    sortTabs = findViewById(R.id.sortByTabs);
  }

}
