package com.irfan.nanamyuk.ui.daftar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.irfan.nanamyuk.HomeActivity
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.databinding.ActivityDaftarBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.login.LoginActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DaftarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDaftarBinding
    private lateinit var daftarViewModel: DaftarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDaftar.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        daftarViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(dataStore)))[DaftarViewModel::class.java]


        setupAction()
    }

    private fun setupAction(){
        binding.btnDaftar.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            val map = hashMapOf(
                "email" to email,
                "password" to password,
                "name" to name,
            )

            if (name.isEmpty() && email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Lengkapi form terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else {
                if (name == "") {
                    daftarViewModel.postDaftar(map, "Nama tidak boleh kosong")
                }else if (email == "") {
                    daftarViewModel.postDaftar(map, "Email tidak boleh kosong")
                }else if (password == "") {
                    daftarViewModel.postDaftar(map, "Password tidak boleh kosong")
                }else if (password.length < 6) {
                    daftarViewModel.postDaftar(map, "Password tidak boleh kurang dari 6 karakter")
                }else if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                    daftarViewModel.postDaftar(map, "Format email tidak sesuai")
                } else {
                    daftarViewModel.postDaftar(map)
                }
            }
            daftarViewModel.state.observe(this) {
                if (it) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()

                    Toast.makeText(this, "Berhasil mendaftar akun!", Toast.LENGTH_SHORT).show()
                } else {
                    daftarViewModel.message.observe(this) { message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        daftarViewModel.isLoading.observe(this, ::showLoading)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progress.visibility = if (isLoading) View.VISIBLE else View.GONE
            card.visibility = if (!isLoading) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        currentFocus?.let {
            val closeKeyboard: InputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as (InputMethodManager)
            closeKeyboard.hideSoftInputFromWindow(it.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}