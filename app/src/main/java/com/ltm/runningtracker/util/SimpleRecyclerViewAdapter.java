package com.ltm.runningtracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.ltm.runningtracker.R;
import com.ltm.runningtracker.database.model.Run;
import java.util.List;

/**
 * Simple adapter for recycler views. Taken from lecture code. Adapted for my list view items to
 * take a List of runs rather than a Cursor.
 */
public class SimpleRecyclerViewAdapter extends
    RecyclerView.Adapter<SimpleRecyclerViewAdapter.ViewHolder> {

  private LayoutInflater inflater;
  private ItemClickListener clickListener;
  private List<Run> runs;

  public SimpleRecyclerViewAdapter(Context context, List<Run> runs) {
    this.inflater = LayoutInflater.from(context);
    this.runs = runs;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.run_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Run run = runs.get(position);

    int id = run._id;
    String location = run.location;
    String date = run.date;
    String type = run.runType;
    float pace = run.pace;

    holder.idView.setText(Integer.toString(id));
    holder.locationView.setText(location);
    holder.dateView.setText(date);
    holder.typeView.setText(type);
    holder.paceView.setText(Float.toString(pace));
  }

  @Override
  public int getItemCount() {
    return runs.size();
  }

  public void swapRuns(List<Run> runs) {
    this.runs = runs;
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView idView;
    TextView locationView;
    TextView dateView;
    TextView typeView;
    TextView paceView;

    ViewHolder(View itemView) {
      super(itemView);
      idView = itemView.findViewById(R.id.id);
      locationView = itemView.findViewById(R.id.location);
      paceView = itemView.findViewById(R.id.pace);
      dateView = itemView.findViewById(R.id.date);
      typeView = itemView.findViewById(R.id.type);

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      if (clickListener != null) {
        clickListener.onItemClick(view, getAdapterPosition());
      }
    }
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    this.clickListener = itemClickListener;
  }

  public interface ItemClickListener {

    void onItemClick(View view, int position);
  }
}