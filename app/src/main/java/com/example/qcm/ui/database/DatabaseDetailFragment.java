package com.example.qcm.ui.database;

import android.annotation.SuppressLint;
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
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DatabaseDetailFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private int totalPoints = 0;
    private double avgFreq;
    private double avgTemp;
    private int pointAIdx;
    private int pointBIdx;
    private int pointA = 0;
    private int pointB = 0;
    ViewGroup rootView;
    private TextView freq_avg_text;
    private TextView fluo_result_text;
    private TextView concentration_result;
    private List<int[]> points;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_database_detail, container, false);
        TextView dataTitle = rootView.findViewById(R.id.data_title);
        Bundle bundle = getArguments();
        GraphView graph = (GraphView) rootView.findViewById(R.id.detail_graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);

        freq_avg_text = rootView.findViewById(R.id.freq_results);
        fluo_result_text = rootView.findViewById(R.id.fluo_result);
        concentration_result = rootView.findViewById(R.id.concentration_result);

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


            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    int index = (int) dataPoint.getX();

                    // Ensure there are at least three data points following the selected point
                    try {
                        // Calculate the average for the next three points
                        int sumFreq = 0;
                        for (int i = index; i <= index + 59; i++) {
                            sumFreq += points.get(i)[1]; // assuming 1 is the index for frequency
                        }
                        int avgFreq = sumFreq / 60;

                        // Assign to pointA or pointB
                        if (pointA == 0 || (pointA > 0 && pointB > 0)) {
                            pointB = 0;
                            pointAIdx = index;
                            pointA = avgFreq;
                            freq_avg_text.setText("B - " + pointA + ": N/A Hz");
//                            ((MainActivity)requireActivity()).showToast("" + pointAIdx);
                            printResult(fileName);
                        } else if (pointB == 0) {
                            pointBIdx = index;
                            pointB = avgFreq;
                            int diffHz = pointB - pointA;
                            freq_avg_text.setText(pointB + " - " + pointA + ": " + diffHz + " Hz");
//                            ((MainActivity)requireActivity()).showToast("" + pointBIdx);
                            printResult(fileName);
                        }
                    } catch (IndexOutOfBoundsException ignored) {
                        ((MainActivity)requireActivity()).showToast("Please select different index!");
                    }
                }
            });

            loadAndDisplayImages(rootView, fileName);
            printResult(fileName);
            // Load average intensity values from the analysis text files

        }

        return rootView;
    }
    private void printResult(String fileName) {
        double beforeAvgIntensity = loadAverageIntensityFromFile(getContext().getExternalFilesDir(null) + "/fl_images/" + fileName.replace(".xlsx", "") + "/before_FITC_analysis.txt");
        double afterAvgIntensity = loadAverageIntensityFromFile(getContext().getExternalFilesDir(null) + "/fl_images/" + fileName.replace(".xlsx", "") + "/after_FITC_analysis.txt");

        if (beforeAvgIntensity != 0) { // prevent division by zero
            String concentration_result_text = "";

            double ratio = afterAvgIntensity / beforeAvgIntensity;
            fluo_result_text.setText("Ratio of Green Intensities: " + String.format(Locale.getDefault(), "%.2f", ratio));

            double x, y;
            concentration_result_text += "Based on Frequency : ";
            if (pointA > 0 && pointB > 0) {
                y = pointB - pointA;

                double log10Y = Math.log10(Math.abs(y));
                x = Math.pow(10, (log10Y - 0.955455) / 0.117809);
                concentration_result_text += String.format(Locale.getDefault(), "%.2f CFU/mL\n", x);
            } else {
                concentration_result_text += "N/A\n";
            }
            y = ratio;
            y = Math.log10(y);
            x = Math.pow(10, (y + 0.021665) / 0.011376);
            concentration_result_text += String.format(Locale.getDefault(), "Based on Fluorescence: %.2f CFU/mL", x);
            concentration_result.setText(concentration_result_text);
        } else {
            fluo_result_text.setText("N/A");
            concentration_result.setText("N/A");
        }
    }

    private double loadAverageIntensityFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Avg Intensity: ")) {
                        String value = line.replace("Avg Intensity: ", "").trim();
                        return Double.parseDouble(value);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0; // Default value or consider throwing an exception
    }
    private List<int[]> loadPoints(String fileName, LineGraphSeries<DataPoint> series) {
        points = new ArrayList<int[]>();
        String path = getContext().getExternalFilesDir(null) + "/experiments/" + fileName;
        File file = new File(path);
        try {
            if (file.exists()){
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
        String directoryPath = getContext().getExternalFilesDir(null) + "/fl_images/" + fileName.replace(".xlsx", "") + "/";

        // File names for "before" and "after" images
        String beforeImagePath = directoryPath + "before_FITC_processed.jpg";
        String afterImagePath = directoryPath + "after_FITC_processed.jpg";

        // Get the ImageView references from the rootView
        ImageView imageBefore = rootView.findViewById(R.id.image_left);
        ImageView imageAfter = rootView.findViewById(R.id.image_right);

        // Load and set the "before" image
        Bitmap bitmapBefore = BitmapFactory.decodeFile(beforeImagePath);
        imageBefore.setImageBitmap(bitmapBefore);

        // Load and set the "after" image
        Bitmap bitmapAfter = BitmapFactory.decodeFile(afterImagePath);
        imageAfter.setImageBitmap(bitmapAfter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}