package com.anspark.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.adapters.MessagesAdapter;
import com.anspark.utils.ImageUtils;
import com.anspark.viewmodel.MessageViewModel;
import com.google.android.material.button.MaterialButton;

public class ChatActivity extends AppCompatActivity {

    private MessagesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private MessageViewModel viewModel;
    private Long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageView chatAvatar = findViewById(R.id.chatAvatar);
        TextView chatTitle = findViewById(R.id.chatTitle);
        TextView chatStatus = findViewById(R.id.chatStatus);
        EditText inputMessage = findViewById(R.id.inputMessage);
        MaterialButton sendButton = findViewById(R.id.buttonSend);
        RecyclerView messagesList = findViewById(R.id.messagesList);

        String name = getIntent().getStringExtra("chat_name");
        String chatIdStr = getIntent().getStringExtra("chat_id");
        String participantName = extractParticipantName(name);

        if (chatIdStr == null || chatIdStr.isEmpty()) {
            chatId = 0L;
        } else {
            try {
                chatId = Long.parseLong(chatIdStr);
            } catch (NumberFormatException e) {
                chatId = 0L;
            }
        }

        if (name == null || name.isEmpty()) {
            name = "Maja, 24";
        }

        chatAvatar.setImageResource(ImageUtils.pickChatPlaceholder(
                !TextUtils.isEmpty(chatIdStr) ? chatIdStr : name
        ));
        chatTitle.setText(name);
        chatStatus.setText("Online teraz");

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(participantName, chatIdStr);
        messagesList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        viewModel.getMessages().observe(this, messages -> {
            adapter.submitList(messages);
            if (messages != null && !messages.isEmpty()) {
                messagesList.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadMessages(chatId);

        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }
            inputMessage.setText("");
            viewModel.sendMessage(chatId, message);
        });
    }

    private String extractParticipantName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "Match";
        }

        String[] parts = fullName.split(",");
        String shortName = parts.length > 0 ? parts[0].trim() : fullName.trim();
        return shortName.isEmpty() ? "Match" : shortName;
    }
}
