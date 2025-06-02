package com.example.intuitivebnb.ui.flat

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.example.intuitivebnb.ui.profile.GuestProfile
import com.example.intuitivebnb.ui.home.SearchMap
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FlatPage : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private var title: String? = null
    private var flatId: String? = null
    private var isBooked: Boolean = false

    // Recupera el título del piso desde los argumentos del fragmento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString("title")
    }

    // Infla el layout del fragmento, carga los datos del piso y configura los botones
    // Configura los elementos de la UI y carga información del piso y sus valoraciones
    // También gestiona navegación hacia atrás, galería de imágenes y valoraciones
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
            val origin = arguments?.getString("origin")

            val fragmentToReturn = when (origin) {
                "guestProfile" -> GuestProfile()
                "searchMap" -> SearchMap()
                else -> null
            }

            if (fragmentToReturn != null) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, fragmentToReturn)
                transaction.commit()
            } else {
                requireActivity().supportFragmentManager.popBackStack()
            }

        }

        db.collection("reviews")
            .whereEqualTo("flatName", title)
            .get()
            .addOnSuccessListener { documents ->
                var totalRate = 0.0
                var count = 0

                for (document in documents) {
                    val rate = document.getDouble("rate")
                    if (rate != null) {
                        totalRate += rate
                        count++
                    }
                }

                if (count > 0) {
                    val averageRate = totalRate / count
                    textAssessment.text = averageRate.toString()
                } else {
                    textAssessment.text = ""
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }

        db.collection("flats")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    flatId = document.id
                    val images = document.get("image") as? List<String>
                    val image = images?.getOrNull(0)
                    val description = document.getString("description")
                    val price = document.getString("price")
                    isBooked = document.getBoolean("booked") ?: false
                    val actualGuest = document.getString("actualGuest") ?: ""

                    val needsCorrection = (isBooked && actualGuest.isBlank()) || (!isBooked && actualGuest.isNotBlank())
                    if (needsCorrection) {
                        val correctionData = mutableMapOf<String, Any>("booked" to isBooked)
                        if (isBooked) {
                            correctionData["actualGuest"] = SessionManager.getLoggedInUser() ?: ""
                        } else {
                            correctionData["actualGuest"] = FieldValue.delete()
                        }

                        db.collection("flats").document(flatId!!).update(correctionData)
                    }

                    titleflatPage.text = title
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
                isBooked = !isBooked // Alternar estado
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

        btnImagesFlat.setOnClickListener {
            val title = titleflatPage.text.toString()
            val bundle = Bundle()
            bundle.putString("title", title)

            val imagesFragment = FlatImages()
            imagesFragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, imagesFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val origin = arguments?.getString("origin")

                val fragmentToReturn = when (origin) {
                    "guestProfile" -> GuestProfile()
                    "searchMap" -> SearchMap()
                    else -> null
                }

                if (fragmentToReturn != null) {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.contenedor, fragmentToReturn)
                    transaction.commit()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }

            }
        })
        return view
    }

    // Actualiza en Firestore el estado de reserva y el usuario asociado
    private fun updateBookingStatus(flatId: String, isBooked: Boolean) {
        val loggedInUser = SessionManager.getLoggedInUser() ?: ""

        val updateData = mutableMapOf<String, Any>(
            "booked" to isBooked
        )

        if (isBooked) {
            updateData["actualGuest"] = loggedInUser
        } else {
            updateData["actualGuest"] = FieldValue.delete()
        }

        db.collection("flats").document(flatId)
            .update(updateData)
            .addOnSuccessListener {
                println("Estado de reserva actualizado correctamente.")
            }
            .addOnFailureListener { e ->
                println("Error al actualizar: ${e.message}")
            }
    }
    // Cambia la apariencia del botón según el estado de reserva
    private fun updateButtonUI(button: Button, isBooked: Boolean) {
        if (isBooked) {
            button.setBackgroundColor(Color.GREEN)
            button.text = "Booked"
        } else {
            button.setBackgroundColor(Color.parseColor("#31ABFF"))
            button.text = "Book"
        }
    }
}
