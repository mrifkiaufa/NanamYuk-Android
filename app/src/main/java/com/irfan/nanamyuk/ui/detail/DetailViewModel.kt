package com.irfan.nanamyuk.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.irfan.nanamyuk.data.api.*
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_URL
import com.irfan.nanamyuk.data.datastore.SessionModel
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel(private val pref: SessionPreferences) : ViewModel() {
    private val _plant = MutableLiveData<PlantResponseItem>()
    val plant: LiveData<PlantResponseItem> = _plant

    private val _isLoading = MutableLiveData<Boolean>()

    private val _state = MutableLiveData<Boolean>()
    val state : LiveData<Boolean> = _state

    fun getUserToken(): LiveData<SessionModel> {
        return pref.getToken().asLiveData()
    }

    fun getPlant(token: String, id: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getPlantById("Bearer $token", id)
        client.enqueue(object : Callback<PlantResponse> {
            override fun onResponse(call: Call<PlantResponse>, response: Response<PlantResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    _plant.value = response.body()?.response
                    Log.e("tes respon", _plant.toString())

                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<PlantResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun deleteUserPlants(token: String, id: String){
        _isLoading.value = true
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).deleteUserPlants("Bearer $token", id)
        client.enqueue(object : Callback<UserPlantResponse> {
            override fun onResponse(
                call: Call<UserPlantResponse>,
                response: Response<UserPlantResponse>
            ) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    _state.value = true
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<UserPlantResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    companion object {
        private const val TAG = "DetailViewModel"
    }
}