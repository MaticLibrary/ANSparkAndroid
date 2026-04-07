package com.anspark.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Chat;
import com.anspark.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    private final List<Chat> items = new ArrayList<>();
    private final OnChatClickListener listener;

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Chat> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = items.get(position);

        String name = chat.getParticipant() != null
                ? chat.getParticipant().getDisplayName()
                : "Chat";

        if (chat.getParticipant() != null && chat.getParticipant().getAge() > 0) {
            name = name + ", " + chat.getParticipant().getAge();
        }

        String preview = chat.getLastMessage() != null && chat.getLastMessage().getText() != null
                && !chat.getLastMessage().getText().trim().isEmpty()
                ? chat.getLastMessage().getText().trim()
                : "Nowa rozmowa. Napisz pierwsza wiadomosc.";
        String timestamp = chat.getLastMessageAt() != null && !chat.getLastMessageAt().trim().isEmpty()
                ? chat.getLastMessageAt().trim()
                : "Nowy match";

        holder.name.setText(name);
        holder.message.setText(preview);
        holder.time.setText(timestamp);
        holder.avatar.setImageResource(ImageUtils.pickChatPlaceholder(chat.getId()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView message;
        final TextView time;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemChatAvatar);
            name = itemView.findViewById(R.id.itemChatName);
            message = itemView.findViewById(R.id.itemChatMessage);
            time = itemView.findViewById(R.id.itemChatTime);
        }
    }
}
