package com.andreklein.myapplication.ui.last

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LastViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is last Fragment"
    }
    val text: LiveData<String> = _text
}