package com.irfan.nanamyuk.data.api

import com.google.gson.annotations.SerializedName

data class SessionsResponse(
    @field:SerializedName("response")
    val response: List<SessionResponseItem>,

    @field:SerializedName("status")
    val status: String,
)

data class SessionResponse(
    @field:SerializedName("response")
    val response: SessionResponseItem,

    @field:SerializedName("status")
    val status: String,
)

data class SessionResponseItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("date")
    val tanggal: String,

    @field:SerializedName("user_plants_id")
    val userPlantID: String
)
