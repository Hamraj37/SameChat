package com.hamraj37.somechat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hamraj37.somechat.R;
import com.hamraj37.somechat.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ACTION = 1;
    private List<User> userList;
    private final OnUserClickListener listener;
    private boolean showActions = false;
    private boolean showRemoveButton = false;
    private boolean showRequestButtons = false;
    private boolean showCancelButton = false;
    private java.util.Map<String, Boolean> adminsMap = null;
    private java.util.Map<String, Boolean> likesMap = null;

    public interface OnUserClickListener {
        void onUserClick(User user);
        default void onRemoveClick(User user) {}
        default void onAcceptClick(User user) {}
        default void onDeclineClick(User user) {}
        default void onCancelClick(User user) {}
        void onNewGroupClick();
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void setShowActions(boolean show) {
        this.showActions = show;
        notifyDataSetChanged();
    }

    public void setShowRemoveButton(boolean show) {
        this.showRemoveButton = show;
        notifyDataSetChanged();
    }

    public void setShowRequestButtons(boolean show) {
        this.showRequestButtons = show;
        notifyDataSetChanged();
    }

    public void setShowCancelButton(boolean show) {
        this.showCancelButton = show;
        notifyDataSetChanged();
    }

    public void setAdminsMap(java.util.Map<String, Boolean> adminsMap) {
        this.adminsMap = adminsMap;
        notifyDataSetChanged();
    }

    public void setLikesMap(java.util.Map<String, Boolean> likesMap) {
        this.likesMap = likesMap;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showActions && position == 0) return VIEW_TYPE_ACTION;
        return VIEW_TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ACTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ActionViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActionViewHolder) {
            ActionViewHolder actionHolder = (ActionViewHolder) holder;
            actionHolder.displayName.setText("New Group");
            actionHolder.username.setVisibility(View.GONE);
            actionHolder.email.setVisibility(View.GONE);
            actionHolder.avatar.setImageResource(android.R.drawable.ic_menu_add);
            actionHolder.avatar.setBackgroundResource(R.drawable.online_indicator_bg);
            actionHolder.avatar.setPadding(12, 12, 12, 12);
            actionHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNewGroupClick();
            });
            return;
        }

        UserViewHolder userHolder = (UserViewHolder) holder;
        User user = userList.get(showActions ? position - 1 : position);
        userHolder.displayName.setText(user.getDisplayName());
        
        if (userHolder.adminBadge != null) {
            if (adminsMap != null && Boolean.TRUE.equals(adminsMap.get(user.getUid()))) {
                userHolder.adminBadge.setVisibility(View.VISIBLE);
            } else {
                userHolder.adminBadge.setVisibility(View.GONE);
            }
        }
        
        String myUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        
        if (showRequestButtons) {
            userHolder.email.setText("Sent you a friend request");
            userHolder.email.setVisibility(View.VISIBLE);
            userHolder.email.setTextColor(com.google.android.material.color.MaterialColors.getColor(userHolder.itemView.getContext(), androidx.appcompat.R.attr.colorPrimary, android.graphics.Color.BLUE));
            userHolder.email.setAlpha(1.0f);
        } else if (showCancelButton) {
            userHolder.email.setText("Waiting for approval...");
            userHolder.email.setVisibility(View.VISIBLE);
            userHolder.email.setTextColor(com.google.android.material.color.MaterialColors.getColor(userHolder.itemView.getContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, android.graphics.Color.GRAY));
            userHolder.email.setAlpha(0.6f);
        } else if (user.getUid() != null && user.getUid().equals(myUid)) {
            userHolder.email.setText(user.getEmail());
            userHolder.email.setVisibility(View.VISIBLE);
            userHolder.email.setAlpha(0.7f);
            userHolder.email.setTextColor(com.google.android.material.color.MaterialColors.getColor(userHolder.itemView.getContext(), com.google.android.material.R.attr.colorOnSurfaceVariant, android.graphics.Color.GRAY));
        } else {
            userHolder.email.setVisibility(View.GONE);
        }
        
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            userHolder.username.setText("@" + user.getUsername());
            userHolder.username.setVisibility(View.VISIBLE);
        } else {
            userHolder.username.setVisibility(View.GONE);
        }

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(userHolder.itemView.getContext())
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(userHolder.avatar);
        } else {
            userHolder.avatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        if (userHolder.likeIcon != null) {
            if (likesMap != null && Boolean.TRUE.equals(likesMap.get(user.getUid()))) {
                userHolder.likeIcon.setVisibility(View.VISIBLE);
            } else {
                userHolder.likeIcon.setVisibility(View.GONE);
            }
        }

        userHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        if (userHolder.itemCard != null) {
            if (showRequestButtons) {
                userHolder.itemCard.setCardBackgroundColor(android.graphics.Color.WHITE);
                userHolder.itemCard.setStrokeWidth((int) (1 * userHolder.itemView.getResources().getDisplayMetrics().density));
                userHolder.itemCard.setStrokeColor(android.graphics.Color.parseColor("#DDDDDD"));
                userHolder.itemCard.setCardElevation(2 * userHolder.itemView.getResources().getDisplayMetrics().density);
            } else {
                userHolder.itemCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
                userHolder.itemCard.setStrokeWidth(0);
                userHolder.itemCard.setCardElevation(0);
            }
        }

        if (userHolder.removeButton != null) {
            userHolder.removeButton.setVisibility(showRemoveButton ? View.VISIBLE : View.GONE);
            userHolder.removeButton.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveClick(user);
            });
        }

        if (userHolder.requestActions != null) {
            userHolder.requestActions.setVisibility(showRequestButtons ? View.VISIBLE : View.GONE);
            userHolder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAcceptClick(user);
            });
            userHolder.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDeclineClick(user);
            });
        }

        if (userHolder.cancelButton != null) {
            userHolder.cancelButton.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);
            userHolder.cancelButton.setOnClickListener(v -> {
                if (listener != null) listener.onCancelClick(user);
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size() + (showActions ? 1 : 0);
    }

    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName;
        TextView username;
        TextView email;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_avatar);
            displayName = itemView.findViewById(R.id.user_display_name);
            username = itemView.findViewById(R.id.user_username);
            email = itemView.findViewById(R.id.user_email);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView displayName;
        TextView username;
        TextView email;
        TextView adminBadge;
        ImageView likeIcon;
        View removeButton;
        View requestActions, btnAccept, btnDecline, cancelButton;
        com.google.android.material.card.MaterialCardView itemCard;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_avatar);
            displayName = itemView.findViewById(R.id.user_display_name);
            username = itemView.findViewById(R.id.user_username);
            email = itemView.findViewById(R.id.user_email);
            adminBadge = itemView.findViewById(R.id.admin_badge);
            likeIcon = itemView.findViewById(R.id.status_like_icon);
            removeButton = itemView.findViewById(R.id.btn_remove_user);
            requestActions = itemView.findViewById(R.id.request_actions);
            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnDecline = itemView.findViewById(R.id.btn_decline_request);
            cancelButton = itemView.findViewById(R.id.btn_cancel_request);
            itemCard = itemView.findViewById(R.id.user_item_card);
        }
    }
}
