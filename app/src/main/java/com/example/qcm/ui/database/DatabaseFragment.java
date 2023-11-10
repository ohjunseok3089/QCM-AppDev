package com.example.qcm.ui.database;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DatabaseAdapter adapter = new DatabaseAdapter(getContext(), new DatabaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DataItem item) {
                // Handle item click here
                String fileName = item.getTitle() + ".xlsx";
                File file = new File(getContext().getExternalFilesDir(null), "experiments/" + fileName);
                if (file.exists()) {
                    System.out.println(fileName);
                    Bundle bundle = new Bundle();
                    bundle.putString("fileName", fileName);

                    DatabaseDetailFragment databaseDetailFragment = new DatabaseDetailFragment();
                    databaseDetailFragment.setArguments(bundle);

                    ((MainActivity) requireActivity()).switchFragment(bundle);

                } else {
                    Log.d("File not found", fileName);
                    Toast.makeText(getContext(), "File not found: " + fileName, Toast.LENGTH_SHORT).show();
                }

            }
        });
        recyclerView.setAdapter(adapter);

        return rootView;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}