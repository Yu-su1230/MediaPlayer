package com.example.musicplayer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val artist = MutableLiveData<String>()

    val title = MutableLiveData<String>()
}