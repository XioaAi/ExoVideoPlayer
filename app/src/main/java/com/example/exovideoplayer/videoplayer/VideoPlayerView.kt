package com.example.exovideoplayer.videoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.session.PlaybackState
import android.net.Uri
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.RelativeLayout
import com.example.exovideoplayer.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource

import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.view_exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.view_exo_playback_controller_top.view.*
import kotlinx.android.synthetic.main.view_exo_player_view.view.*


class VideoPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {


    interface VideoStateListener {
        fun onStateChanged(state: Int)
    }

    interface OnOrientationChangedListener{
        fun onOrientationChange(isLand: Boolean)
    }

    interface VideoLoadingListener {
        fun onLoadingChanged(isLoading: Boolean)
    }

    interface VideoBackListener{
        fun onBackClick(isLand: Boolean)
    }

    private var isLand = false

    var videoUrl: String? = null
        set(value) {
            if (videoUrl != null) {
                exo_player_view.player.stop()
            }
            field = value
            if (videoUrl?.count() ?: 0 > 0) {

                val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(videoUrl))
                player.prepare(videoSource)
            }
        }
    var autoPlay = true
        set(value) {
            field = value
            player.playWhenReady = autoPlay
        }

    var userController = false
        set(value) {
            field = value
            exo_player_view.useController = value
        }
    var stateListener: VideoStateListener? = null
    var orientationChangeListener : OnOrientationChangedListener? = null
    var videoLoadingListener : VideoLoadingListener? = null
    var videoBackListener : VideoBackListener? = null

    private var player: SimpleExoPlayer
    private val dataSourceFactory : DefaultDataSourceFactory
    private val eventListener = object : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

        }

        override fun onSeekProcessed() {

        }

        override fun onTracksChanged(
            trackGroups: TrackGroupArray?,
            trackSelections: TrackSelectionArray?
        ) {

        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            if (stateListener != null) {
                stateListener!!.onStateChanged(PlaybackState.STATE_ERROR)
            }
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            videoLoadingListener?.onLoadingChanged(isLoading)
        }

        override fun onPositionDiscontinuity(reason: Int) {

        }

        override fun onRepeatModeChanged(repeatMode: Int) {

        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (stateListener != null) {
                stateListener!!.onStateChanged(playbackState)
            }
        }
    }

    init {
        View.inflate(context, R.layout.view_exo_player_view, this)
        exo_player_view.useController = userController
        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        player.addListener(eventListener)
        player.playWhenReady = autoPlay
        exo_player_view.player = player
        exo_player_view.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )

        exo_toggle_full_screen.setOnClickListener {
            fullScreenClick()
        }

        video_play_top_back.setOnClickListener {
            fullScreenClick()
        }
    }

    fun fullScreenClick(){
        isLand = if (isLand) {
            changeToPortrait()
            false
        } else{
            changeToLandscape()
            true
        }
        orientationChangeListener?.onOrientationChange(isLand)
    }

    fun changeToPortrait() {
        exo_toggle_full_screen.setImageResource(R.mipmap.full_screen)
        video_controller_top_view.visibility = View.GONE
    }

    fun changeToLandscape() {
        exo_toggle_full_screen.setImageResource(R.mipmap.cancle_full_screen)
        video_controller_top_view.visibility = View.VISIBLE
    }

    fun seekTo(time: Long) {
        player.seekTo(time)
    }
    fun currentTime(): Long {
        return player.currentPosition
    }
    fun duration(): Long {
        return player.duration
    }

    fun isPause(): Boolean {
        return player.playWhenReady
    }

    fun pause() {
        player.playWhenReady = false
    }
    fun play() {
        player.playWhenReady = true
    }
    fun tryToPlay() {
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl))
        player.prepare(videoSource)
    }

    fun stop() {
        player.stop()
    }
    fun destroy() {
        player.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isLand) {
                videoBackListener?.onBackClick(isLand)
            } else {
                exo_toggle_full_screen.setImageResource(R.mipmap.full_screen)
                fullScreenClick()
                isLand = false
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}