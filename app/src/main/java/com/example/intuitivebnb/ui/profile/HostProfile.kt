package com.example.intuitivebnb.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.intuitivebnb.MainActivity
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

//inflater.inflate(R.layout.fragment_host_profile, container, false)

class HostProfile : Fragment() {

    private var activeBooks: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_host_profile, container, false)

        activeBooks = view.findViewById(R.id.activeBooks)
        val imageProfile = view.findViewById<ImageView>(R.id.imageHostProfile)
        val textName = view.findViewById<TextView>(R.id.textNameHostProfile)
        val btnLogOut = view.findViewById<Button>(R.id.bntLogOutHostProfile)

        btnLogOut.setOnClickListener {
            SessionManager.logout()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

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
        activeBooks?.removeAllViews()

        val email = SessionManager.getLoggedInUser()
        if (email == null) return

        db.collection("flats")
            .whereEqualTo("host", email)
            .whereEqualTo("booked", true)
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
        val flatView = inflater.inflate(R.layout.booked_flat_view, activeBooks, false)
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


        activeBooks?.addView(flatView)
    }
}
