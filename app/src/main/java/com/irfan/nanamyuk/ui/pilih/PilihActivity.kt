package com.irfan.nanamyuk.ui.pilih


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.irfan.nanamyuk.HomeActivity
import com.irfan.nanamyuk.R
import com.irfan.nanamyuk.adapter.PilihAdapter
import com.irfan.nanamyuk.data.api.PlantResponseItem
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.databinding.ActivityPilihBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.dash.DashViewModel
import com.parassidhu.simpledate.toZuluFormat
import me.moallemi.tools.extension.date.now

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PilihActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPilihBinding
    private lateinit var pilihViewModel: PilihViewModel
    private lateinit var dashViewModel: DashViewModel

    private lateinit var adapter: PilihAdapter
    private var tanamanId = ""
    private var token = ""
    private var namaPenanda = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPilihBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val method = intent.extras?.get(METHOD).toString()

        setupViewModel()
        setupAction(method)
    }


    @SuppressLint("NewApi")
    private fun setupAction(method: String) {
        val layoutManager = LinearLayoutManager(this)
        binding.rvPlant.layoutManager = layoutManager
      
        pilihViewModel.getUserToken().observe(this) {
            token = it.token
            pilihViewModel.getPlants(it.token)
        }

        pilihViewModel.plants.observe(this) { plants ->
            when(method) {
                "rekomendasi" -> {
                    binding.tvTambah.text = "Hasil Rekomendasi"
                    binding.tvRekom.visibility = View.VISIBLE
                    val tanah = intent.extras?.get("idTanah").toString()
                    val intensitas = intent.extras?.get("intensitas").toString()
                    val kota = intent.extras?.get("kota").toString()

                    pilihViewModel.getRecom(token, tanah, intensitas, kota)
                    pilihViewModel.recoms.observe(this) { recomPlants ->
                        if (recomPlants.response == 404) {
                            Toast.makeText(this, "Kota tidak ditemukan :(", Toast.LENGTH_SHORT).show()
                            finish()
                        } else if(recomPlants.response == 200) {
                            val recoms = mutableListOf<PlantResponseItem>()

                            for (x in plants) {
                                if (x.namaTanaman == recomPlants.plant1){
                                    recoms.add(x)
                                }
                            }
                            for (x in plants) {
                                if (x.namaTanaman == recomPlants.plant2){
                                    recoms.add(x)
                                }
                            }
                            for (x in plants) {
                                if (x.namaTanaman == recomPlants.plant3){
                                    recoms.add(x)
                                }
                            }
                            for (x in plants) {
                                if (x.namaTanaman == recomPlants.plant4){
                                    recoms.add(x)
                                }
                            }
                            for (x in plants) {
                                if (x.namaTanaman == recomPlants.plant5){
                                    recoms.add(x)
                                }
                            }

                            Log.e("fafu", recoms.toString())

                            adapter = PilihAdapter(recoms)

                            binding.rvPlant.adapter = adapter

                            adapter.setOnItemClickLitener(object : PilihAdapter.OnItemClickListener {
                                override fun onItemClick(view: View, position: Int) {

                                    tanamanId = adapter.SingleViewHolder(view).mTvId.text.toString()

                                    adapter.setSelection(position)
                                }
                            })
                        }
                    }
                }
                "pilih" -> {
                    adapter = PilihAdapter(plants)

                    binding.rvPlant.adapter = adapter

                    adapter.setOnItemClickLitener(object : PilihAdapter.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {

                            tanamanId = adapter.SingleViewHolder(view).mTvId.text.toString()

                            adapter.setSelection(position)
                        }
                    })
                }
            }
        }

        pilihViewModel.isLoading.observe(this) { isLoading ->
            binding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.progressText.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.nextButton.setOnClickListener {
            pilihViewModel.getUserToken().observe(this) {

                namaPenanda = binding.tvPenanda.text.toString()
                val user = it.id
                val plantId = tanamanId
                val wateringState = true
                val dryState = false
                val humidState = false

                val userPlantMap: HashMap<String, Any> = hashMapOf(
                    "watering_date" to setTanggal(),
                    "move_date" to setTanggal(),
                    "tag_name" to namaPenanda,
                    "user_id" to user,
                    "plant_id" to plantId,
                    "watering_state" to wateringState,
                    "dry_state" to dryState,
                    "humid_state" to humidState
                )


                if (namaPenanda.isNotEmpty() && tanamanId.isNotEmpty() && namaPenanda.isNotEmpty()){
                    if (namaPenanda.isBlank()) {
                        Toast.makeText(this, "Nama penanda tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    } else if (checkPunctuation(namaPenanda)) {
                        Toast.makeText(this, "Nama penanda tidak boleh mengandung tanda baca", Toast.LENGTH_SHORT).show()
                    }else {
                        //binding.nextButton.visibility  = View.INVISIBLE
                        MaterialAlertDialogBuilder(this)
                            .setTitle(resources.getString(R.string.add_tittle))
                            .setMessage(resources.getString(R.string.add_supporting_text))
                            .setPositiveButton(resources.getString(R.string.iya)) { _, _ ->
                                pilihViewModel.postUserPlants(token, userPlantMap)
                                pilihViewModel.state.observe(this){ state ->
                                    Log.e("nilai state", state.toString())
                                    if (state) {
                                        val intent = Intent(this, HomeActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        Toast.makeText(this, "Berhasil menambah tanaman", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton(resources.getString(R.string.batal)) { _, _ ->
                                Toast.makeText(this, "Batal menambah tanaman", Toast.LENGTH_SHORT).show()
                            }
                            .show()
                    }
                } else if (namaPenanda.isEmpty()) {
                    Toast.makeText(this, "Isi Nama Penanda Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                } else if (tanamanId.isEmpty()) {
                    Toast.makeText(this, "Pilih Tanaman Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun checkPunctuation(string: String): Boolean {
        val regex = Regex("[^A-Za-z0-9 ]")
        return regex.containsMatchIn(string)
    }
    private fun setTanggal(): String {
        return now().toZuluFormat()
    }

    private fun setupViewModel(){
        pilihViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(dataStore)))[PilihViewModel::class.java]
        dashViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(dataStore)))[DashViewModel::class.java]
    }

    companion object {
        const val METHOD = "method"
    }
}