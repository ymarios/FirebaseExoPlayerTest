package com.example.firebaseexoplayertest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import kotlinx.android.synthetic.main.activity_videos.*

class VideosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        //actionbar title
        title = "Videos"

        //handle click
        addVideoFab.setOnClickListener {
            startActivity(Intent(this,AddVideosActivity::class.java))
        }
    }
}