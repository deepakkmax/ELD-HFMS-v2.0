package com.hutchsystems.hutchconnect;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayer extends AppCompatActivity {

    VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_player);
        initialize();
    }

    // Created By: Deepak Sharma
    // Created Date: 25 March 2020
    // Purpose: initialize video
    private void initialize() {

        videoView = findViewById(R.id.videoPlayer);
        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        //specify the location of media file
        // Uri uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        if (getIntent() != null) {
            Uri uri = getIntent().getData();

            //Setting MediaController and URI, then starting the videoView
            videoView.setMediaController(mediaController);

            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mediaPlayer) {
                    // close the progress bar and play the video

                    //if we have a position on savedInstanceState, the video playback should start from here

                    videoView.start();

                }
            });
        }
    }

}