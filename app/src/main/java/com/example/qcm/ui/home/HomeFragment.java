package com.example.qcm.ui.home;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
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
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TextView rdata;
    private HomeViewModel homeViewModel;


    private FragmentHomeBinding binding;
    // Helper function
    private void showNewExperimentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Experiment");

        final EditText inputTitle = setupExperimentTitleInput();

        LinearLayout layout = setupDialogLayout(inputTitle);
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleOkButton(inputTitle);
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

    private EditText setupExperimentTitleInput() {
        EditText inputTitle = new EditText(requireContext());
        inputTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        inputTitle.setHint("Experiment Title");
        inputTitle.setPadding(20, 20, 20, 40);

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(50);
        inputTitle.setFilters(filters);

        return inputTitle;
    }



    private LinearLayout setupDialogLayout(EditText inputTitle) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(inputTitle);
        return layout;
    }

    private void handleOkButton(EditText inputTitle) {
        String title = inputTitle.getText().toString();
        File file = new File(getContext().getExternalFilesDir("experiments"), title + ".xlsx");

        ((MainActivity)getActivity()).setCurExcelFile(file);
        ((MainActivity)getActivity()).setCurExcelName(title);

        if (file.exists()) {
            showAlert("Excel File Creation Error", "Experiment title already exists");
        } else {
            createExperimentExcelFile(title, file);
        }
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void createExperimentExcelFile(String title, File file) {
        try {
            File directory = new File(getContext().getExternalFilesDir("fl_images"), title);
            directory.mkdirs();

            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("Time_Temperature_Frequency");

            Row row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("Time");
            row1.createCell(1).setCellValue("Temperature");
            row1.createCell(2).setCellValue("Frequency");

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
                showAlert("Excel File Created", "The Excel file \"" + title + "\" has been created.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Excel File Creation Error", "The Excel file \"" + title + "\" cannot be created.");
        }
    }

    // Main
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView bluetoothDeviceName = binding.bluetoothDeviceName;

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
                    try {
                        ((MainActivity) getActivity()).connectBluetooth();
                    } catch (Exception e) {
                        toast = Toast.makeText(root.getContext(), "Connection Error!", Toast.LENGTH_SHORT);
                        bluetoothSwitch.setChecked(((MainActivity) getActivity()).checkBluetooth());
                        toast.show();
                    }

                } else {
                    // The switch is off
                    Toast toast = Toast.makeText(root.getContext(), "Disconnecting Bluetooth...", Toast.LENGTH_SHORT);
                    toast.show();
                    ((MainActivity) getActivity()).disconnectBluetooth();
                }
            }
        });
        // Link ViewModel to MainActivity's data fetch
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setOnDataFetchedListener(homeViewModel);
        }

        // Observe the LiveData and update UI
        homeViewModel.getData().observe(getViewLifecycleOwner(), array -> {
            if (array != null && array.length == 2 && array[0] != 0) {
                TextView rdata = root.findViewById(R.id.receive_data_home);  // Adjust ID as necessary
                rdata.setText("Frequency: " + array[0] + "Hz | Temperature: " + array[1] + "K");
            } else {
                Log.d("HomeFragment", "LiveData observed. Data format unexpected.");
            }
        });



        // Trigger fetching data from MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).fetchData();
        }
        binding.newExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewExperimentDialog();
            }
        });

        return root;
    }

    /**
     * Async update rdata
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }
}