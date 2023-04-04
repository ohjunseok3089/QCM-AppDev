package com.example.qcm.ui.database;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.qcm.R;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DatabaseDetail extends Fragment {
    private Context mContext;
    private List<Point> points = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = (ViewGroup) inflater.inflate(R.layout.fragment_database_detail, container, false);


        Bundle bundle = getArguments();
        if (bundle != null) {
            String fileName = bundle.getString("fileName");
            System.out.println(fileName);
            // load the points from the Excel file
            loadPoints(fileName);
            System.out.println(points);
        }

        return rootView;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    private void loadPoints(String fileName) {
        File file = new File(getContext().getExternalFilesDir(null), "experiments/" + fileName);

        try {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.rowIterator();
            rowIterator.next(); // skip the header row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int x = (int) row.getCell(0).getNumericCellValue();
                int y = (int) row.getCell(1).getNumericCellValue();
                points.add(new Point(x, y));
            }

            // display the points in a chart or a table
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
