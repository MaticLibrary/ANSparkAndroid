package com.anspark.api;

import com.anspark.models.AuthResponse;
import com.anspark.models.LoginRequest;
import com.anspark.models.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/v1/auth/logout")
    Call<Void> logout();
}