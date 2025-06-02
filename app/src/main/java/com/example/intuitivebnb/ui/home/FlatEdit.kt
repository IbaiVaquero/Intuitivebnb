package com.example.intuitivebnb.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

val db = Firebase.firestore

class FlatEdit : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_flat_edit, container, false)

        val editFlatTitle: EditText = view.findViewById(R.id.editAddTitle)
        val editFlatDescription: EditText = view.findViewById(R.id.editFlatDescription)
        val editFlatLocation: EditText = view.findViewById(R.id.editFlatLocation)
        val editFlatPrice: EditText = view.findViewById(R.id.editFlatPrice)
        val editFlatImage: EditText = view.findViewById(R.id.editFlatImage)
        val btnSave: Button = view.findViewById(R.id.btnSave)
        val btnAddImages: Button = view.findViewById(R.id.btnAddImages)

        val receivedTitle = arguments?.getString("title")

        if (receivedTitle != null) {
            cargarDatos(receivedTitle, editFlatTitle, editFlatDescription, editFlatLocation, editFlatPrice, editFlatImage)
        }

        btnAddImages.setOnClickListener {
            val title = editFlatTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Primero introduce un título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply { putString("flatTitle", title) }
            val fragment = addImages()
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnSave.setOnClickListener {
            val title = editFlatTitle.text.toString().trim()
            val description = editFlatDescription.text.toString().trim()
            val location = editFlatLocation.text.toString().trim()
            val priceInput = editFlatPrice.text.toString().trim()
            val image = editFlatImage.text.toString().trim()

            if (!checks(title, description, location, priceInput, image)) return@setOnClickListener

            val (lonStr, latStr) = location.split(",").map { it.trim() }
            val longitude = lonStr.toDouble()
            val latitude = latStr.toDouble()
            val formattedPrice = "${priceInput.toDouble()}€"

            val flat: HashMap<String, Any?> = hashMapOf(
                "title" to title,
                "description" to description,
                "latitude" to latitude,
                "longitude" to longitude,
                "price" to formattedPrice,
                "image" to listOf(image) // siempre array con la imagen principal
            )

            if (receivedTitle != null) {
                editFlat(flat, receivedTitle)
            } else {
                db.collection("flats")
                    .whereEqualTo("title", title)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            Toast.makeText(requireContext(), "Ya existe un piso con ese título", Toast.LENGTH_SHORT).show()
                        } else {
                            flat["booked"] = false
                            flat["actualGuest"] = "None"
                            flat["host"] = SessionManager.getLoggedInUser()
                            newFlat(flat)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al verificar título", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

    private fun cargarDatos(
        receivedTitle: String,
        editTitle: EditText,
        editDescription: EditText,
        editLocation: EditText,
        editPrice: EditText,
        editImage: EditText
    ) {
        db.collection("flats")
            .whereEqualTo("title", receivedTitle)
            .get()
            .addOnSuccessListener { documents ->
                val doc = documents.firstOrNull() ?: return@addOnSuccessListener
                val images = doc.get("image") as? List<String>
                val mainImage = images?.firstOrNull() ?: ""
                val description = doc.getString("description")
                val price = doc.getString("price")?.replace("€", "")
                val latitude = doc.getDouble("latitude")
                val longitude = doc.getDouble("longitude")

                editTitle.setText(receivedTitle)
                editDescription.setText(description)
                editLocation.setText("$longitude, $latitude")
                editPrice.setText(price)
                editImage.setText(mainImage)
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    private fun newFlat(flat: HashMap<String, Any?>) {
        db.collection("flats")
            .add(flat)
            .addOnSuccessListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor, MyAdds())
                    .commit()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun editFlat(flat: HashMap<String, Any?>, receivedTitle: String) {
        db.collection("flats")
            .whereEqualTo("title", receivedTitle)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val existingImages = doc.get("image") as? List<String> ?: emptyList()
                    val newMainImage = (flat["image"] as List<*>).firstOrNull() as? String ?: ""
                    val updatedImages = listOf(newMainImage) + existingImages.filter { it != newMainImage }

                    flat["image"] = updatedImages

                    db.collection("flats")
                        .document(doc.id)
                        .update(flat as Map<String, Any>)
                        .addOnSuccessListener {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.contenedor, MyAdds())
                                .commit()
                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Error al actualizar el documento", it)
                        }
                } else {
                    Log.w(TAG, "No se encontró un documento con el título: $receivedTitle")
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Error al buscar el documento", it)
            }
    }

    private fun checks(title: String, description: String, location: String, priceInput: String, image: String): Boolean {
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            return false
        }

        if (title.length > 50) {
            Toast.makeText(requireContext(), "El título no puede superar los 50 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        if (description.isBlank()) {
            Toast.makeText(requireContext(), "La descripción no puede estar vacía", Toast.LENGTH_SHORT).show()
            return false
        }

        val parts = location.split(",")
        if (parts.size != 2) {
            Toast.makeText(requireContext(), "La localización debe tener formato 'longitud, latitud'", Toast.LENGTH_SHORT).show()
            return false
        }

        val lon = parts[0].trim().toDoubleOrNull()
        val lat = parts[1].trim().toDoubleOrNull()
        if (lon == null || lat == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
            Toast.makeText(requireContext(), "Latitud o longitud no válidas", Toast.LENGTH_SHORT).show()
            return false
        }

        if ("€" in priceInput) {
            Toast.makeText(requireContext(), "No incluyas el símbolo € en el precio", Toast.LENGTH_SHORT).show()
            return false
        }

        val priceNumber = priceInput.toDoubleOrNull()
        if (priceNumber == null || priceNumber < 0) {
            Toast.makeText(requireContext(), "Introduce un precio válido", Toast.LENGTH_SHORT).show()
            return false
        }

        val urlRegex = Regex("^https?://.*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$", RegexOption.IGNORE_CASE)
        if (!urlRegex.matches(image)) {
            Toast.makeText(requireContext(), "La URL de la imagen no es válida", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
