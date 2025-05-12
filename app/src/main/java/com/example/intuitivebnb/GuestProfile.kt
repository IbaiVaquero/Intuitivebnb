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

class GuestProfile : Fragment() {

    private var myBooks: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guest_profile, container, false)

        myBooks = view.findViewById(R.id.myBooks)
        val imageProfile = view.findViewById<ImageView>(R.id.imageGuestProfile)
        val textName = view.findViewById<TextView>(R.id.textNameGuestProfile)

        loadProfile(imageProfile, textName)
        loadAdds(inflater)

        return view
    }

    private fun loadProfile(imageProfile: ImageView, textName: TextView) {
        val email = SessionManager.getLoggedInUser()
        if (email == null) return

        db.collection("users")
            .whereEqualTo("mail", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val name = document.getString("name")
                    val image = document.getString("image")

                    textName.text = name ?: "Usuario"
                    Picasso.get()
                        .load(image)
                        .resize(120, 120)
                        .centerCrop()
                        .into(imageProfile)
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    private fun loadAdds(inflater: LayoutInflater) {
        myBooks?.removeAllViews()

        val email = SessionManager.getLoggedInUser()
        if (email == null) return

        db.collection("flats")
            .whereEqualTo("actualGuest", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    val image = document.getString("image")
                    val description = document.getString("description")
                    val price = document.getString("price")
                    val calificacion = document.getDouble("calificacion")

                    addFlat(inflater, title, image, description, price, calificacion)
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    private fun addFlat(
        inflater: LayoutInflater,
        title: String?,
        image: String?,
        description: String?,
        price: String?,
        calificacion: Double?
    ) {
        val flatView = inflater.inflate(R.layout.booked_flat_view, myBooks, false)
        val titleTextView = flatView.findViewById<TextView>(R.id.myAddTitle)
        val descriptionTextView = flatView.findViewById<TextView>(R.id.myAddDescription)
        val priceTextView = flatView.findViewById<TextView>(R.id.myAddPrice)
        val imageImageView = flatView.findViewById<ImageView>(R.id.myAddImage)
        val btnEnterFlat = flatView.findViewById<Button>(R.id.btnEnterBookedFlat)

        titleTextView.text = title
        descriptionTextView.text = description
        priceTextView.text = price

        Picasso.get()
            .load(image)
            .into(imageImageView)

        btnEnterFlat.setOnClickListener {
            val flatTitle = titleTextView.text.toString()
            val flatFragment = FlatPage().apply {
                arguments = Bundle().apply {
                    putString("title", flatTitle)
                }
            }

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, flatFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            myBooks?.removeAllViews()
        }

        myBooks?.addView(flatView)
    }
}
