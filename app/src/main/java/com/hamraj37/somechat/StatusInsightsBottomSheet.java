package com.hamraj37.somechat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hamraj37.somechat.adapters.UserAdapter;
import com.hamraj37.somechat.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatusInsightsBottomSheet extends BottomSheetDialogFragment {

    private Map<String, Long> views;
    private Map<String, Boolean> likes;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private TextView insightTitle;

    public static StatusInsightsBottomSheet newInstance(Map<String, Long> views, Map<String, Boolean> likes) {
        StatusInsightsBottomSheet fragment = new StatusInsightsBottomSheet();
        fragment.views = views;
        fragment.likes = likes;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_status_insights, container, false);

        recyclerView = view.findViewById(R.id.insights_recycler);
        emptyText = view.findViewById(R.id.empty_insight_text);
        insightTitle = view.findViewById(R.id.insight_title);

        adapter = new UserAdapter(userList, new UserAdapter.OnUserClickListener() {
            @Override public void onUserClick(User user) {}
            @Override public void onNewGroupClick() {}
        });
        adapter.setLikesMap(likes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadUsers();
        return view;
    }

    private void loadUsers() {
        userList.clear();
        adapter.notifyDataSetChanged();
        
        java.util.Set<String> uids = new java.util.HashSet<>();
        if (views != null) uids.addAll(views.keySet());
        if (likes != null) uids.addAll(likes.keySet());
        
        int viewsCount = views != null ? views.size() : 0;
        insightTitle.setText(viewsCount + " Views");

        if (uids.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No views yet");
            return;
        }

        emptyText.setVisibility(View.GONE);
        final int total = uids.size();
        final int[] count = {0};

        for (String uid : uids) {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                userList.add(user);
                            }
                            count[0]++;
                            if (count[0] == total) {
                                // Sort: Liked users first, then by display name
                                userList.sort((u1, u2) -> {
                                    boolean l1 = likes != null && Objects.equals(likes.get(u1.getUid()), true);
                                    boolean l2 = likes != null && Objects.equals(likes.get(u2.getUid()), true);
                                    if (l1 != l2) return l1 ? -1 : 1;
                                    return u1.getDisplayName().compareToIgnoreCase(u2.getDisplayName());
                                });
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            count[0]++;
                        }
                    });
        }
    }
}
