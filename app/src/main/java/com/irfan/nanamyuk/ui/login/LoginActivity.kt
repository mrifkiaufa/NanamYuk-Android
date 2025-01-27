package com.irfan.nanamyuk.ui.login

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
import com.irfan.nanamyuk.databinding.ActivityLoginBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.daftar.DaftarActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDaftar.setOnClickListener {
            val intent = Intent(this, DaftarActivity::class.java)
            startActivity(intent)
        }

        loginViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(dataStore)))[LoginViewModel::class.java]

        setupAction()
    }

    private fun setupAction(){
        loginViewModel.getUserToken().observe(this) {
            if(it.token.isNotEmpty()) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                finish()
                startActivity(intent)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            val map = hashMapOf(
                "email" to email,
                "password" to password
            )

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Lengkapi form terlebih dahulu!", Toast.LENGTH_SHORT).show()
            } else if (!(Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
                Toast.makeText(this, "Format email tidak sesuai", Toast.LENGTH_SHORT).show()
            } else {
                if (email.isBlank()) {
                    Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }else if (password.isBlank()) {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }else {
                    loginViewModel.postLogin(map)
                }
            }

            loginViewModel.state.observe(this) {
                if (it) {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()

                    Toast.makeText(this, "Anda berhasil login", Toast.LENGTH_SHORT).show()
                } else {
                    loginViewModel.message.observe(this) { message ->
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loginViewModel.isLoading.observe(this, ::showLoading)
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