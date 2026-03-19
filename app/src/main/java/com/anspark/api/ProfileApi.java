package com.anspark.api;

import com.anspark.models.Photo;
import com.anspark.models.Profile;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProfileApi {

    @GET("api/v1/profile/me")
    Call<Profile> getMyProfile();

    @PUT("api/v1/profile/me")
    Call<Profile> updateProfile(@Body Profile profile);

    @Multipart
    @POST("api/v1/profile/me/photo")
    Call<Photo> uploadPhoto(@Part MultipartBody.Part file);
}