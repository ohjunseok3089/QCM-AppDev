package com.example.qcm.ui.database;

import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Iterator;
import java.util.List;

public class DatabaseDetailFragment extends Fragment {

    private FragmentDashboardBinding binding;
    ViewGroup rootView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TextView receiveDataTextView = getActivity().findViewById(R.id.receive_data);
        receiveDataTextView.setVisibility(View.GONE);

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

        if (bundle != null) {
            String fileName = bundle.getString("fileName");
            System.out.println(fileName);
            dataTitle.setText(fileName.replace(".xlsx", ""));
            List<int[]> points = loadPoints(fileName, series);
            graph.getViewport().setMaxX(points.size() + 1);
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
                while(rowIterator.hasNext()) {
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
                        freq = Integer.parseInt(cell2.getStringCellValue());
                    } else {
                        // Handle other cell types if necessary
                    }
                    series.appendData(new DataPoint(time, freq), true, time + 1);
                    points.add(new int[] { time, freq, temp });
                }
            }


            // display the points in a chart or a table
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}