package com.irfan.nanamyuk.data.api

import com.irfan.nanamyuk.WeatherResponse
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import retrofit2.Call
import retrofit2.http.*


interface ServiceApi {
    @POST("auth/login")
    fun postLogin(
        @Body params: HashMap<String, String>
    ) : Call<AuthResponse>

    @POST("auth/logout")
    fun logout(
        @Header("Authorization") header: String,
    ) : Call<AuthResponse>

    @POST("auth/register")
    fun postRegister(
        @Body params: HashMap<String, String>
    ) : Call<AuthResponse>

    @GET("weather")
    fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ) : Call<WeatherResponse>

    @GET("session")
    fun getSessions(
        @Header("Authorization") header: String
    ) : Call<SessionsResponse>

    @GET("session/{id}")
    fun getSession(
        @Header("Authorization") header: String,
        @Path("id") id: String
    ) : Call<SessionResponse>

    @POST("session")
    fun postSession(
        @Header("Authorization") header: String,
        @Body params: HashMap<String, Any>
    ) : Call<SessionResponse>

    @POST("session/{id}")
    fun updateSession(
        @Header("Authorization") header: String,
        @Path("id") id: String,
        @Body params: HashMap<String, Any>
    ) : Call<SessionResponse>

    @GET("Plant")
    fun getPlant(
        @Header("Authorization") header: String
    ) : Call<PlantsResponse>

    @GET("Plant/{id}")
    fun getPlantById(
        @Header("Authorization") header: String,
        @Path("id") id: String
    ) : Call<PlantResponse>

    @GET("UserPlants?\$lookup=*")
    fun getUserPlants(
        @Header("Authorization") header: String,
    ) : Call<UserPlantsResponse>

    @GET("UserPlants/{id}")
    fun getUserPlant(
        @Header("Authorization") header: String,
        @Path("id") id: String
    ) : Call<UserPlantResponse>

    @POST("UserPlants")
    fun postUserPlants(
        @Header("Authorization") header: String,
        @Body params: HashMap<String, Any>
    ) : Call<UserPlantResponse>

    @PATCH("UserPlants/{id}")
    fun updateUserPlants(
        @Header("Authorization") header: String,
        @Path("id") id: String,
        @Body params: HashMap<String, Any>
    ): Call<UserPlantResponse>

    @DELETE("UserPlants/{id}")
    fun deleteUserPlants(
        @Header("Authorization") header: String,
        @Path("id") id: String
    ): Call<UserPlantResponse>

    @GET("predict")
    fun getRecommendation(
        @Header("Authorization") header: String,
        @Query("soil") soil: String,
        @Query("light") light: String,
        @Query("city") city: String
    ) : Call<RecomResponse>
}