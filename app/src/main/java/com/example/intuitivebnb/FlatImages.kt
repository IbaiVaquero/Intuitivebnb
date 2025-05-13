package com.example.intuitivebnb

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FlatImages : Fragment() {
    private var imagesBox: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()
    private var title: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flat_images, container, false)
        imagesBox = view.findViewById(R.id.imagesBox)
        val titleImagesPage = view.findViewById<TextView>(R.id.titleFlatPage4)
        titleImagesPage.text = title
        loadImages(inflater)
        val btnBack= view.findViewById<Button>(R.id.btnBackImages)
        btnBack.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, SearchMap())
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return view
    }
    private fun loadImages(inflater: LayoutInflater) {
        imagesBox?.removeAllViews()

        db.collection("flats")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrls = document["images"] as? List<*>

                    imageUrls?.forEach { url ->
                        if (url is String && url.isNotBlank()) {
                            val imageViewLayout = inflater.inflate(R.layout.image_team, imagesBox, false)
                            val imageView = imageViewLayout.findViewById<ImageView>(R.id.imageFlat)
                            Picasso.get().load(url).into(imageView)
                            imagesBox?.addView(imageViewLayout)
                        }
                    }
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }
    }
