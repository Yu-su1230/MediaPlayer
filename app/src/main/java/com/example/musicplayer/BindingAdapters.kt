package com.example.musicplayer

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object BindingAdapters {

    @BindingAdapter("setBitmapImage")
    @JvmStatic
    fun ImageView.setBitmapImage(bitmapImage: Bitmap?) {
        if (bitmapImage != null) {
            setImageBitmap(bitmapImage)
        } else {
            setImageResource(R.drawable.img_now_playing_album_art)
        }
    }
}