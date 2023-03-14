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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DatabaseAdapter extends RecyclerView.Adapter<DatabaseAdapter.ViewHolder> {

    private Context mContext;
    private List<DataItem> mItems;

    public DatabaseAdapter(Context context) {
        mContext = context;

        List<DataItem> itemList = new ArrayList<>();
        File file = new File(context.getExternalFilesDir(null), "experiments");

        File[] files = file.listFiles();
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());


        if (file != null) {
            int data_number = 1;
            for (File excel_file: files) {
                if (excel_file.isFile() && excel_file.getName().endsWith(".xlsx")) {
                    itemList.add(new DataItem(data_number++, excel_file.getName().replace(".xlsx", ""), new Date(excel_file.lastModified()).toString(), "", R.drawable.default_thumbnail));
                }
            }
        }

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
