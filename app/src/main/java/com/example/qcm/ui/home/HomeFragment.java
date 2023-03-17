package com.example.qcm.ui.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentHomeBinding;

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

        // Bluetooth button
        Switch bluetoothSwitch = root.findViewById(R.id.bluetooth_toggle);
        bluetoothSwitch.setChecked(((MainActivity) getActivity()).checkBluetooth());
        bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Call your method here
                if (isChecked) {
                    // The switch is on
                    Toast toast = Toast.makeText(root.getContext(), "Connecting Bluetooth...", Toast.LENGTH_SHORT);
                    toast.show();
//                    ((MainActivity) getActivity()).connectBluetooth();
                } else {
                    // The switch is off
                    Toast toast = Toast.makeText(root.getContext(), "Disconnecting Bluetooth...", Toast.LENGTH_SHORT);
                    toast.show();
//                    ((MainActivity) getActivity()).disconnectBluetooth();
                }
            }
        });

        binding.newExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("New Experiment");

                // Set up the input
                final EditText inputTitle = new EditText(requireContext());

                // Set the input fields
                inputTitle.setInputType(InputType.TYPE_CLASS_TEXT);
                inputTitle.setHint("Experiment Title");

                // Add the input fields to the dialog box
                LinearLayout layout = new LinearLayout(requireContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(inputTitle);
                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = inputTitle.getText().toString();

                        File file = new File(getContext().getExternalFilesDir("experiments"), title + ".xlsx");

                        // Check if the file with the given title already exists
                        if (file.exists()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setTitle("Excel File Creation Error");
                            builder.setMessage("Experiment title already exists");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            // Save the experiment with the given title and sample
                            try {
                                File directory = new File(getContext().getExternalFilesDir("fl_images"), title);
                                directory.mkdirs();

                                // Create a new workbook
                                Workbook workbook = WorkbookFactory.create(true);

                                // Create a new sheet
                                Sheet sheet = workbook.createSheet("Time_Temperature_Frequency");

                                Row row1 = sheet.createRow(0);

                                Cell cell1 = row1.createCell(0);
                                cell1.setCellValue("Time");

                                Cell cell2 = row1.createCell(1);
                                cell2.setCellValue("Temperature");

                                Cell cell3 = row1.createCell(2);
                                cell3.setCellValue("Frequency");

                                FileOutputStream outputStream = new FileOutputStream(file);
                                workbook.write(outputStream);

                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setTitle("Excel File Created");
                                builder.setMessage("The Excel file \"" + title + "\" has been created.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();


                            } catch (IOException e) {
                                e.printStackTrace();

                                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                builder.setTitle("Excel File Creation Error");
                                builder.setMessage("The Excel file \"" + title + "\" cannot be created.");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }


                        }
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