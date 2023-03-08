package com.irfan.nanamyuk.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.irfan.nanamyuk.data.datastore.SessionModel
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import kotlinx.coroutines.launch

class SplashViewModel(private val pref: SessionPreferences): ViewModel() {
    fun getUserToken(): LiveData<SessionModel> {
        return pref.getToken().asLiveData()
    }

    fun setLatLon(lat: String, lon: String) {
        viewModelScope.launch {
            pref.postLatLon(lat, lon)
        }
    }
}