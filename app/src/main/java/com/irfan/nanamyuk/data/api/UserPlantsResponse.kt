package com.irfan.nanamyuk.data.api

import com.google.gson.annotations.SerializedName

data class UserPlantsResponse(
	@field:SerializedName("response")
	val response: List<UserPlantsResponseItem>,

	@field:SerializedName("status")
	val status: String,
)

data class UserPlantResponse(
	@field:SerializedName("response")
	val response: UserPlantsResponseItem,

	@field:SerializedName("status")
	val status: String,
)

data class UserPlantsResponseItem(

	@field:SerializedName("tag_name")
	val namaPenanda: String,

	@field:SerializedName("plant")
	val plant: PlantItem,

	@field:SerializedName("date")
	val date: String,

	@field:SerializedName("watering_state")
	val wateringState: Boolean,

	@field:SerializedName("dry_state")
	val dryState: Boolean,

	@field:SerializedName("humid_state")
	val humidState: Boolean,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("user")
	val user: UserItem
)

data class PlantItem(
	@field:SerializedName("image")
	val image: String,

	@field:SerializedName("plant_name")
	val namaTanaman: String,

	@field:SerializedName("watering_duration")
	val durasiSiram: String,

	@field:SerializedName("plant_id")
	val id: String
)

data class UserItem(
	@field:SerializedName("user_name")
	val name: String,

	@field:SerializedName("user_id")
	val id: String,
)
