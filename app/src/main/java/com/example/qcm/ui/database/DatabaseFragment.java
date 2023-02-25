package com.example.qcm.ui.database;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;

public class DatabaseFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
//    private DatabaseAdapter adapter;
//    private List<DatabaseItem> itemList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_database, container, false);

        // Find the RecyclerView in the layout
//        recyclerView = rootView.findViewById(R.id.recycler_view);
//
//        // Create the list of database items
//        itemList = new ArrayList<>();
//        itemList.add(new DatabaseItem(R.drawable.thumbnail_1, "Title 1", "Date 1", "Type 1"));
//        itemList.add(new DatabaseItem(R.drawable.thumbnail_2, "Title 2", "Date 2", "Type 2"));
//        itemList.add(new DatabaseItem(R.drawable.thumbnail_3, "Title 3", "Date 3", "Type 3"));
//        // Add more items as needed
//
//        // Create and set the adapter for the RecyclerView
//        adapter = new DatabaseAdapter(itemList);
//        recyclerView.setAdapter(adapter);
//
//        // Set the layout manager for the RecyclerView
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}