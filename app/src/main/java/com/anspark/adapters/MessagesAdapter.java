package com.anspark.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.models.Message;
import com.anspark.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;

    private final List<Message> items = new ArrayList<>();
    private final String participantName;
    private final String participantSeed;

    public MessagesAdapter(String participantName, String participantSeed) {
        this.participantName = TextUtils.isEmpty(participantName) ? "Match" : participantName.trim();
        this.participantSeed = TextUtils.isEmpty(participantSeed) ? this.participantName : participantSeed.trim();
    }

    public void submitList(List<Message> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isOutgoing() ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == TYPE_OUTGOING
                ? R.layout.item_message_outgoing
                : R.layout.item_message;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = items.get(position);
        holder.sender.setText(message.isOutgoing() ? "Ty" : participantName);
        holder.text.setText(message.getText() != null ? message.getText() : "");

        String timestamp = formatTimestamp(message.getCreatedAt());
        if (TextUtils.isEmpty(timestamp)) {
            holder.meta.setVisibility(View.GONE);
        } else {
            holder.meta.setVisibility(View.VISIBLE);
            holder.meta.setText(timestamp);
        }

        if (message.isOutgoing()) {
            holder.avatar.setImageResource(R.drawable.male_profile);
        } else {
            String seed = !TextUtils.isEmpty(message.getSenderId()) ? message.getSenderId() : participantSeed;
            holder.avatar.setImageResource(ImageUtils.pickChatPlaceholder(seed));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatTimestamp(String rawTimestamp) {
        if (TextUtils.isEmpty(rawTimestamp)) {
            return "";
        }

        String value = rawTimestamp.trim();
        if (value.isEmpty()) {
            return "";
        }

        if ("now".equalsIgnoreCase(value)) {
            return "Teraz";
        }

        int separatorIndex = value.indexOf('T');
        if (separatorIndex >= 0 && value.length() >= separatorIndex + 6) {
            return value.substring(separatorIndex + 1, separatorIndex + 6);
        }

        return value;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView sender;
        final TextView text;
        final TextView meta;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemMessageAvatar);
            sender = itemView.findViewById(R.id.itemMessageSender);
            text = itemView.findViewById(R.id.itemMessageText);
            meta = itemView.findViewById(R.id.itemMessageMeta);
        }
    }
}
