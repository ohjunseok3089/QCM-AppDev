package com.example.qcm.ui.database;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.MainActivity;
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

    private OnItemClickListener listener;


    public DatabaseAdapter(Context context, OnItemClickListener listener) {
        this.listener = listener;
        mContext = context;

        List<DataItem> itemList = new ArrayList<>();
        File file = new File(context.getExternalFilesDir(null), "experiments");

        File[] files = file.listFiles();
        if (file != null && files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        }

        if (file != null && files != null) {
            int data_number = 1;
            for (File excel_file: files) {
                if (excel_file.isFile() && excel_file.getName().endsWith(".xlsx")) {
                    itemList.add(new DataItem(data_number++, excel_file.getName().replace(".xlsx", ""), new Date(excel_file.lastModified()).toString(), "", R.drawable.default_thumbnail));
                }
            }
        } else {
            Toast toast = Toast.makeText(mContext, "No Experiment exists! Please go back to home screen.", Toast.LENGTH_LONG);
            toast.show();
        }

        mItems = itemList;
    }

    public interface OnItemClickListener {
        void onItemClick(DataItem item);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView dateTextView;
        public TextView typeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find the views in the item layout
            thumbnailImageView = itemView.findViewById(R.id.thumbnail);
            titleTextView = itemView.findViewById(R.id.title);
            dateTextView = itemView.findViewById(R.id.date);
            typeTextView = itemView.findViewById(R.id.type);

            // Add an OnClickListener to the itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the position of the item clicked
                    int position = getAdapterPosition();

                    // Make sure the position is valid
                    if (position != RecyclerView.NO_POSITION) {
                        // Get the item at the position
                        DataItem item = mItems.get(position);

                        // Call the onItemClick method of the listener object
                        if (listener != null) {
                            listener.onItemClick(item);
                        }
                    }
                }
            });
        }
    }
}
