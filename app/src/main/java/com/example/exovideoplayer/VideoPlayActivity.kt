package com.example.exovideoplayer

import android.content.pm.ActivityInfo
import android.media.session.PlaybackState
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.exovideoplayer.videoplayer.VideoPlayerView
import kotlinx.android.synthetic.main.activity_video_play.*

class VideoPlayActivity : AppCompatActivity() {

    private var currentIsLand:Boolean = false //记录当前是否是横屏

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        video_player_view.userController = true
        video_player_view.videoUrl = ""

        initListener()

    }

    private fun initListener() {
        video_player_view.stateListener = object : VideoPlayerView.VideoStateListener{
            override fun onStateChanged(state: Int) {
                when(state){
                    PlaybackState.STATE_PLAYING -> {
                    }
                    PlaybackState.STATE_FAST_FORWARDING ,
                    PlaybackState.STATE_ERROR -> {
                        if(currentIsLand){
                            video_player_view.seekTo(0)
                            video_player_view.fullScreenClick()
                        }

                        if(state == PlaybackState.STATE_ERROR){
                            Toast.makeText(this@VideoPlayActivity,"播放失败，请检查网络",Toast.LENGTH_LONG).show()
                        }
                    }

                    PlaybackState.STATE_BUFFERING ->{
                    }

                    else ->{
                        Log.e("TAG","播放状态$state")
                    }
                }
            }
        }

        video_player_view.orientationChangeListener = object: VideoPlayerView.OnOrientationChangedListener{
            override fun onOrientationChange(isLand: Boolean) {
                currentIsLand = isLand
                if(isLand){

                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    supportActionBar?.hide()
                    setFullScreen(true)

                }else{
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    supportActionBar?.show()
                    setFullScreen(false)
                }
                resizeVideoLayout(isLand)

            }
        }

        video_player_view.videoBackListener = object:VideoPlayerView.VideoBackListener{
            override fun onBackClick(isLand: Boolean) {
                if (!isLand) {
                    finish()
                }
            }

        }
    }

    protected fun setFullScreen(full: Boolean) {
        val params = window.attributes
        if (full) {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = params
//            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }else {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            window.attributes = params
//            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    /**
     * 修改VideoPlayView大小
     */
    fun resizeVideoLayout(isLand : Boolean){
        val layoutParams = video_player_view.layoutParams
        if(!isLand){
            layoutParams.height = 420
            layoutParams.width = -1
        }else{
            layoutParams.height = -1
            layoutParams.width = -1
        }
        video_player_view.layoutParams = layoutParams
        video_player_view.requestLayout()

    }

    override fun onDestroy() {
        super.onDestroy()
        video_player_view.destroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            video_player_view.onKeyDown(keyCode, event)
        } else super.onKeyDown(keyCode, event)
    }
}
