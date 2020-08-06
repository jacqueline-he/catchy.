package com.example.catchy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.catchy.R
import com.example.catchy.databinding.FragmentBioDialogBinding

class BioDialogFragment : DialogFragment() {
    private var fullName: String? = null
    private var bio: String? = null
    private var imgUrl: String? = null
    private var layout: RelativeLayout? = null
    private var _binding: FragmentBioDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            fullName = requireArguments().getString("fullName")
            bio = requireArguments().getString("bio")
            imgUrl = requireArguments().getString("imgUrl")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBioDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvFullName : TextView = binding.tvFullName
        val tvBio : TextView = binding.tvBio
        layout = binding.biodialog
        val ivProfileImage : ImageView = binding.ivProfileImage
        tvFullName.text = fullName
        tvBio.text = bio
        Glide.with(this).load(imgUrl).into(ivProfileImage)
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance(fullName: String?, bio: String?, imgUrl: String?): BioDialogFragment {
            val fragment = BioDialogFragment()
            val args = Bundle()
            args.putString("fullName", fullName)
            args.putString("bio", bio)
            args.putString("imgUrl", imgUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}