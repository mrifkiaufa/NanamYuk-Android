package com.irfan.nanamyuk.ui.daftar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irfan.nanamyuk.data.api.AuthResponse
import com.irfan.nanamyuk.data.api.ConfigApi
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.irfan.nanamyuk.data.api.ConfigApi.Companion.BASE_URL

class DaftarViewModel(private val pref: SessionPreferences) : ViewModel() {
    private val _login = MutableLiveData<AuthResponse>()
    val login : LiveData<AuthResponse> = _login

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _state = MutableLiveData<Boolean>()
    val state: LiveData<Boolean> = _state

    fun postDaftar(map : HashMap<String, String>, message: String = "") {
        _isLoading.value = true
        _state.value = false

        val client = ConfigApi.getApiService(BASE_URL).postRegister(map)
        client.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _state.value = true

                    viewModelScope.launch {
                        val token = response.body()?.token
                        val name = response.body()?.user?.name
                        val id = response.body()?.userId

                        if (token != null && name != null && id != null) {
                            pref.login(token, name, id)
                        }
                    }
                } else {
                    _state.value = false
                    if (message != "") {
                        _message.value = message
                    } else {
                        _message.value = "Email sudah digunakan, silakan coba email lain."
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _state.value = false
                _message.value = t.message
            }
        })
    }
}