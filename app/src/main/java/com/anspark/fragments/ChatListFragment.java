package com.anspark.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anspark.R;
import com.anspark.activities.ChatActivity;
import com.anspark.adapters.ChatAdapter;
import com.anspark.models.Chat;
import com.anspark.models.Match;
import com.anspark.viewmodel.ChatListViewModel;
import com.anspark.viewmodel.MatchViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatListFragment extends Fragment {
    private MatchViewModel matchViewModel;
    private ChatListViewModel chatListViewModel;
    private ChatAdapter adapter;
    private List<Match> latestMatches = new ArrayList<>();
    private List<Chat> latestChatSummaries = new ArrayList<>();

    public ChatListFragment() {
        super(R.layout.fragment_chat_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ChatAdapter(this::openChat);
        recyclerView.setAdapter(adapter);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        chatListViewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        matchViewModel.getMatches().observe(getViewLifecycleOwner(), matches -> {
            latestMatches = matches != null ? matches : new ArrayList<>();
            renderChats();
        });

        matchViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        chatListViewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            latestChatSummaries = chats != null ? chats : new ArrayList<>();
            renderChats();
        });

        chatListViewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        matchViewModel.loadMatches();
        chatListViewModel.loadChats();
    }

    private void renderChats() {
        adapter.submitList(mergeMatchesWithChats(latestMatches, latestChatSummaries));
    }

    private List<Chat> mergeMatchesWithChats(List<Match> matches, List<Chat> chatSummaries) {
        List<Chat> chats = new ArrayList<>();
        Map<String, Chat> summariesByParticipant = new HashMap<>();

        if (chatSummaries != null) {
            for (Chat summary : chatSummaries) {
                String key = buildParticipantKey(summary);
                if (key != null) {
                    summariesByParticipant.put(key, summary);
                }
            }
        }

        if (matches != null) {
            for (Match match : matches) {
                Chat chat = new Chat();
                chat.setId(match.getId());
                chat.setParticipant(match.getProfile());

                Chat summary = summariesByParticipant.get(buildParticipantKey(match));
                if (summary != null) {
                    chat.setLastMessage(summary.getLastMessage());
                    chat.setLastMessageAt(summary.getLastMessageAt());
                } else {
                    chat.setLastMessage(null);
                    chat.setLastMessageAt(match.getMatchedAt());
                }

                chats.add(chat);
            }
        }

        return chats;
    }

    @Nullable
    private String buildParticipantKey(@Nullable Match match) {
        if (match == null || match.getProfile() == null) {
            return null;
        }

        if (match.getProfile().getId() != null) {
            return "id:" + match.getProfile().getId();
        }

        String displayName = match.getProfile().getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        return "name:" + displayName.trim().toLowerCase(Locale.ROOT);
    }

    @Nullable
    private String buildParticipantKey(@Nullable Chat chat) {
        if (chat == null || chat.getParticipant() == null) {
            return null;
        }

        if (chat.getParticipant().getId() != null) {
            return "id:" + chat.getParticipant().getId();
        }

        String displayName = chat.getParticipant().getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }

        return "name:" + displayName.trim().toLowerCase(Locale.ROOT);
    }

    private void openChat(Chat chat) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("chat_id", chat.getId());
        if (chat.getParticipant() != null) {
            String name = chat.getParticipant().getDisplayName() != null ?
                    chat.getParticipant().getDisplayName() : "Chat";
            if (chat.getParticipant().getAge() > 0) {
                name += ", " + chat.getParticipant().getAge();
            }
            intent.putExtra("chat_name", name);
        }
        startActivity(intent);
    }
}
