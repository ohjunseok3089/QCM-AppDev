package com.example.qcm.ui.frequency;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FrequencyViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FrequencyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is frequency fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}