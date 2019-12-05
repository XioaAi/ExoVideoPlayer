package com.example.exovideoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.exovideoplayer.model.ListData
import com.example.exovideoplayer.videoplayer.VideoPlayerView
import kotlinx.android.synthetic.main.activity_video_list_play.*
import kotlinx.android.synthetic.main.activity_video_play.*

class VideoListPlayActivity : AppCompatActivity() {

    private var data = ArrayList<ListData>()
    private var manager = LinearLayoutManager(this)

    private lateinit var playerView:VideoPlayerView
    private lateinit var listAdapter : RecycleViewAdapter

    var parentViews: Array<ViewGroup?>? = null
    var currentPlayPosition:Int= -1

    var currentIsLand:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_list_play)

        repeat(8) {
            data.add(ListData("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1281343961,3647721500&fm=173&app=49&f=JPEG?w=411&h=266&s=76E49B504F307A158478495F030080F2",
                "",false))
        }

        playerView = VideoPlayerView(this)
        playerView.userController = true

        recycle_view.layoutManager = manager
        listAdapter = RecycleViewAdapter(this,data,playerView)

        recycle_view.adapter = listAdapter

        initListener()

    }

    private fun initListener() {

        playerView.orientationChangeListener = object : VideoPlayerView.OnOrientationChangedListener {
            override fun onOrientationChange(isLand: Boolean) {
                currentIsLand = isLand
                if(isLand){
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    supportActionBar?.hide()
                    land_video_play.visibility = View.VISIBLE
                    recycle_view.visibility = View.GONE
                    parentViews?.get(1)?.removeView(playerView)
                    land_video_play.addView(playerView)
                }else{
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    supportActionBar?.show()
                    land_video_play.visibility = View.GONE
                    recycle_view.visibility = View.VISIBLE
                    land_video_play.removeView(playerView)
                    parentViews?.get(1)?.addView(playerView)
                }

                setFullScreen(isLand)
                resizeVideoLayout(playerView,isLand)
            }
        }

        playerView.stateListener = object : VideoPlayerView.VideoStateListener{
            override fun onStateChanged(state: Int) {
                when(state){
                    PlaybackState.STATE_PLAYING -> {
                    }
                    PlaybackState.STATE_FAST_FORWARDING ,
                    PlaybackState.STATE_ERROR -> {

                        if(currentIsLand){
                            playerView.stop()
                            playerView.fullScreenClick()
                        }
                        if(currentPlayPosition>=0){
                            data[currentPlayPosition].isPlay = false
                            parentViews?.get(1)?.removeView(playerView)
                            listAdapter.notifyDataSetChanged()
                        }

                        if(state == PlaybackState.STATE_ERROR){
                            Toast.makeText(this@VideoListPlayActivity,"播放失败，请检查网络", Toast.LENGTH_LONG).show()
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

        playerView.videoBackListener = object:VideoPlayerView.VideoBackListener{
            override fun onBackClick(isLand: Boolean) {
                if (!isLand) {
                    finish()
                }
            }
        }

        listAdapter.setOnItemVideoPlayListener(object : RecycleViewAdapter.OnItemVideoPlayListener{
            override fun onItemVideoPlay(parentViews: Array<ViewGroup?>, position: Int) {
                this@VideoListPlayActivity.parentViews = parentViews
                currentPlayPosition = position
                for(item in data.indices){
                    data[item].isPlay = item == position
                }
                parentViews[0]?.removeView(playerView)
                parentViews[1]?.addView(playerView)
                listAdapter.notifyDataSetChanged()
            }
        })
    }

    protected fun setFullScreen(full: Boolean) {
        val params = window.attributes
        if (full) {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = params
        }else {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            window.attributes = params
        }
    }
    /**
     * 修改VideoPlayView大小
     */
    fun resizeVideoLayout(playerView:VideoPlayerView,isLand : Boolean){
        val layoutParams = playerView.layoutParams
        if(!isLand){
            layoutParams.height = 380
            layoutParams.width = -1
        }else{
            layoutParams.height = -1
            layoutParams.width = -1
        }
        playerView.layoutParams = layoutParams
        playerView.requestLayout()

    }

    override fun onDestroy() {
        super.onDestroy()
        parentViews?.get(1)?.removeView(playerView)
        playerView.destroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && currentPlayPosition>=0) {
            playerView.onKeyDown(keyCode, event)
            return true
        } else super.onKeyDown(keyCode, event)
    }


}

class RecycleViewAdapter(var context:Context , var data:ArrayList<ListData> ,var playerView: VideoPlayerView) : RecyclerView.Adapter<RecycleViewAdapter.ViewHolder>(){

    private var viewGroups: Array<ViewGroup?> = arrayOfNulls(2)

    private var itemVideoPlayListener:OnItemVideoPlayListener?= null

    fun setOnItemVideoPlayListener(onItemVideoPlayListener: OnItemVideoPlayListener){
        this.itemVideoPlayListener = onItemVideoPlayListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_play_item,parent,false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(data[position],position)
    }

    inner class ViewHolder(var item:View) : RecyclerView.ViewHolder(item){

        fun bindView(bean:ListData,position: Int){
            val imageView = item.findViewById<ImageView>(R.id.list_item_image)
            val videoPlayLayout = item.findViewById<CardView>(R.id.list_video_player_view_layout)

            Glide.with(context).load(bean.Url).into(imageView)

            if(bean.isPlay){
                imageView.visibility = View.GONE
                videoPlayLayout.visibility = View.VISIBLE
            }else{
                imageView.visibility = View.VISIBLE
                videoPlayLayout.visibility = View.GONE
            }

            imageView.setOnClickListener {

                //this@RecycleViewAdapter.notifyDataSetChanged()
                viewGroups[1]?.let {
                    viewGroups[0] = viewGroups[1]
                }
                viewGroups[1] = videoPlayLayout

                itemVideoPlayListener?.onItemVideoPlay(viewGroups,position)
                playerView.videoUrl = bean.videoUri
            }
        }
    }

    interface OnItemVideoPlayListener{
        fun onItemVideoPlay(parentViews: Array<ViewGroup?>,position:Int)
    }


}
