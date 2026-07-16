package com.hamraj37.somechat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.hamraj37.somechat.adapters.MessageAdapter;
import com.hamraj37.somechat.models.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private final List<Message> messageList = new ArrayList<>();
    private EditText messageInput;
    private LinearProgressIndicator typingIndicator;
    private String myUid;
    private static final String AI_ID = "somechat_ai";
    private final OkHttpClient client = new OkHttpClient();
    private String currentModelId = "google/gemma-4-26b-a4b-it:free";
    private static final String OPENROUTER_API_KEY = BuildConfig.OPENROUTER_API_KEY;

    private static final String PREFS_NAME = "AIChatPrefs";
    private static final String KEY_MODEL_ID = "selected_model_id";
    private static final String KEY_MODEL_SHORT_NAME = "selected_model_short_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        boolean isNightMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(!isNightMode);
        
        setContentView(R.layout.activity_ai_chat);

        myUid = FirebaseAuth.getInstance().getUid();

        // Load saved model
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentModelId = prefs.getString(KEY_MODEL_ID, "google/gemma-4-26b-a4b-it:free");
        String savedShortName = prefs.getString(KEY_MODEL_SHORT_NAME, "Gemma 4");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.ai_chat_recycler);
        messageInput = findViewById(R.id.ai_message_input);
        typingIndicator = findViewById(R.id.ai_typing_indicator);
        android.widget.TextView aiStatus = findViewById(R.id.ai_status);
        aiStatus.setText(getString(R.string.ai_online, savedShortName));

        findViewById(R.id.ai_header_container).setOnClickListener(this::showModelSelectionMenu);
        findViewById(R.id.btn_ai_about).setOnClickListener(v -> showAboutDialog());

        adapter = new MessageAdapter(messageList, new MessageAdapter.OnMessageClickListener() {
            @Override public void onReplyClick(String messageId) {}
            @Override public void onMessageLongClick(Message message, View view) {}
            @Override public void onMessageClick(Message message) {}
            @Override public void onSelectionChanged(int count) {}
            @Override public void onReactionClick(Message message, String emoji) {}
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_ai_send).setOnClickListener(v -> sendMessage());

        // Initial AI greeting
        addAiMessage(getString(R.string.ai_greeting));
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        messageInput.setText("");
        
        Message userMsg = new Message(UUID.randomUUID().toString(), myUid, AI_ID, text, System.currentTimeMillis());
        messageList.add(userMsg);
        adapter.notifyItemInserted(messageList.size());
        recyclerView.scrollToPosition(messageList.size());

        getAiResponse(text);
    }

    private void getAiResponse(String userText) {
        typingIndicator.setVisibility(View.VISIBLE);

        try {
            JSONObject root = new JSONObject();
            root.put("model", currentModelId);
            
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userText);
            messages.put(userMsg);
            
            root.put("messages", messages);
            root.put("stream", true);

            RequestBody body = RequestBody.create(root.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .addHeader("HTTP-Referer", "https://github.com/hamraj37/SomeChat")
                    .addHeader("X-Title", "SomeChat")
                    .build();

            // Use a specific AI message for streaming
            Message streamingMsg = new Message(UUID.randomUUID().toString(), AI_ID, myUid, "", System.currentTimeMillis());
            final StringBuilder aiContent = new StringBuilder();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        typingIndicator.setVisibility(View.GONE);
                        Toast.makeText(AIChatActivity.this, "AI Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            typingIndicator.setVisibility(View.GONE);
                            Toast.makeText(AIChatActivity.this, "AI Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    if (response.body() == null) return;
                    okio.BufferedSource source = response.body().source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line != null && line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if (data.equals("[DONE]")) break;

                            try {
                                JSONObject chunk = new JSONObject(data);
                                if (chunk.has("choices")) {
                                    JSONObject delta = chunk.getJSONArray("choices").getJSONObject(0).getJSONObject("delta");
                                    if (delta.has("content")) {
                                        String content = delta.getString("content");
                                        aiContent.append(content);
                                        
                                        runOnUiThread(() -> {
                                            if (typingIndicator.getVisibility() == View.VISIBLE) {
                                                typingIndicator.setVisibility(View.GONE);
                                                messageList.add(streamingMsg);
                                                adapter.notifyItemInserted(messageList.size());
                                            }
                                            streamingMsg.setText(aiContent.toString());
                                            adapter.notifyItemChanged(messageList.size());
                                            recyclerView.scrollToPosition(messageList.size());
                                        });
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    runOnUiThread(() -> typingIndicator.setVisibility(View.GONE));
                }
            });

        } catch (Exception e) {
            typingIndicator.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private void addAiMessage(String text) {
        Message aiMsg = new Message(UUID.randomUUID().toString(), AI_ID, myUid, text, System.currentTimeMillis());
        messageList.add(aiMsg);
        adapter.notifyItemInserted(messageList.size());
        recyclerView.scrollToPosition(messageList.size());
    }

    private void showModelSelectionMenu(View v) {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, v);
        String[] models = {
                "google/gemma-4-26b-a4b-it:free",
                "tencent/hy3:free",
                "poolside/laguna-xs-2.1:free",
                "nvidia/nemotron-3-super-120b-a12b:free"
        };
        String[] modelNames = {
                "Gemma 4 26B (Google)",
                "Hunyuan 3 (Tencent)",
                "Laguna XS 2.1 (Poolside)",
                "Nemotron 3 Super 120B (NVIDIA)"
        };
        String[] shortNames = {
                "Gemma 4",
                "Hunyuan 3",
                "Laguna XS",
                "Nemotron 3"
        };

        for (int i = 0; i < models.length; i++) {
            popupMenu.getMenu().add(0, i, i, modelNames[i]);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            currentModelId = models[index];
            String shortName = shortNames[index];
            
            android.widget.TextView aiStatus = findViewById(R.id.ai_status);
            aiStatus.setText(getString(R.string.ai_online, shortName));

            // Save selection
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(KEY_MODEL_ID, currentModelId)
                    .putString(KEY_MODEL_SHORT_NAME, shortName)
                    .apply();
            
            Toast.makeText(this, "Model changed to: " + modelNames[index], Toast.LENGTH_SHORT).show();
            return true;
        });
        popupMenu.show();
    }

    private void showAboutDialog() {
        String title = "";
        String message = "";
        
        if (currentModelId.contains("gemma-4")) {
            title = "About Gemma 4";
            message = "Developed by Google. A state-of-the-art model with 26B parameters, designed for high-quality reasoning, creative writing, and complex problem-solving.";
        } else if (currentModelId.contains("hy3")) {
            title = "About Hunyuan 3";
            message = "Developed by Tencent. A highly intelligent and versatile model with expertise in SEO, Programming, Science, Technology, and multi-language translation.";
        } else if (currentModelId.contains("laguna")) {
            title = "About Laguna XS";
            message = "Developed by Poolside. An ultra-fast, lightweight model optimized for lightning-quick responses and efficient software engineering assistance.";
        } else if (currentModelId.contains("nemotron")) {
            title = "About Nemotron 3";
            message = "Developed by NVIDIA. A massive 120B parameter model that excels at high-performance tasks, complex reasoning, and providing detailed, accurate information.";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
