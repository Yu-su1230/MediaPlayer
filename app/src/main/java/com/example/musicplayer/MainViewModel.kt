package com.example.musicplayer

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val artist = MutableLiveData<String>()

    val title = MutableLiveData<String>()

    val albumArt = MutableLiveData<Bitmap>()

    val seekMax = MutableLiveData<Int>()
}