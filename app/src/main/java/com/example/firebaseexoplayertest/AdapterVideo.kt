package com.example.firebaseexoplayertest

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class AdapterVideo(
    private var context: Context,
    private var videoArrayList: ArrayList<ModelVideo>?
) : RecyclerView.Adapter<AdapterVideo.HolderVideo>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderVideo {
        //inflate layout row_video.xml
        val view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false)
        return HolderVideo(view)
    }

    override fun onBindViewHolder(holder: HolderVideo, position: Int) {
        /*get data, set data, handle clicks etc*/
        //get data
        val modelVideo = videoArrayList!! [position]

        //get specific data
        val id: String? = modelVideo.id
        val title: String? = modelVideo.title
        val timestamp: String? = modelVideo.timestamp
        val videoUri: String? = modelVideo.videoUri

        //format date e.g. 28/09/2020 05:10PM
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp!!.toLong()
        val formattedDateTime = android.text.format.DateFormat.format("dd/MM/yyyy K:mm a", calendar).toString()
    
        //set data
        holder.titleTv.text = title
        holder.timeTv.text = formattedDateTime
        setVideoUrl(modelVideo, holder)
    
    }

    private fun setVideoUrl(modelVideo: ModelVideo, holder: HolderVideo) {
        //show progress
        holder.progressBar.visibility = View.VISIBLE

        //get video url
        val videoUrl: String? = modelVideo.videoUri

        //MediaController for play/pause/time etc
        val mediaController = MediaController(context)
        mediaController.setAnchorView(holder.videoView)
        val videoUri = Uri.parse(videoUrl)

        holder.videoView.setMediaController(mediaController)
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.requestFocus()

        holder.videoView.setOnPreparedListener { mediaPlayer ->
            //video is prepared to play
            mediaPlayer.start()
        }
        holder.videoView.setOnInfoListener(MediaPlayer.OnInfoListener{mp, what, extra ->
            //check if buffering/rendering etc
            when(what){
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    //rendering started
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    //buffering started
                    holder.progressBar.visibility = View.VISIBLE
                    return@OnInfoListener true
                }

                MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    //buffering ended
                    holder.progressBar.visibility = View.GONE
                    return@OnInfoListener true
                }
            }
            false
        })

        holder.videoView.setOnCompletionListener { mediaPlayer ->
            //restart video when completed | loop video
            mediaPlayer.start()
        }
    }

    override fun getItemCount(): Int {
        return videoArrayList!!.size //return size/length or the arraylist
    }

    //view holder class holds and inits UI Views or row_video.xml
    class HolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //init UI Views
        var videoView:VideoView = itemView.findViewById(R.id.videoView)
        var titleTv:TextView = itemView.findViewById(R.id.titleTv)
        var timeTv:TextView = itemView.findViewById(R.id.timeTv)
        var progressBar:ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}