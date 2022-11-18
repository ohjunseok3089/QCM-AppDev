package com.example.qcm.ui.imaging;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ImagingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ImagingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is imaging fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}