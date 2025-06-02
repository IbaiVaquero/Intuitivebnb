package com.example.intuitivebnb.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.intuitivebnb.R
import com.google.firebase.firestore.FirebaseFirestore

class addImages : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var editFlatImage2: EditText
    private lateinit var editFlatImage3: EditText
    private lateinit var editFlatImage4: EditText
    private lateinit var editFlatImage5: EditText
    private lateinit var editFlatImage6: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_images, container, false)

        val btnSaveImages: Button = view.findViewById(R.id.btnSaveImages)
        editFlatImage2 = view.findViewById(R.id.editFlatImage2)
        editFlatImage3 = view.findViewById(R.id.editFlatImage3)
        editFlatImage4 = view.findViewById(R.id.editFlatImage4)
        editFlatImage5 = view.findViewById(R.id.editFlatImage5)
        editFlatImage6 = view.findViewById(R.id.editFlatImage6)

        val flatTitle = arguments?.getString("flatTitle")
        if (flatTitle == null) {
            Toast.makeText(requireContext(), "No se recibió el título del piso", Toast.LENGTH_SHORT).show()
            return view
        }

        // Carga imágenes existentes (excepto la primera)
        db.collection("flats")
            .whereEqualTo("title", flatTitle)
            .get()
            .addOnSuccessListener { documents ->
                val flatDoc = documents.firstOrNull()
                if (flatDoc != null) {
                    val images = flatDoc.get("image") as? List<String> ?: emptyList()
                    if (images.size > 1) editFlatImage2.setText(images.getOrNull(1) ?: "")
                    if (images.size > 2) editFlatImage3.setText(images.getOrNull(2) ?: "")
                    if (images.size > 3) editFlatImage4.setText(images.getOrNull(3) ?: "")
                    if (images.size > 4) editFlatImage5.setText(images.getOrNull(4) ?: "")
                    if (images.size > 5) editFlatImage6.setText(images.getOrNull(5) ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar imágenes", Toast.LENGTH_SHORT).show()
            }

        // Guarda las nuevas imágenes en Firestore
        btnSaveImages.setOnClickListener {
            val newImages = listOf(
                editFlatImage2.text.toString(),
                editFlatImage3.text.toString(),
                editFlatImage4.text.toString(),
                editFlatImage5.text.toString(),
                editFlatImage6.text.toString()
            ).filter { it.isNotEmpty() }

            if (newImages.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa al menos una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("flats")
                .whereEqualTo("title", flatTitle)
                .get()
                .addOnSuccessListener { documents ->
                    val flatDoc = documents.firstOrNull() ?: return@addOnSuccessListener
                    val images = flatDoc.get("image") as? MutableList<String> ?: mutableListOf()
                    val firstImage = images.firstOrNull()

                    val updatedImages = mutableListOf<String>()
                    if (firstImage != null) updatedImages.add(firstImage)
                    updatedImages.addAll(newImages)

                    db.collection("flats")
                        .document(flatDoc.id)
                        .update("image", updatedImages)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Imágenes actualizadas correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al actualizar imágenes", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al buscar el piso", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
