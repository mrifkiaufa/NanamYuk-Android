package com.irfan.nanamyuk.ui.dash

import android.util.Log
import androidx.lifecycle.*
import com.irfan.nanamyuk.WeatherResponse
import com.irfan.nanamyuk.data.api.*
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_OWM
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_URL
import com.irfan.nanamyuk.data.datastore.SessionModel
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.ui.detail.DetailViewModel
import com.irfan.nanamyuk.ui.pilih.PilihViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashViewModel(private val pref: SessionPreferences) : ViewModel() {
    private val _userplants = MutableLiveData<List<UserPlantsResponseItem>>()
    val userplants: LiveData<List<UserPlantsResponseItem>> = _userplants

    private val _isPlantsEmpty = MutableLiveData<Boolean>()
    val isPlantsEmpty: LiveData<Boolean> = _isPlantsEmpty

    private val _userPlant = MutableLiveData<UserPlantsResponseItem>()
    val userPlant: LiveData<UserPlantsResponseItem> = _userPlant

    private val _weather = MutableLiveData<WeatherResponse>()
    val weather: LiveData<WeatherResponse> = _weather

    private val _sessions = MutableLiveData<List<SessionResponseItem>>()
    val sessions: LiveData<List<SessionResponseItem>> = _sessions

    private val _session = MutableLiveData<SessionResponseItem>()
    val session: LiveData<SessionResponseItem> = _session

    private val _plant = MutableLiveData<PlantResponseItem>()
    val plant: LiveData<PlantResponseItem> = _plant

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

    fun getPlant(token: String, id: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getPlantById("Bearer $token", id)
        client.enqueue(object : Callback<PlantResponse> {
            override fun onResponse(call: Call<PlantResponse>, response: Response<PlantResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    //Log.e("fafifu", response.body().toString())
                    _plant.value = response.body()?.response
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

    fun getSessions(token: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getSessions("Bearer $token")
        client.enqueue(object : Callback<SessionsResponse> {
            override fun onResponse(call: Call<SessionsResponse>, response: Response<SessionsResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    //Log.e("fafifu", response.body().toString())
                    _sessions.value = response.body()?.response
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<SessionsResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun getSession(id: String, token: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getSession("Bearer $token", id)
        client.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(call: Call<SessionResponse>, response: Response<SessionResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    //Log.e("fafifu", response.body().toString())
                    _session.value = response.body()?.response
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun updateSession(token: String, map : HashMap<String, Any>, id: String){
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).updateSession("Bearer $token", id, map)
        client.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(
                call: Call<SessionResponse>,
                response: Response<SessionResponse>
            ) {
                if (response.isSuccessful){
                    _state.value = true
                }
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }

        })
    }

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
        client.enqueue(object : Callback<UserPlantsResponse> {
            override fun onResponse(call: Call<UserPlantsResponse>, response: Response<UserPlantsResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    if(!response.body()?.response.isNullOrEmpty()){
                        _userplants.value = response.body()?.response
                        _isPlantsEmpty.value = false
                    }else{
                        _isPlantsEmpty.value = true
                    }
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<UserPlantsResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun updateUserPlants(token: String, map : HashMap<String, Any>, id: String){
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).updateUserPlants("Bearer $token", id, map)
        client.enqueue(object : Callback<UserPlantResponse> {
            override fun onResponse(
                call: Call<UserPlantResponse>,
                response: Response<UserPlantResponse>
            ) {
                if (response.isSuccessful){
                    _state.value = true
                }
            }

            override fun onFailure(call: Call<UserPlantResponse>, t: Throwable) {
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }

        })
    }

    fun getUserPlant(token: String, id: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getUserPlant("Bearer $token", id)
        client.enqueue(object : Callback<UserPlantResponse> {
            override fun onResponse(call: Call<UserPlantResponse>, response: Response<UserPlantResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    //Log.e("fafifu", response.body().toString())
                    _userPlant.value = response.body()?.response
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
        private const val TAG = "DashViewModel"
    }

}