package com.example.musicplayer

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import timber.log.Timber

object MusicLibrary {
    fun getAssetsFile(context: Context): AssetFileDescriptor {
        return context.resources.openRawResourceFd(R.raw.fracture_ray)
    }

    fun getMetadata(context: Context): MediaMetadataCompat {
        val retriever = MediaMetadataRetriever().apply {
            val assetsFileData = getAssetsFile(context)
            setDataSource(assetsFileData.fileDescriptor, assetsFileData.startOffset, assetsFileData.length)
        }
        return MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "fracture_ray.mp3")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                createArt(retriever))
            .build()
    }

    fun getMediaSource(dataSourceFactory: DataSource.Factory, rawDataResource: RawResourceDataSource): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(rawDataResource.uri)
    }

    private fun createArt(retriever: MediaMetadataRetriever): Bitmap? {
        return try {
            val picture = retriever.embeddedPicture
            BitmapFactory.decodeByteArray(picture, 0, picture.size)
        } catch (e: Exception) {
            Timber.w(e)
            null
        }
    }
}
