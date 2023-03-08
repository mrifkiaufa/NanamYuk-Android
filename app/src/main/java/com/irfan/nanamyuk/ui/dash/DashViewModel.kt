package com.irfan.nanamyuk.ui.dash

import android.util.Log
import androidx.lifecycle.*
import com.irfan.nanamyuk.WeatherResponse
import com.irfan.nanamyuk.data.api.ConfigApi
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_OWM
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_URL
import com.irfan.nanamyuk.data.api.UserPlantsResponseItem
import com.irfan.nanamyuk.data.datastore.SessionModel
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashViewModel(private val pref: SessionPreferences) : ViewModel() {
    private val _userplants = MutableLiveData<List<UserPlantsResponseItem>>()
    val userplants: LiveData<List<UserPlantsResponseItem>> = _userplants

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    fun getUserToken(): LiveData<SessionModel> {
        return pref.getToken().asLiveData()
    }

    fun setTemperature(temperature: String) {
        viewModelScope.launch {
            pref.postTemperature(temperature)
        }
    }

    private val _state = MutableLiveData<Boolean>()
    val state: LiveData<Boolean> = _state

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getWeather(lat: String, lon: String, appid: String) {
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_OWM).getWeather(lat, lon, appid)
        client.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    _weather.value = response.body()
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }
    
    fun getUserPlants(token: String) {
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getUserPlants("Bearer $token")
        client.enqueue(object : Callback<List<UserPlantsResponseItem>> {
            override fun onResponse(call: Call<List<UserPlantsResponseItem>>, response: Response<List<UserPlantsResponseItem>>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    _userplants.value = response.body()
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<List<UserPlantsResponseItem>>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun updateUserPlants(token: String, map : HashMap<String, Any>, id: String){
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).updateUserPlants("Bearer $token", id, map)
        client.enqueue(object : Callback<UserPlantsResponseItem> {
            override fun onResponse(
                call: Call<UserPlantsResponseItem>,
                response: Response<UserPlantsResponseItem>
            ) {
                if (response.isSuccessful){
                    _state.value = true

                }
            }

            override fun onFailure(call: Call<UserPlantsResponseItem>, t: Throwable) {
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }

        })
    }

    companion object {
        private const val TAG = "DashViewModel"
    }

}