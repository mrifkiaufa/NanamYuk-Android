package com.irfan.nanamyuk.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.irfan.nanamyuk.data.api.UserPlantsResponseItem
import com.irfan.nanamyuk.databinding.ItemStatusBinding
import com.irfan.nanamyuk.ui.detail.DetailActivity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UserPlantsAdapter(private val datas: List<UserPlantsResponseItem>) :
    RecyclerView.Adapter<UserPlantsAdapter.ViewHolder>() {

    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val UID = "uid"
    }

    private var onClick: OnItemClickListener? = null

    fun setOnItemClickLitener(mOnItemClickListener: OnItemClickListener) {
        this.onClick = mOnItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStatusBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (namaPenanda, plant, date, wateringState, dryState, humidState, id) = datas[position]

        if (wateringState) {
            holder.binding.fabWater.visibility = View.GONE
            holder.binding.check.visibility = View.VISIBLE
        }

        if (dryState) {
            holder.binding.fabSun.visibility = View.VISIBLE
            holder.binding.fabCloud.visibility = View.GONE
        }

        if (humidState) {
            holder.binding.fabCloud.visibility = View.VISIBLE
            holder.binding.fabCloud.visibility = View.GONE
        }

        Glide.with(holder.itemView).load(plant.image).into(holder.binding.circleImageView)
        holder.binding.tvPenanda.text = namaPenanda
        holder.binding.tvTanaman.text = plant.namaTanaman

        holder.binding.tvDate.text = formatDate(date)


        Log.e("Plant data", plant.toString())
        holder.binding.card.setOnClickListener {
            val i = Intent(holder.itemView.context, DetailActivity::class.java)
            i.putExtra(ID, plant.id)
            i.putExtra(NAME, namaPenanda)
            i.putExtra(UID, id)
            holder.itemView.context.startActivity(i)
        }

        if (onClick != null) {
            holder.binding.fabWater.setOnClickListener {
                onClick!!.onItemClick(
                    holder.binding.fabWater,
                    holder.adapterPosition,
                    id,
                    date,
                    plant.durasiSiram
                )
            }
        }
    }

    private fun formatDate(currentDateString: String): String {
        val instant = Instant.parse(currentDateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.of("UTC"))
        return formatter.format(instant)
    }


    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int, id:String, date: String, duration: String)
    }

    override fun getItemCount(): Int = datas.size

    inner class ViewHolder(var binding: ItemStatusBinding) : RecyclerView.ViewHolder(binding.root)
}
