package com.example.qcm.ui.database;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter extends RecyclerView.Adapter<DatabaseAdapter.ViewHolder> {

    private Context mContext;
    private List<DataItem> mItems;

    public DatabaseAdapter(Context context) {
        List<DataItem> itemList = new ArrayList<>();
        itemList.add(new DataItem(1, "E.coli_10^9", "2023-02-25", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(2, "E.coli_10^9", "2023-02-26", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(3, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(4, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(5, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(6, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(7, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));
        itemList.add(new DataItem(8, "E.coli_10^9", "2023-02-27", "Positive Or 10^9", R.drawable.default_thumbnail));

        mContext = context;
        mItems = itemList;  // your implementation to retrieve the list of items to display
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_database, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem item = mItems.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.dateTextView.setText(item.getDate());
        holder.typeTextView.setText(item.getType());
        // set image thumbnail using Glide or Picasso or other image loading library
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView dateTextView;
        public TextView typeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail);
            titleTextView = itemView.findViewById(R.id.title);
            dateTextView = itemView.findViewById(R.id.date);
            typeTextView = itemView.findViewById(R.id.type);
        }
    }
}
