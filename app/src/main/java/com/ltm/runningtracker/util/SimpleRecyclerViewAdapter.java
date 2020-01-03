package com.ltm.runningtracker.util;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.ltm.runningtracker.R;

/**
 * Simple adapter for recycler views.
 * Taken from lecture code.
 */
public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.ViewHolder> {

  private LayoutInflater inflater;
  private ItemClickListener clickListener;

  Cursor cursor;

  public SimpleRecyclerViewAdapter(Context context, Cursor cursor) {
    this.inflater = LayoutInflater.from(context);
    this.cursor = cursor;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.run_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    cursor.moveToPosition(position);
    String id = cursor.getString(0);
    String location = cursor.getString(1);
    String date = cursor.getString(2);
    String type = cursor.getString(3);
    String pace = cursor.getString(9);

    holder.idView.setText(id);
    holder.locationView.setText(location);
    holder.dateView.setText(date);
    holder.typeView.setText(type);
    holder.paceView.setText(pace);

  }

  @Override
  public int getItemCount() {
    return cursor.getCount();
  }

  public void swapCursor(Cursor c) {
    this.cursor = c;
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
      if (clickListener != null)
        clickListener.onItemClick(view, getAdapterPosition());
    }
  }

  public void setClickListener(ItemClickListener itemClickListener) {
    this.clickListener = itemClickListener;
  }

  public interface ItemClickListener {
    void onItemClick(View view, int position);
  }
}
