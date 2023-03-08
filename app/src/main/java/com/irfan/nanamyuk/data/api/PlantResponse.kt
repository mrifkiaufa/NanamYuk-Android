package com.irfan.nanamyuk.data.api

import com.google.gson.annotations.SerializedName

data class PlantResponseItem(
	@field:SerializedName("name")
	val namaTanaman: String,

	@field:SerializedName("image")
	val image: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("description")
	val deskripsi: String,

	@field:SerializedName("temperature")
	val suhu: String,

	@field:SerializedName("watering_duration")
	val durasiSiram: String,

	@field:SerializedName("soil")
	val tanah: String,

	@field:SerializedName("light")
	val cahaya: String,

	@field:SerializedName("humidity")
	val kelembapan: String,

	@field:SerializedName("rainfall")
	val rainfall: String,

	@field:SerializedName("tutorial")
	val tutorial: String
)
