package com.irfan.nanamyuk.ui.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.irfan.nanamyuk.R
import com.irfan.nanamyuk.databinding.FragmentAddBinding
import com.irfan.nanamyuk.ui.form.FormActivity
import com.irfan.nanamyuk.ui.pilih.PilihActivity

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickListener = View.OnClickListener { v ->
            when (v.id) {
                R.id.rbRecom -> {
                    val intent = Intent(activity, FormActivity::class.java)
                    startActivity(intent)
                }
                R.id.rbChoice -> {
                    val intent = Intent(activity, PilihActivity::class.java)
                    intent.putExtra("method", "pilih")
                    startActivity(intent)
                }
            }
        }

        binding.rbRecom.setOnClickListener(onClickListener)
        binding.rbChoice.setOnClickListener(onClickListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}