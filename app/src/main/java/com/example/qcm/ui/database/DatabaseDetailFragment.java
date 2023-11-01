package com.example.qcm.ui.database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class DatabaseDetailFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private int totalPoints = 0;
    private double avgFreq;
    private double avgTemp;
    ViewGroup rootView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_database_detail, container, false);
        TextView dataTitle = rootView.findViewById(R.id.data_title);
        Bundle bundle = getArguments();
        GraphView graph = (GraphView) rootView.findViewById(R.id.detail_graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        // Set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);

        // Set scrollable and scalable
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.getGridLabelRenderer().setVerticalAxisTitle("Frequency (Hz)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");

        if (bundle != null) {
            String fileName = bundle.getString("fileName");
            System.out.println(fileName);
            dataTitle.setText(fileName.replace(".xlsx", ""));
            loadPoints(fileName, series);

            double freq_avg = Math.round(avgFreq * 100.0) / 100.0;
            double temp_avg = Math.round(avgTemp * 100.0) / 100.0;
            TextView freq_avg_text = rootView.findViewById(R.id.avg_freq_text);
//            TextView temp_avg_text = rootView.findViewById(R.id.avg_temp_text);
            freq_avg_text.setText(freq_avg + " Hz");
//            temp_avg_text.setText(temp_avg + "  K");
            loadAndDisplayImages(rootView, fileName);
        }

        return rootView;
    }

    private List<int[]> loadPoints(String fileName, LineGraphSeries<DataPoint> series) {
        List<int[]> points = new ArrayList<int[]>();
        String path = getContext().getExternalFilesDir(null) + "/experiments/" + fileName;
        File file = new File(path);
        try {
            if (file.exists()){
//                InputStream fis = new FileInputStream(file);
                System.out.println(path);
                Workbook workbook = WorkbookFactory.create(file);
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.rowIterator();
                rowIterator.next();

                int time = 0;
                int freq = 0;
                int temp = 0;

                int sumFreq = 0;
                int sumTemp = 0;
                while (rowIterator.hasNext()) {
                    totalPoints++;
                    Row row = rowIterator.next();
                    Cell cell1 = row.getCell(0);
                    Cell cell2 = row.getCell(1);

                    if (cell1.getCellType() == CellType.NUMERIC) {
                        time = (int) cell1.getNumericCellValue();
                    } else if (cell1.getCellType() == CellType.STRING) {
                        time = Integer.parseInt(cell1.getStringCellValue());
                    } else {
                        // Handle other cell types if necessary
                    }
                    if (cell2.getCellType() == CellType.NUMERIC) {
                        freq = (int) cell2.getNumericCellValue();
                    } else if (cell2.getCellType() == CellType.STRING) {
                        String input = cell2.getStringCellValue();
                        String expression = input.substring(1, input.length() - 1); // remove the square brackets
                        String[] parts = expression.split("/");
                        double temperature = Double.parseDouble(parts[0]);
                        double frequency = Double.parseDouble(parts[1]);

                        // TODO code 수정
                        freq = (int) frequency;
                        temp = (int) temperature;
                    } else {
                        // Handle other cell types if necessary
                    }
                    sumFreq += freq;
                    sumTemp += temp;
                    series.appendData(new DataPoint(time, freq), true, time + 1);
                    points.add(new int[] { time, freq, temp });
                }
                avgFreq = ((double) sumFreq / totalPoints);
                avgTemp = ((double) sumTemp / totalPoints);
            }


            // display the points in a chart or a table
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private void loadAndDisplayImages(View rootView, String fileName) {
        // Get the directory path
        String path = getContext().getExternalFilesDir(null) + "/fl_images/" + fileName.replace(".xlsx", "") + "/";
        File directory = new File(path);

        // Check if directory exists and is a directory
        if (directory.exists() && directory.isDirectory()) {
            File[] imageFiles = directory.listFiles();

            // Sort the image files based on last modified time (ascending order)
            Arrays.sort(imageFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });

            // Get the LinearLayout container from the rootView
            LinearLayout imageContainer = rootView.findViewById(R.id.image_container);

            // Loop through the sorted image files and populate the custom layout
            for (int i = 0; i < imageFiles.length; i += 2) {
                // Inflate the custom layout for two images
                View imageRow = LayoutInflater.from(getContext()).inflate(R.layout.custom_image_row, null, false);

                // Load the left image
                ImageView imageLeft = imageRow.findViewById(R.id.image_left);
                Bitmap bitmapLeft = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());
                imageLeft.setImageBitmap(bitmapLeft);

                // Load the right image if available
                if (i + 1 < imageFiles.length) {
                    ImageView imageRight = imageRow.findViewById(R.id.image_right);
                    Bitmap bitmapRight = BitmapFactory.decodeFile(imageFiles[i + 1].getAbsolutePath());
                    imageRight.setImageBitmap(bitmapRight);
                }

                // Add the custom layout to the LinearLayout container
                imageContainer.addView(imageRow);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}