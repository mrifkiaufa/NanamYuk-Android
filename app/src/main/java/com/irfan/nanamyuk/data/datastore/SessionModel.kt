package com.irfan.nanamyuk.data.datastore

data class SessionModel(
    val token: String,
    val name: String,
    val id: String,
    val lat: String,
    val lon: String,
    val temperature: String
)