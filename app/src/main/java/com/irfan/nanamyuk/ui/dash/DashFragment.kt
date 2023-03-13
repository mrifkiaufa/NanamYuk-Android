package com.irfan.nanamyuk.ui.dash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.irfan.nanamyuk.HomeActivity
import com.irfan.nanamyuk.adapter.UserPlantsAdapter
import com.irfan.nanamyuk.data.api.UserPlantsResponseItem
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.databinding.FragmentDashboardBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.subscription.SubscriptionActivity
import com.parassidhu.simpledate.toDateStandardConcise
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import me.moallemi.tools.extension.date.now
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DashFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var dashViewModel: DashViewModel
    private var token = ""
    private var temp = 0.0

    private var minPlantTemp = 0.0
    private var maxPlantTemp = 0.0

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

        dashViewModel.getUserToken().observe(viewLifecycleOwner) { data ->
            binding.tvHalo.text = "Halo, " + data.name
            dashViewModel.getUserPlants(data.token)
            token = data.token

            dashViewModel.getWeather(data.lat, data.lon, APP_ID)

            dashViewModel.weather.observe(viewLifecycleOwner) { weather ->
                var temp = kelvinToCelcius(weather.main.temp)
                temp = (temp * 100.0).roundToInt() / 100.0
                dashViewModel.setTemperature(temp.toString())
            }

            temp = data.temperature.toDouble()

        }

        binding.btnPremium.setOnClickListener {
            val i = Intent(activity, SubscriptionActivity::class.java)
            startActivity(i)
        }

        setupAction()

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        dashViewModel.getUserToken().observe(viewLifecycleOwner) {
            binding.tvHalo.text = "Halo, " + it.name
            dashViewModel.getUserPlants(it.token)
            token = it.token
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat", "NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupAction() {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

        binding.tvDate.text = date.format(calendar.time)

        dashViewModel.userplants.observe(viewLifecycleOwner) { UserPlants ->
            val notFinish = mutableListOf<UserPlantsResponseItem>()
            val finish = mutableListOf<UserPlantsResponseItem>()

            if(!UserPlants.isNullOrEmpty()) {
                for (x in UserPlants) {
                    val dateNow = now().toDateStandardConcise()
                    Log.e("date now", dateNow)

                    dashViewModel.getPlant(token, x.plant.id)
                    dashViewModel.plant.observe(viewLifecycleOwner) { plant ->
                        val plantTemps = plant.suhu.split("-")

                        minPlantTemp = plantTemps[0].toDouble()
                        maxPlantTemp = plantTemps[1].toDouble()
                    }

//                    Log.e("Plant Temperature: ", minPlantTemp.toString())
//                    Log.e("Plant Temperature: ", maxPlantTemp.toString())
//                    Log.e("Temperature: ", temp.toString())

                    if(temp > maxPlantTemp) {
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "date" to x.date,
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to x.wateringState,
                            "dry_state" to x.dryState,
                            "humid_state" to true
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                    } else if (temp < minPlantTemp) {
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "date" to x.date,
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to x.wateringState,
                            "dry_state" to true,
                            "humid_state" to x.humidState
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                    }

                    val dateUserPlant = formatDate(x.date, "dd MMM yyyy")
                    Log.e("plant date", dateUserPlant)
                    if (dateNow == dateUserPlant && x.wateringState) {
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "date" to x.date,
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to false,
                            "dry_state" to x.dryState,
                            "humid_state" to x.humidState
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                    }
                }

                for (i in UserPlants) {
                    if (i.wateringState) {
                        finish.add(i)
                    } else {
                        notFinish.add(i)
                    }
                }
            }

            val adapterNotFinish = UserPlantsAdapter(notFinish)
            val adapterFinish = UserPlantsAdapter(finish)

            if (notFinish.isNotEmpty()) {
                binding.animationView2.visibility = View.GONE
            }
            if (finish.isNotEmpty()) {
                binding.animationView1.visibility = View.GONE
            }
            if (finish.isEmpty() && notFinish.isEmpty()){
                binding.animationView3.visibility = View.VISIBLE
                binding.tvNoPlant.visibility = View.VISIBLE
                binding.tvStatusNo.visibility = View.GONE
                binding.tvStatusYes.visibility = View.GONE
                binding.animationView2.visibility = View.GONE
                binding.animationView1.visibility = View.GONE
            }

            if (UserPlants.isNotEmpty()) {
                binding.rvNotFinish.layoutManager = LinearLayoutManager(activity)
                binding.rvNotFinish.adapter = adapterNotFinish

                binding.rvFinish.layoutManager = LinearLayoutManager(activity)
                binding.rvFinish.adapter = adapterFinish
            }

            adapterFinish.notifyDataSetChanged()
            adapterNotFinish.notifyDataSetChanged()

            adapterNotFinish.setOnItemClickLitener(object : UserPlantsAdapter.OnItemClickListener {
                override fun onItemClick(
                    view: View,
                    position: Int,
                    id: String,
                    date: String,
                    duration: String
                ) {
                    dashViewModel.getUserPlant(token, id)
                    dashViewModel.userPlant.observe(viewLifecycleOwner) { x ->
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "date" to setTanggal(date, duration),
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to true,
                            "dry_state" to x.dryState,
                            "humid_state" to x.humidState
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, id)
                    }

                    val i = Intent(activity, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }

            })
        }
        dashViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setTanggal(date: String, duration: String): String {
        val timeZone = TimeZone.of("UTC")
        val d = LocalDateTime.parse(date.replace("Z", ""))
        val instant = d.toInstant(timeZone)

        var add = 1

        if (duration == "1x7") {
            add = 7
        } else if (duration == "1x14") {
            add = 14
        }

        val instantOneDayLater = instant.plus(add, DateTimeUnit.DAY, timeZone)
        val next = instantOneDayLater.toLocalDateTime(timeZone)

        return next.toString() + "Z"
    }

    private fun formatDate(currentDateString: String, pattern: String): String {
        val instant = Instant.parse(currentDateString)
        val formatter = DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneId.of("UTC"))
        return formatter.format(instant)
    }

    private fun kelvinToCelcius(kelvinTemp: Double): Double = kelvinTemp - 273.15

    private fun setupViewModel(){
        dashViewModel = ViewModelProvider(this, ViewModelFactory(SessionPreferences.getInstance(requireContext().dataStore)))[DashViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val APP_ID = "6259b6ecf75fd6424dd3351f6db69f2a"
    }
}