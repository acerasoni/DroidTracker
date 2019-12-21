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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.android.contentprovider.ContentProviderContract;

public class PerformanceActivity extends AppCompatActivity {


  private SimpleCursorAdapter dataAdapter;
  private ListView listView;

  private final String RUN_DISPLAYED_DATA[] = new String[]{
      ContentProviderContract._ID,
      ContentProviderContract.WEATHER,
      ContentProviderContract.DURATION
  };

  private final int[] COLUMNS_RESULT_IDS = new int[]{
      R.id.value1,
      R.id.value2,
      R.id.value3
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_performance);
    listView = findViewById(R.id.runList);

    AsyncTask.execute(() -> {
      Cursor c = getRunRepository().getRunCursor();
      if (c.moveToFirst()) {
        do {
          Log.d("Cursor: ID", "" + c.getInt(0));

          Log.d("Cursor: Weather", "" + c.getString(1));
        } while (c.moveToNext());
      }
      Log.d("Cursor", "" + c.getCount());
      dataAdapter = new SimpleCursorAdapter(
          getApplicationContext(),
          R.layout.run_list_item,
          c,
          RUN_DISPLAYED_DATA,
          COLUMNS_RESULT_IDS,
          0);
      runOnUiThread(() -> listView.setAdapter(dataAdapter));
    });

  }

  public void onFreezing(View v) {

  }

  public void onCold(View v) {

  }

  public void onWarm(View v) {

  }

}
