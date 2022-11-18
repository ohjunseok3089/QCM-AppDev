package com.example.qcm.ui.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DatabaseViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DatabaseViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is database fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}