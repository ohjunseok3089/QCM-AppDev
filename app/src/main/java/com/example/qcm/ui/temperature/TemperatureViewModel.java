package com.example.qcm.ui.temperature;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TemperatureViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TemperatureViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is temperature fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}