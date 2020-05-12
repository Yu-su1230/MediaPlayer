package com.example.musicplayer

import android.content.Intent
import android.drm.DrmStore
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util.getUserAgent
import kotlin.reflect.jvm.internal.impl.platform.PlatformUtilKt

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    @PlaybackStateCompat.State
    private var mediaState: Int = PlaybackStateCompat.STATE_NONE

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var exoPlayer: ExoPlayer

    private lateinit var rawDataSource: RawResourceDataSource

    override fun onCreate() {
        super.onCreate()

        exoPlayer = SimpleExoPlayer.Builder(baseContext).build()
        rawDataSource = RawResourceDataSource(baseContext)

        mediaSession = MediaSessionCompat(baseContext, MediaPlaybackService::class.simpleName!!).apply {
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("app-media-root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val meta = MusicLibrary.getMetadata(baseContext)
        val list = mutableListOf(
            MediaBrowserCompat.MediaItem(
                meta.description,
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        )
        result.sendResult(list)
    }

    private fun setNewState(@PlaybackStateCompat.State newState: Int) {
        mediaState = newState
        stateBuilder = PlaybackStateCompat.Builder()
        stateBuilder
            .setActions(getAvailableActions())
            .setState(newState, exoPlayer.currentPosition, 1.0f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }

    // 現在の再生状態によって、許容する操作を切り替える
    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long {
        var actions = (
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
        actions = when (mediaState) {
            PlaybackStateCompat.STATE_STOPPED -> actions or (
                    PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                    )
            PlaybackStateCompat.STATE_PLAYING -> actions or (
                    PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SEEK_TO
                    )
            PlaybackStateCompat.STATE_PAUSED -> actions or (
                    PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP
                    )
            else -> actions or (
                    PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
                    )
        }
        return actions
    }

    private val callback = object : MediaSessionCompat.Callback() {

        override fun onPrepare() {
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            mediaSession.setMetadata(MusicLibrary.getMetadata(baseContext))
            rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(R.raw.fracture_ray)))

            val mediaSource = MusicLibrary.getMediaSource(
                DefaultDataSourceFactory(
                    baseContext,
                    getUserAgent(baseContext, "ExoText")
                ),
                rawDataSource
            )

            exoPlayer.prepare(mediaSource)
        }

        override fun onPlay() {
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            mediaSession.isActive = true
            exoPlayer.playWhenReady = true
            startService(Intent(baseContext, MediaPlaybackService::class.java))
        }

        override fun onPause() {
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            exoPlayer.playWhenReady = false
        }

        override fun onStop() {
            setNewState(PlaybackStateCompat.STATE_STOPPED)
            exoPlayer.stop()
            mediaSession.isActive = false
            stopSelf()
        }

    }
}