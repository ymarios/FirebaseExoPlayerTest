package com.example.firebaseexoplayertest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.View
import android.widget.VideoView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_videos.*

class VideosActivity : AppCompatActivity() {

    //arraylist for video list
    private lateinit var videoArrayList: ArrayList<ModelVideo>
    //adapter
    private lateinit var adapterVideo: AdapterVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        //actionbar title
        title = "Videos"

        //function call to load videos from firebase
        loadVideosFromFirebase()

        //handle click
        addVideoFab.setOnClickListener {
            startActivity(Intent(this,AddVideosActivity::class.java))
        }
    }

    private fun loadVideosFromFirebase() {
        //init arraylist before adding data into it
        videoArrayList = ArrayList()

        //db reference of firebase
        val ref = FirebaseDatabase.getInstance().getReference("Videos")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into it
                videoArrayList.clear()
                for (ds in snapshot.children) {
                    //get data as model
                    val modelVideo = ds.getValue(ModelVideo::class.java)
                    //add to array list
                    videoArrayList.add(modelVideo!!)
                }
                //setup adapter
                adapterVideo = AdapterVideo(this@VideosActivity, videoArrayList)
                //set adapter to recyclerview
                videosRv.adapter = adapterVideo
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}