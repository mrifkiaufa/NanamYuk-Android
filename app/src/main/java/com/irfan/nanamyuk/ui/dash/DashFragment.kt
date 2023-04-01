package com.irfan.nanamyuk.ui.dash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.irfan.nanamyuk.HomeActivity
import com.irfan.nanamyuk.adapter.MoveDryAdapter
import com.irfan.nanamyuk.adapter.MoveHumidAdapter
import com.irfan.nanamyuk.adapter.UserPlantsAdapter
import com.irfan.nanamyuk.data.api.UserPlantsResponseItem
import com.irfan.nanamyuk.data.datastore.SessionPreferences
import com.irfan.nanamyuk.databinding.FragmentDashboardBinding
import com.irfan.nanamyuk.ui.ViewModelFactory
import com.irfan.nanamyuk.ui.pilih.PilihActivity
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

    private val binding get() = _binding!!

    private val dateNow = now().toDateStandardConcise()

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
                //Log.e("TEMP1", weather.toString())
            }
            Log.e("TEMP2", data.toString())
            if (data.temperature.isNotEmpty()) {
                temp = data.temperature.toDouble()
            }

        }

        binding.btnPremium.visibility = GONE
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
            val needMoveDry = mutableListOf<UserPlantsResponseItem>()
            val needMoveHumid = mutableListOf<UserPlantsResponseItem>()
            val finish = mutableListOf<UserPlantsResponseItem>()

            Log.e("DATE NEXT dateNow", now().toString())

            if(!UserPlants.isNullOrEmpty()) {
                for (x in UserPlants) {
                    dashViewModel.getUserPlant(token, x.id)

                    val dateUserPlant = formatDate(x.wateringDate, "dd MMM yyyy")
                    Log.e("plant date", dateUserPlant)
                    Log.e("now date", dateNow)
                    Log.e("now date", (dateNow >= dateUserPlant).toString())
                    if (dateNow >= dateUserPlant) {
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "watering_date" to x.wateringDate,
                            "move_date" to x.moveDate,
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to true,
                            "dry_state" to x.dryState,
                            "humid_state" to x.humidState
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                    } else {
                        val userPlantMap: HashMap<String, Any> = hashMapOf(
                            "watering_date" to x.wateringDate,
                            "move_date" to x.moveDate,
                            "tag_name" to x.namaPenanda,
                            "user_id" to x.user.id,
                            "plant_id" to x.plant.id,
                            "watering_state" to false,
                            "dry_state" to x.dryState,
                            "humid_state" to x.humidState
                        )

                        dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                    }

                    dashViewModel.getPlant(token, x.plant.id)
                    dashViewModel.plant.observe(viewLifecycleOwner) { plant ->
                        if(plant.id == x.plant.id) {
                            val minTemp = x.plant.minTemp.toDouble()
                            val maxTemp = x.plant.maxTemp.toDouble()

                            val dateSession = formatDate(x.moveDate, "dd MMM yyyy")
                            if(temp in minTemp..maxTemp || dateNow < dateSession) {
                                val userPlantMap: HashMap<String, Any> = hashMapOf(
                                    "watering_date" to x.wateringDate,
                                    "move_date" to x.moveDate,
                                    "tag_name" to x.namaPenanda,
                                    "user_id" to x.user.id,
                                    "plant_id" to x.plant.id,
                                    "watering_state" to x.wateringState,
                                    "dry_state" to false,
                                    "humid_state" to false
                                )

                                dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                            }
                             else {
                                if(temp > maxTemp) {
                                    val userPlantMap: HashMap<String, Any> = hashMapOf(
                                        "watering_date" to x.wateringDate,
                                        "move_date" to x.moveDate,
                                        "tag_name" to x.namaPenanda,
                                        "user_id" to x.user.id,
                                        "plant_id" to x.plant.id,
                                        "watering_state" to x.wateringState,
                                        "dry_state" to false,
                                        "humid_state" to true
                                    )

                                    dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                                } else if (temp < minTemp) {
                                    val userPlantMap: HashMap<String, Any> = hashMapOf(
                                        "watering_date" to x.wateringDate,
                                        "move_date" to x.moveDate,
                                        "tag_name" to x.namaPenanda,
                                        "user_id" to x.user.id,
                                        "plant_id" to x.plant.id,
                                        "watering_state" to x.wateringState,
                                        "dry_state" to true,
                                        "humid_state" to false
                                    )

                                    dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                                }
                            }
                        }
                    }
                }

                for (i in UserPlants) {
                    if(i.wateringState) {
                        notFinish.add(i)
                    } else if (i.dryState && !i.humidState) {
                        needMoveDry.add(i)
                    } else if (i.humidState && !i.dryState) {
                        needMoveHumid.add(i)
                    } else {
                        finish.add(i)
                    }
                }
            }

            val adapterNotFinish = UserPlantsAdapter(notFinish)
            val adapterNeedMoveDry = MoveDryAdapter(needMoveDry)
            val adapterNeedMoveHumid = MoveHumidAdapter(needMoveHumid)
            val adapterFinish = UserPlantsAdapter(finish)

            if (notFinish.isNotEmpty()) {
                binding.animationView4.visibility = GONE
            }
            if (needMoveDry.isNotEmpty() || needMoveHumid.isNotEmpty()) {
                binding.animationView2.visibility = GONE
            }
            if (needMoveDry.isEmpty() && needMoveHumid.isEmpty()) {
                binding.rvNeedMoveHumid.visibility = GONE
            }
            if (finish.isNotEmpty()) {
                binding.animationView1.visibility = GONE
            }
            if (UserPlants.isNullOrEmpty()){
                binding.animationView3.visibility = View.VISIBLE
//                binding.tvAddPlantHome.visibility = View.VISIBLE
                binding.addButtonHome.visibility = View.VISIBLE
                binding.tvNoPlant.visibility = View.VISIBLE
                binding.rvNeedMoveHumid.visibility = GONE
                binding.tvStatusNo.visibility = GONE
                binding.tvStatusMove.visibility = GONE
                binding.tvStatusYes.visibility = GONE
                binding.animationView2.visibility = GONE
                binding.animationView1.visibility = GONE
                binding.animationView4.visibility = GONE

                binding.addButtonHome.setOnClickListener {
                    val intent = Intent(activity, PilihActivity::class.java)
                    intent.putExtra("method", "pilih")
                    startActivity(intent)
                }
            }

            if (UserPlants.isNotEmpty()) {
                binding.rvNotFinish.layoutManager = LinearLayoutManager(activity)
                binding.rvNotFinish.adapter = adapterNotFinish

                binding.rvNeedMoveDry.layoutManager = LinearLayoutManager(activity)
                binding.rvNeedMoveDry.adapter = adapterNeedMoveDry

                binding.rvNeedMoveHumid.layoutManager = LinearLayoutManager(activity)
                binding.rvNeedMoveHumid.adapter = adapterNeedMoveHumid

                binding.rvFinish.layoutManager = LinearLayoutManager(activity)
                binding.rvFinish.adapter = adapterFinish
            }

            adapterFinish.notifyDataSetChanged()
            adapterNeedMoveDry.notifyDataSetChanged()
            adapterNeedMoveHumid.notifyDataSetChanged()
            adapterNotFinish.notifyDataSetChanged()

            adapterNeedMoveDry.setOnItemClickLitener(object: MoveDryAdapter.OnItemClickListener {
                override fun onItemClickMoving(
                    view: View,
                    position: Int,
                    userPlantID: String
                ) {
                    dashViewModel.getUserPlants(token)
                    dashViewModel.userplants.observe(viewLifecycleOwner) { userPlants ->
                        for (x in userPlants) {
                            if (x.id == userPlantID) {
                                val userPlantMap: HashMap<String, Any> = hashMapOf(
                                    "watering_date" to x.wateringDate,
                                    "move_date" to setTanggal(),
                                    "tag_name" to x.namaPenanda,
                                    "user_id" to x.user.id,
                                    "plant_id" to x.plant.id,
                                    "watering_state" to x.wateringState,
                                    "dry_state" to false,
                                    "humid_state" to x.humidState
                                )

                                dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                            }
                        }
                    }
                    val i = Intent(activity, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }
            })

            adapterNeedMoveHumid.setOnItemClickLitener(object: MoveHumidAdapter.OnItemClickListener {
                override fun onItemClickMoving(
                    view: View,
                    position: Int,
                    userPlantID: String
                ) {
                    dashViewModel.getUserPlants(token)
                    dashViewModel.userplants.observe(viewLifecycleOwner) { userPlants ->
                        for (x in userPlants) {
                            if (x.id == userPlantID) {
                                val userPlantMap: HashMap<String, Any> = hashMapOf(
                                    "watering_date" to x.wateringDate,
                                    "move_date" to setTanggal(),
                                    "tag_name" to x.namaPenanda,
                                    "user_id" to x.user.id,
                                    "plant_id" to x.plant.id,
                                    "watering_state" to x.wateringState,
                                    "dry_state" to x.dryState,
                                    "humid_state" to false
                                )

                                dashViewModel.updateUserPlants(token, userPlantMap, x.id)
                            }
                        }
                    }
                    val i = Intent(activity, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }
            })

            adapterNotFinish.setOnItemClickLitener(object : UserPlantsAdapter.OnItemClickListener {
                override fun onItemClickWatering(
                    view: View,
                    position: Int,
                    id: String,
                    date: String,
                    duration: String
                ) {

                    dashViewModel.getUserPlants(token)
                    dashViewModel.userplants.observe(viewLifecycleOwner) { userPlants ->
                        for(x in userPlants) {
                            if (x.id == id) {
                                val userPlantMap: HashMap<String, Any> = hashMapOf(
                                    "watering_date" to setTanggal(duration),
                                    "move_date" to x.moveDate,
                                    "tag_name" to x.namaPenanda,
                                    "user_id" to x.user.id,
                                    "plant_id" to x.plant.id,
                                    "watering_state" to false,
                                    "dry_state" to x.dryState,
                                    "humid_state" to x.humidState
                                )

                                dashViewModel.updateUserPlants(token, userPlantMap, id)
                            }
                        }
                    }

                    val i = Intent(activity, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                }

            })
        }
        dashViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progress.visibility = if (isLoading) View.VISIBLE else GONE
        }
    }

    private fun setTanggal(duration: String = " "): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val currentDate = Date()
        val date = dateFormat.format(currentDate)

        val timeZone = TimeZone.of("UTC")
        val d = LocalDateTime.parse(date.replace("Z", ""))
        val instant = d.toInstant(timeZone)

        var add = 1

        if(duration != " ") {
            val splitDuration = duration.split("x")
            add = splitDuration[1].toInt()
        }

        val instantOneDayLater = instant.plus(add, DateTimeUnit.DAY, timeZone)
        val next = instantOneDayLater.toLocalDateTime(timeZone)

        Log.e("DATE NEXT", next.toString() + "Z")

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