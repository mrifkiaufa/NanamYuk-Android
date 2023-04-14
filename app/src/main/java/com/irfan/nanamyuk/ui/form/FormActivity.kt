package com.irfan.nanamyuk.ui.form

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.irfan.nanamyuk.R
import com.irfan.nanamyuk.adapter.TanahAdapter
import com.irfan.nanamyuk.data.Tanah
import com.irfan.nanamyuk.ui.pilih.PilihActivity
import com.irfan.nanamyuk.databinding.ActivityFormBinding

class FormActivity : AppCompatActivity() {

    private lateinit var jenisTanah: RecyclerView
    private val list = ArrayList<Tanah>()
    private lateinit var binding: ActivityFormBinding

    private var namaKota = ""
    private var intensitasCahaya = ""
    private var namaTanah = ""
    private var idTanah = ""

    private var addMethodCahayaID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jenisTanah = findViewById(R.id.jenis_tanah)
        jenisTanah.setHasFixedSize(true)

        list.addAll(listTanah)

        jenisTanah.layoutManager = LinearLayoutManager(this)
        val singleAdapter = TanahAdapter(this, list)
        jenisTanah.adapter = singleAdapter

        singleAdapter.setOnItemClickLitener(object : TanahAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                singleAdapter.setSelection(position)

                namaTanah = singleAdapter.SingleViewHolder(view).mNamaTanah.text.toString()

                when(namaTanah) {
                    "Tanah berpasir" -> {
                        idTanah = "1"
                    }"Tanah lempung" -> {
                        idTanah = "2"
                    }"Tanah liat" -> {
                        idTanah = "3"
                    }
                }
            }

        })

        binding.nextButton.setOnClickListener {
            namaKota = binding.editTextKota.text.toString()

            pilihIntensitas()
            if(namaTanah.isNotEmpty() and namaKota.isNotBlank() and intensitasCahaya.isNotEmpty()) {
                if (isNotLetters(namaKota)) {
                    Toast.makeText(this, "Nama kota tidak boleh mengandung angka dan tanda baca", Toast.LENGTH_SHORT).show()
                } else if (namaKota.length < 3) {
                    Toast.makeText(this, "Kota tidak ditemukan :(", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, PilihActivity::class.java)
                    intent.putExtra("method", "rekomendasi")
                    intent.putExtra("kota", namaKota)
                    intent.putExtra("intensitas", intensitasCahaya)
                    intent.putExtra("idTanah", idTanah)
                    startActivity(intent)
                }
            } else if(namaKota.isBlank()){
                Toast.makeText(this@FormActivity, "Silakan isi kota terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if(intensitasCahaya.isEmpty()){
                Toast.makeText(this@FormActivity, "Silakan pilih intensitas cahaya terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if(namaTanah.isEmpty()){
                Toast.makeText(this@FormActivity, "Silakan pilih tanah terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pilihIntensitas() {
        val addMethodCahaya = binding.rgCahaya.checkedRadioButtonId

        if(addMethodCahaya != -1) {
            addMethodCahayaID = resources.getResourceEntryName(addMethodCahaya)
        }

        when(addMethodCahayaID){
            "rb_langsung" -> {
                intensitasCahaya = "1"
            }"rb_tidak_langsung" -> {
                intensitasCahaya = "2"
            }
        }
    }

    private fun isNotLetters(string: String): Boolean {
        val regex = Regex("[^A-Za-z ]|[0-9]")
        return regex.containsMatchIn(string)
    }

    private val listTanah: ArrayList<Tanah>
        get() {
            val dataNama = resources.getStringArray(R.array.data_nama_tanah)
            val dataDeskripsi = resources.getStringArray(R.array.data_deskripsi_tanah)
            val dataUrlPhoto = resources.getStringArray(R.array.data_urlPhoto)

            val list = ArrayList<Tanah>()
            for (i in dataNama.indices) {
                Log.e("nnyuk", dataUrlPhoto.get(i))
                val tanah = Tanah(dataNama[i],dataDeskripsi[i], dataUrlPhoto[i])
                list.add(tanah)
            }
            return list
        }
}