package com.example.qcm.ui.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.databinding.FragmentHomeBinding;
//import com.jjoe64.graphview.GraphView;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
        final TextView bluetoothDeviceName = binding.bluetoothDeviceName;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        binding.newExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("New Experiment");

                // Set up the input
                final EditText inputTitle = new EditText(requireContext());
                final EditText inputSample = new EditText(requireContext());

                // Set the input fields
                inputTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                inputTitle.setHint("Experiment Title");

                inputSample.setInputType(InputType.TYPE_CLASS_TEXT);
                inputSample.setHint("Sample");

                // Add the input fields to the dialog box
                LinearLayout layout = new LinearLayout(requireContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(inputTitle);
                layout.addView(inputSample);
                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = inputTitle.getText().toString();
                        String sample = inputSample.getText().toString();
                        // Save the experiment with the given title and sample
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}