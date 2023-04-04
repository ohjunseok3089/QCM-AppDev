package com.example.qcm.ui.database;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFragment extends Fragment {

    private FragmentDashboardBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TextView receiveDataTextView = getActivity().findViewById(R.id.receive_data);
        receiveDataTextView.setVisibility(View.GONE);

        View rootView = inflater.inflate(R.layout.fragment_database, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DatabaseAdapter adapter = new DatabaseAdapter(getContext(), new DatabaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DataItem item) {
                String fileName = item.getTitle() + ".xlsx"; // get the selected Excel file name
                Bundle bundle = new Bundle();
                bundle.putString("fileName", fileName);

                DatabaseDetail databaseDetail = new DatabaseDetail();
                databaseDetail.setArguments(bundle);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.recycler_view, databaseDetail);
                transaction.addToBackStack(null);
                transaction.commit();
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