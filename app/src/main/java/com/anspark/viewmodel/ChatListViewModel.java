package com.anspark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.anspark.models.Chat;
import com.anspark.repository.ChatRepository;
import com.anspark.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatListViewModel extends AndroidViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ChatListViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChatRepository(application);
    }

    public LiveData<List<Chat>> getChats() {
        return chats;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadChats() {
        repository.getChats(new RepositoryCallback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> data) {
                chats.postValue(data);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }
}
