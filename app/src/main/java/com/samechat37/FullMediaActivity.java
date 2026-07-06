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
        String encryptedInfo = getIntent().getStringExtra("encrypted_info");

        btnBack.setOnClickListener(v -> finish());

        if (encryptedInfo != null) {
            downloadAndDecrypt(encryptedInfo, type, fullImage, fullVideo, loading);
        } else if (mediaUrl != null) {
            displayMedia(mediaUrl, type, fullImage, fullVideo, loading);
        }
    }

    private void downloadAndDecrypt(String encryptedInfo, String type, ShapeableImageView fullImage, VideoView fullVideo, CircularProgressIndicator loading) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(encryptedInfo);
            String url = json.getString("u");
            String key = json.getString("k");

            com.samechat37.utils.GitHubStorage.downloadFile(url, new okhttp3.Callback() {
                @Override
                public void onFailure(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull java.io.IOException e) {
                    runOnUiThread(() -> loading.setVisibility(View.GONE));
                }

                @Override
                public void onResponse(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull okhttp3.Response response) throws java.io.IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            byte[] encryptedBytes = response.body().bytes();
                            javax.crypto.SecretKey secretKey = com.samechat37.utils.EncryptionManager.decodeKey(key);
                            byte[] decryptedBytes = com.samechat37.utils.EncryptionManager.decryptRaw(encryptedBytes, secretKey);

                            runOnUiThread(() -> {
                                if ("image".equals(type)) {
                                    fullImage.setVisibility(View.VISIBLE);
                                    com.bumptech.glide.Glide.with(FullMediaActivity.this).load(decryptedBytes).into(fullImage);
                                    loading.setVisibility(View.GONE);
                                } else {
                                    try {
                                        java.io.File tempFile = java.io.File.createTempFile("decrypted", ".mp4", getCacheDir());
                                        java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
                                        fos.write(decryptedBytes);
                                        fos.close();
                                        displayMedia(tempFile.getAbsolutePath(), type, fullImage, fullVideo, loading);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayMedia(String path, String type, ShapeableImageView fullImage, VideoView fullVideo, CircularProgressIndicator loading) {
        if ("image".equals(type)) {
            fullImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(path)
                    .into(fullImage);
            loading.setVisibility(View.GONE);
        } else if ("video".equals(type)) {
            fullVideo.setVisibility(View.VISIBLE);
            fullVideo.setVideoPath(path);
            fullVideo.setOnPreparedListener(mp -> {
                loading.setVisibility(View.GONE);
                fullVideo.start();
            });
            fullVideo.setOnErrorListener((mp, what, extra) -> {
                loading.setVisibility(View.GONE);
                return false;
            });
            
            android.widget.MediaController mediaController = new android.widget.MediaController(this);
            mediaController.setAnchorView(fullVideo);
            fullVideo.setMediaController(mediaController);
        }
    }
}
