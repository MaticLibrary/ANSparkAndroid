package com.anspark.api;

import com.anspark.models.Chat;
import com.anspark.models.Message;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ChatApi {
    @GET("api/v1/matches/{matchId}/messages")    // ← змінити
    Call<List<Message>> getMessages(@Path("matchId") long matchId);

    @POST("api/v1/matches/{matchId}/messages")   // ← змінити
    Call<Message> sendMessage(@Path("matchId") long matchId, @Body String content);
}