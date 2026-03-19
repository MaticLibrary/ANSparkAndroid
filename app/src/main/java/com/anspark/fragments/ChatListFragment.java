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
import com.anspark.viewmodel.MatchViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private MatchViewModel viewModel;
    private ChatAdapter adapter;

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

        viewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        viewModel.getMatches().observe(getViewLifecycleOwner(), matches -> {
            List<Chat> chats = convertMatchesToChats(matches);
            adapter.submitList(chats);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadMatches();
    }

    private List<Chat> convertMatchesToChats(List<Match> matches) {
        List<Chat> chats = new ArrayList<>();
        if (matches != null) {
            for (Match match : matches) {
                Chat chat = new Chat();
                chat.setId(match.getId());
                chat.setParticipant(match.getProfile());
                chat.setLastMessage(null);
                chat.setLastMessageAt(match.getMatchedAt());
                chats.add(chat);
            }
        }
        return chats;
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