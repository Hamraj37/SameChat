package com.samechat37.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.samechat37.R;
import com.samechat37.models.Message;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private Context context;
    private List<Message> mediaMessages;
    private OnMediaClickListener listener;

    public interface OnMediaClickListener {
        void onMediaClick(Message message);
    }

    public MediaAdapter(Context context, List<Message> mediaMessages, OnMediaClickListener listener) {
        this.context = context;
        this.mediaMessages = mediaMessages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_thumbnail, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Message message = mediaMessages.get(position);
        
        // mediaUrl is already decrypted when passed to this list or we decrypt it here
        // For simplicity, let's assume we pass decrypted info or the adapter handles it.
        // Actually, it's better to pass decrypted URL to the adapter or have a method to get it.
        
        String type = message.getType();
        holder.videoIcon.setVisibility("video".equalsIgnoreCase(type) ? View.VISIBLE : View.GONE);

        // We need the raw URL from the mediaInfo JSON
        String mediaInfo = message.getMediaUrl(); 
        String url = null;
        try {
            // Check if it's already a direct URL (shouldn't be if E2EE is on) or a JSON
            if (mediaInfo != null && mediaInfo.startsWith("{")) {
                org.json.JSONObject json = new org.json.JSONObject(mediaInfo);
                url = json.optString("u");
            } else {
                url = mediaInfo;
            }
        } catch (Exception e) {
            url = mediaInfo;
        }

        if (url != null) {
            Glide.with(context)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.unread_badge_bg) // fallback
                    .into(holder.thumbnail);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMediaClick(message);
        });
    }

    @Override
    public int getItemCount() {
        return mediaMessages.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        ImageView videoIcon;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            videoIcon = itemView.findViewById(R.id.video_icon);
        }
    }
}
