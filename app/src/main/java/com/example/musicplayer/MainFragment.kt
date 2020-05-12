package com.example.musicplayer

import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.example.musicplayer.databinding.FragmentNowplayingBinding

class MainFragment : Fragment() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private lateinit var binding: FragmentNowplayingBinding

    private val viewModel = MainViewModel()

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            // 曲情報の変化に合わせてUI更新
//            binding.textViewMusicTitle.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//            binding.textViewArtistName.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//            binding.imageViewJacket.setImageBitmap(metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART))
            binding.viewModel?.title?.value = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            binding.viewModel?.artist?.value = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    binding.imageButtonMusicPlayAndStop.setImageResource(R.drawable.exo_icon_pause)
                }
                else -> {
                    binding.imageButtonMusicPlayAndStop.setImageResource(R.drawable.exo_icon_play)
                }
            }
        }
    }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            MediaControllerCompat.getMediaController(requireActivity())?.transportControls?.prepare()
        }
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // 受け取ったTokenで操作するようにする
            MediaControllerCompat.setMediaController(
                requireActivity(),
                MediaControllerCompat(context,mediaBrowser.sessionToken)
            )
            buildTransportControls()
            // 接続後、曲リストを購読する。ここでparentIdを渡す
            mediaBrowser.subscribe(mediaBrowser.root,subscriptionCallback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(requireContext(), MediaPlaybackService::class.java),
            connectionCallback,
            null
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNowplayingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(requireActivity())?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(requireActivity())
        binding.imageButtonMusicPlayAndStop.apply {
            setOnClickListener {
                val state = mediaController.playbackState.state
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                } else {
                    mediaController.transportControls.play()
                }
            }
        }
        // 操作の監視(サービス接続後なら、ここじゃなくてもOK)
        mediaController.registerCallback(controllerCallback)
    }
}