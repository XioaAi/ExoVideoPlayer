package com.example.exovideoplayer

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        video_play.setOnClickListener{
            startActivity(Intent(this@MainActivity,VideoPlayActivity::class.java))
        }

        video_list_play.setOnClickListener{
            startActivity(Intent(this@MainActivity,VideoListPlayActivity::class.java))
        }



    }
}
