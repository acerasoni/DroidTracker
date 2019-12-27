package com.ltm.runningtracker.android.activity;

import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.COLD_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.FREEZING_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.HOT_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.MILD_RUNS_URI;
import static com.ltm.runningtracker.android.contentprovider.ContentProviderContract.WARM_RUNS_URI;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.ltm.runningtracker.R;

/**
 * Graph 1 = Running pace (average speed) Graph 2 = Total distance Graph 3 = Total time
 */
public class WeatherPerformanceActivity extends AppCompatActivity {

  private GraphView graphView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather_performance);

    AsyncTask.execute(() -> {
      graphView = findViewById(R.id.graphView);
      graphView.setTitle("Performance by weather (mph)");
      LineGraphSeries<DataPoint> freezingLine = graphLineBuilder(FREEZING_RUNS_URI);
      LineGraphSeries<DataPoint> coldLine = graphLineBuilder(COLD_RUNS_URI);
      LineGraphSeries<DataPoint> mildLine = graphLineBuilder(MILD_RUNS_URI);
      LineGraphSeries<DataPoint> warmLine = graphLineBuilder(WARM_RUNS_URI);
      LineGraphSeries<DataPoint> hotLine = graphLineBuilder(HOT_RUNS_URI);
      graphView.addSeries(freezingLine);
      graphView.addSeries(coldLine);
      graphView.addSeries(mildLine);
      graphView.addSeries(warmLine);
      graphView.addSeries(hotLine);
    });

//    LineGraphSeries<DataPoint> distance = new LineGraphSeries<>(new DataPoint[] {
//        new DataPoint(0, 1),
//    });
//
//    LineGraphSeries<DataPoint> time = new LineGraphSeries<>(new DataPoint[] {
//        new DataPoint(0, 1),
//    });

  }

  private LineGraphSeries<DataPoint> graphLineBuilder(Uri uri) {
    DataPoint[] runs;
    Cursor c = getApplicationContext().getContentResolver().query(uri, null,
        null, null, null);
    runs = new DataPoint[c.getCount()];
    if(c.moveToFirst()){
      do {
        Log.d("ID: ", c.getString(0));
        Log.d("ID: ", c.getString(1));
        Log.d("ID: ", c.getString(2));
        Log.d("ID: ", c.getString(3));
        Log.d("ID: ", c.getString(4));
        Log.d("ID: ", c.getString(5));
        Log.d("ID: ", c.getString(6));
        Log.d("ID: ", c.getString(7));
        Log.d("ID: ", c.getString(8));
        Log.d("ID: ", c.getString(9));
        Log.d("ID: ", c.getString(10));
        Log.d("ID: ", c.getString(11));
      } while(c.moveToNext());
    }
    c.moveToFirst();
    for (int i = 0; i < runs.length; i++) {
      runs[i] = new DataPoint(i, c.getDouble(11));
      c.moveToNext();
    }
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(runs);
    series.setDrawDataPoints(true);
    return series;
  }

}
