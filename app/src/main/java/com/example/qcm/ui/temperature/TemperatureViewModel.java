package com.example.qcm.ui.temperature;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qcm.MainActivity;

public class TemperatureViewModel extends ViewModel implements MainActivity.OnDataFetchedListener {

    private final MutableLiveData<double[]> data = new MutableLiveData<>();

    public LiveData<double[]> getData() {
        return data;
    }

    @Override
    public void onDataFetched(double[] array) {
//        Log.d("HomeViewModel", "Data received in ViewModel: " + Arrays.toString(array));
        data.setValue(array);
    }
}