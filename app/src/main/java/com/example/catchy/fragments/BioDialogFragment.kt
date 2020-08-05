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

class BioDialogFragment : DialogFragment() {
    private var fullName: String? = null
    private var bio: String? = null
    private var imgUrl: String? = null
    private var layout: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            fullName = requireArguments().getString("fullName")
            bio = requireArguments().getString("bio")
            imgUrl = requireArguments().getString("imgUrl")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bio_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvFullName : TextView = view.findViewById(R.id.tvFullName) as TextView
        val tvBio : TextView = view.findViewById(R.id.tvBio) as TextView
        layout = view.findViewById(R.id.biodialog) as RelativeLayout
        val ivProfileImage : ImageView = view.findViewById(R.id.ivProfileImage) as ImageView
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
}