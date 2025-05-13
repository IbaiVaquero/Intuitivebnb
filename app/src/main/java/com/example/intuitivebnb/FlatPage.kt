package com.example.intuitivebnb

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FlatPage : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private var title: String? = null
    private var flatId: String? = null
    private var isBooked: Boolean = false  // Estado de reserva

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString("title")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flat_page, container, false)
        val titleflatPage = view.findViewById<TextView>(R.id.titleFlatPage)
        val imageViewFlat = view.findViewById<ImageView>(R.id.imageViewFlat)
        val btnBook = view.findViewById<Button>(R.id.btnBook)
        val textAssessment = view.findViewById<TextView>(R.id.textAssessment)
        val textPrice = view.findViewById<TextView>(R.id.textPrice)
        val textDescription = view.findViewById<TextView>(R.id.textDescription)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnImagesFlat = view.findViewById<Button>(R.id.btnImagesFlat)
        val btnFlatReviews = view.findViewById<Button>(R.id.btnReviews)
            btnBack.setOnClickListener {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, SearchMap()) // Reemplazar el fragmento actual con SearchMap
                transaction.addToBackStack(null) // Añadir a la pila de retroceso si deseas poder volver al fragmento anterior
                transaction.commit() // Confirmar la transacción
            }


        db.collection("flats")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    flatId = document.id
                    val image = document.getString("image")
                    val description = document.getString("description")
                    val price = document.getString("price")
                    val calification = document.getDouble("calification")
                    isBooked = document.getBoolean("booked") ?: false

                    // Actualizar UI
                    titleflatPage.text = title
                    textAssessment.text = calification.toString()
                    textPrice.text = price
                    textDescription.text = description
                    Picasso.get().load(image).into(imageViewFlat)

                    updateButtonUI(btnBook, isBooked)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }

        btnBook.setOnClickListener {
            flatId?.let { id ->
                isBooked = !isBooked // Alternar el estado
                updateBookingStatus(id, isBooked)
                updateButtonUI(btnBook, isBooked)
            }
        }

        btnFlatReviews.setOnClickListener {
            val title = titleflatPage.text.toString()

            val bundle = Bundle()
            bundle.putString("title", title)

            val reviewsFragment = Reviews()
            reviewsFragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, reviewsFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }






        return view
    }

    private fun updateBookingStatus(flatId: String, isBooked: Boolean) {
        val loggedInUser = SessionManager.getLoggedInUser() ?: ""
        val updateData = mutableMapOf<String, Any>(
            "booked" to isBooked,
            "actualGuest" to loggedInUser
        )

        db.collection("flats").document(flatId)
            .update(updateData)
            .addOnSuccessListener {
                println("Estado de reserva actualizado correctamente.")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar: ${e.message}")
            }
    }

    private fun updateButtonUI(button: Button, isBooked: Boolean) {
        if (isBooked) {
            button.setBackgroundColor(Color.GREEN)
            button.text = "Booked"
        }
    }
}
