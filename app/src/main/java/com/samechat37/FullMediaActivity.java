package com.samechat37;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class FullMediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_media);

        ShapeableImageView fullImage = findViewById(R.id.full_image);
        VideoView fullVideo = findViewById(R.id.full_video);
        CircularProgressIndicator loading = findViewById(R.id.loading_progress);
        ImageButton btnBack = findViewById(R.id.btn_back);

        String type = getIntent().getStringExtra("type");
        String mediaUrl = getIntent().getStringExtra("url");

        btnBack.setOnClickListener(v -> finish());

        if ("image".equals(type)) {
            fullImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(mediaUrl)
                    .into(fullImage);
            loading.setVisibility(View.GONE);
        } else if ("video".equals(type)) {
            fullVideo.setVisibility(View.VISIBLE);
            fullVideo.setVideoPath(mediaUrl);
            fullVideo.setOnPreparedListener(mp -> {
                loading.setVisibility(View.GONE);
                fullVideo.start();
            });
            fullVideo.setOnErrorListener((mp, what, extra) -> {
                loading.setVisibility(View.GONE);
                return false;
            });
            
            // Add media controller for playback control
            android.widget.MediaController mediaController = new android.widget.MediaController(this);
            mediaController.setAnchorView(fullVideo);
            fullVideo.setMediaController(mediaController);
        }
    }
}
