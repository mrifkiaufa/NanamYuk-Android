package com.irfan.nanamyuk.ui.pilih

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.irfan.nanamyuk.data.api.*
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_ML
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_URL
import com.irfan.nanamyuk.data.datastore.SessionModel
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.ui.dash.DashViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PilihViewModel(private val pref: SessionPreferences): ViewModel() {
    private val _plants = MutableLiveData<List<PlantResponseItem>>()
    val plants: LiveData<List<PlantResponseItem>> = _plants

    private val _userPlant = MutableLiveData<UserPlantsResponseItem>()
    val userPlant: LiveData<UserPlantsResponseItem> = _userPlant

    private val _recoms = MutableLiveData<RecomResponse>()
    val recoms: LiveData<RecomResponse> = _recoms

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _state = MutableLiveData<Boolean>()
    val state: LiveData<Boolean> = _state

    fun getUserToken(): LiveData<SessionModel> {
        return pref.getToken().asLiveData()
    }

    fun getPlants(token: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_URL).getPlant("Bearer $token")
        client.enqueue(object : Callback<PlantsResponse> {
            override fun onResponse(call: Call<PlantsResponse>, response: Response<PlantsResponse>) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    //Log.e("fafifu", response.body().toString())
                    _plants.value = response.body()?.response
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<PlantsResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    fun postUserPlants(token: String, map : HashMap<String, Any>){
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).postUserPlants("Bearer $token", map)
        client.enqueue(object : Callback<UserPlantResponse> {
            override fun onResponse(
                call: Call<UserPlantResponse>,
                response: Response<UserPlantResponse>
            ) {
                if (response.isSuccessful){
                    _userPlant.value = response.body()?.response
                    _state.value = true
                    Log.e("userplant value", _userPlant.value.toString())
                }
            }

            override fun onFailure(call: Call<UserPlantResponse>, t: Throwable) {
                _state.value = true
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }

        })
    }

    fun postSession(token: String, map : HashMap<String, Any>){
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).postSession("Bearer $token", map)
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
                _state.value = true
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }

        })
    }

    fun getRecom(token: String, idTanah: String, intensitas: String, kota: String){
        _isLoading.value = true

        val client = ConfigApi.getApiService(BASE_ML).getRecommendation("Bearer $token", idTanah, intensitas, kota)
        client.enqueue(object : Callback<RecomResponse> {
            override fun onResponse(call: Call<RecomResponse>, response: Response<RecomResponse>) {


                if (response.isSuccessful) {
                    _isLoading.value = false
                    _recoms.value = response.body()
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<RecomResponse>, t: Throwable) {
                //_isLoading.value = false
                Log.e(TAG, "onFailure Throw: ${t.message}")
            }
        })
    }

    companion object {
        private const val TAG = "PilihViewModel"
    }
    
}