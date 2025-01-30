package com.example.hospitalfinder;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Field;
import retrofit2.http.Query;

public interface APIService {
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("register.php")
    Call<RegisterResponse> register(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("save_location.php")
    Call<ServerResponse> saveUserLocation(
            @Field("username") String username,
            @Field("state") String state,
            @Field("district") String district,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    // ✅ Corrected GET request to fetch user location
    @GET("getUserLocation.php")
    Call<UserLocationResponse> getUserLocation(
            @Query("username") String username
    );
}
