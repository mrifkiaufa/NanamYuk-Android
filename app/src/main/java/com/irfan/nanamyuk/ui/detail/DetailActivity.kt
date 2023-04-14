package com.irfan.nanamyuk.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.irfan.nanamyuk.HomeActivity
import com.irfan.nanamyuk.R
import com.irfan.nanamyuk.adapter.UserPlantsAdapter.Companion.ID
import com.irfan.nanamyuk.adapter.UserPlantsAdapter.Companion.NAME
import com.irfan.nanamyuk.adapter.UserPlantsAdapter.Companion.UID
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.databinding.ActivityDetailBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.login.LoginActivity


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailBinding
    private lateinit var detailViewModel: DetailViewModel
    private var token = ""
    private var sessionId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.extras?.get(ID).toString()
        val name = intent.extras?.get(NAME).toString()
        val uid = intent.extras?.get(UID).toString()

        Log.e("tes intent", id)

        setupViewModel()
        setupSession(uid)
        setupAction(id, name, uid)
    }

    private fun setupSession(id: String) {
        detailViewModel.getSessions(token)
        detailViewModel.sessions.observe(this) { sessions ->
            for (x in sessions) {
                if (x.userPlantID == id) {
                    sessionId = x.id
                }
            }
        }
    }

    private fun setupViewModel() {
        detailViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(dataStore)))[DetailViewModel::class.java]
    }

    private fun setupAction(id : String, name : String, uid: String) {
        detailViewModel.getUserToken().observe(this) {
            detailViewModel.getPlant(it.token, id)
            token = it.token
        }

        detailViewModel.plant.observe(this){ plant ->
            Glide.with(this).load(plant.image).into(binding.imagePlant)
            binding.apply {
                plantTag.text = name
                plantName.text = plant.namaTanaman
                textTemperature.text = plant.suhu
                textKelembapan.text = plant.kelembapan
                textRainfall.text = plant.rainfall
                textTanah.text = plant.tanah
                textCahaya.text = plant.cahaya
                textDurasi.text = plant.durasiSiram
                textDeskripsi.text = plant.deskripsi
                textTutorial.text = plant.tutorial
            }
        }

        binding.delete.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.delete_tittle))
                .setMessage(resources.getString(R.string.delete_supporting_text))
                .setPositiveButton(resources.getString(R.string.iya)) { _, _ ->
                    detailViewModel.deleteSession(token, sessionId)
                    detailViewModel.deleteUserPlants(token, uid)

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(this, "Berhasil menghapus tanaman", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(resources.getString(R.string.batal)) { _, _ ->
                    Toast.makeText(this, "Batal menghapus tanaman", Toast.LENGTH_SHORT).show()
                }
                .show()
            }
        }
}
