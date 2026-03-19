package com.anspark.api;

import com.anspark.models.Match;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface MatchApi {
    @POST("api/v1/likes/")
    Call<Map<String, Object>> like(@Body Map<String, Object> likeRequest);

    @GET("api/v1/likes/matches")
    Call<List<Match>> getMatches();  // ← замість getLikedMatches()!
}