package com.example.firebaseexoplayertest

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_video.*

class AddVideosActivity : AppCompatActivity() {

    //actionbar
    private lateinit var actionBar: ActionBar

    //constants to pick video
    private val VIDEO_PICK_GALLERY_CODE = 100
    private val VIDEO_PICK_CAMERA_CODE = 101
    //constant to request camera permission to record video from camera
    private val CAMERA_REQUEST_CODE = 102

    //array for camera request permissions
    private lateinit var cameraPermissions:Array<String>

    //progress bar
    private lateinit var progressDialog: ProgressDialog

    private var videoUri: Uri? = null //uri of picked video

    private var title:String = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video)

        //init actionbar
        actionBar = supportActionBar!!
        //title
        actionBar.title = "Add New Video"
        //add back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        //init camera permission array
        cameraPermissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init progressbar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Uploading Video...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, upload video
        uploadVideoBtn.setOnClickListener {
            //get title
            title = titleEt.text.toString().trim()
            if (TextUtils.isEmpty(title)){
                //no title is entered
                Toast.makeText(this,"Title is required",Toast.LENGTH_SHORT).show()
            }
            else if (videoUri == null) {
                //video is not picked
                Toast.makeText(this,"Pick the video first",Toast.LENGTH_SHORT).show()
            }
            else{
                //title entered, video picked, so now upload video
                uploadVideoFirebase()
            }
        }

        //handle click, pick video
        pickVideoFab.setOnClickListener {
            videoPickDialog()
        }
    }

    private fun uploadVideoFirebase() {
        //show progress
        progressDialog.show()

        //timestamp
        val timestamp = ""+System.currentTimeMillis()

        //file path and name in firebase storage
        val filePathName = "Videos/video_$timestamp"

        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathName)
        //upload video using uri of video to storage
        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                //uploaded get url of uploaded video
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result
                if (uriTask.isSuccessful) {
                    //video url is received successfully

                    //now we can add video details to firebase db
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = "$timestamp"
                    hashMap["title"] = "$title"
                    hashMap["videoUri"] = "$downloadUri"

                    //put the above info to db
                    val dbReference = FirebaseDatabase.getInstance().getReference("Videos")
                    dbReference.child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener { taskSnapshot ->
                            //video info added successfully
                            progressDialog.dismiss()
                            Toast.makeText(this,"Video Uploaded",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{ e ->
                            //failed adding video info
                            progressDialog.dismiss()
                            Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                //failed uploading
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setVideoToVideoView() {
        //set the picked video to video view

        //video play controls
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        //set media controller
        videoView.setMediaController(mediaController)
        //set video uri
        videoView.setVideoURI(videoUri)
        videoView.requestFocus()
        videoView.setOnPreparedListener {
            //when video is ready, by default don't play automatically
            videoView.pause()
        }

    }

    private fun videoPickDialog() {
        //options to display in dialog
        val options = arrayOf("Camera", "Gallery")

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Video From")
            .setItems(options){ dialogInterface, i->
                //handle item clicks
                if (i==0) {
                    //camera clicked
                    if (!checkCameraPermissions()) {
                        //permissions was not allowed, request
                        requestCameraPermission()
                    }
                    else{
                        //permissions was allowed, pick video
                        videoPickCamera()
                    }
                }
                else{
                    //gallery clicked
                    videoPickGallery()
                }
            }
            .show()
    }

    private fun requestCameraPermission(){
        //request camera permissions
        ActivityCompat.requestPermissions(
            this,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkCameraPermissions():Boolean{
        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result2 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        //return results as true/false
        return result1 && result2
    }

    private fun videoPickGallery(){
        //video pick intent gallery
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(intent,"Choose video"),
            VIDEO_PICK_GALLERY_CODE
        )
    }

    private fun videoPickCamera(){
        //video pick intent camera
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //goto previous activity
        return super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE ->
                if (grantResults.size > 0){
                    //check if permission allowed or denied
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        //both permissions allowed
                    }
                    else{
                        //both or one of those are denied
                        Toast.makeText(this,"Permissions denied", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK){
            //video is picked from camera or gallery
            if (requestCode == VIDEO_PICK_CAMERA_CODE){
                //video picked from camera
                videoUri == data!!.data
                setVideoToVideoView()
            }
            else if (requestCode == VIDEO_PICK_GALLERY_CODE){
                //video picked from gallery
                videoUri = data!!.data
                setVideoToVideoView()
            }
        }
        else{
            //cancelled picking video
            Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}