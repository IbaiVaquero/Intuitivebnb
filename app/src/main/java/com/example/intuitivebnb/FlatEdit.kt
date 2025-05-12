package com.example.intuitivebnb

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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

val db = Firebase.firestore

class FlatEdit : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flat_edit, container, false)

        val editFlatTitle: EditText = view.findViewById(R.id.editAddTitle)
        val editFlatDescription: EditText = view.findViewById(R.id.editFlatDescription)
        val editFlatLocation: EditText = view.findViewById(R.id.editFlatLocation)
        val editFlatPrice: EditText = view.findViewById(R.id.editFlatPrice)
        val editFlatimage: EditText = view.findViewById(R.id.editFlatImage)
        val btnSave: Button = view.findViewById(R.id.btnSave)
        val btnAddImages: Button = view.findViewById(R.id.btnAddImages)

        // Obtener los argumentos del Fragmento
        val recivedTitle = arguments?.getString("title")

        if (recivedTitle != null) {
            cargarDatos(recivedTitle, editFlatTitle, editFlatDescription, editFlatLocation, editFlatPrice, editFlatimage)
        }


        btnSave.setOnClickListener {
            val title = editFlatTitle.text.toString()
            val description = editFlatDescription.text.toString()
            val location = editFlatLocation.text.toString()
            val priceInput = editFlatPrice.text.toString()
            val image = editFlatimage.text.toString()

            // Validar título
            if (title.length > 50) {
                Toast.makeText(requireContext(), "El título no puede superar los 50 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato de localización
            val parts = location.split(",")
            if (parts.size != 2) {
                Toast.makeText(requireContext(), "La localización debe tener formato 'longitud, latitud'", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val longitude = parts[0].trim()
            val latitude = parts[1].trim()

            val lat = latitude.toDoubleOrNull()
            val lon = longitude.toDoubleOrNull()

            if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                Toast.makeText(requireContext(), "Latitud o longitud no válidas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar URL de la imagen
            val urlRegex = Regex("^https?://.*\\.(jpg|jpeg|png|gif|bmp)(\\?.*)?$", RegexOption.IGNORE_CASE)
            if (!urlRegex.matches(image)) {
                Toast.makeText(requireContext(), "La URL de la imagen no es válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar precio (debe ser número y no debe contener €)
            if (priceInput.contains("€")) {
                Toast.makeText(requireContext(), "No incluyas el símbolo € en el precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val priceNumber = priceInput.toDoubleOrNull()
            if (priceNumber == null || priceNumber < 0) {
                Toast.makeText(requireContext(), "Introduce un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formattedPrice = "${priceNumber}€"

            if (recivedTitle != null) {
                val flat: HashMap<String, Any?> = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "latitude" to lat,
                    "longitude" to lon,
                    "price" to formattedPrice,
                    "image" to image
                )
                editFlat(flat, recivedTitle)
            }else{
                val flat: HashMap<String, Any?> = hashMapOf(
                    "booked" to false,
                    "actualGuest" to "None",
                    "host" to SessionManager.getLoggedInUser(),
                    "title" to title,
                    "description" to description,
                    "latitude" to lat.toString(),
                    "longitude" to lon.toString(),
                    "price" to formattedPrice,
                    "image" to image
                )
                newFlat(flat)
            }
        }

        return view
    }

    private fun cargarDatos(
        recivedTitle: String?,
        editFlatTitle1: EditText,
        editFlatDescription: EditText,
        editFlatLocation: EditText,
        editFlatPrice: EditText,
        editFlatimage: EditText
    ) {
        db.collection("flats")
            .whereEqualTo("title", recivedTitle)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val image = document.getString("image")
                    val description = document.getString("actualGuest")
                    val price = document.getString("price")?.replace("€", "")
                    editFlatTitle1.setText(recivedTitle)
                    editFlatDescription.setText(description)
                    editFlatLocation.setText(document.getString("latitude") + "," + document.getString("longitude"))
                    editFlatPrice.setText(price)
                    editFlatimage.setText(image)
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    fun newFlat(flat: HashMap<String, Any?>) {
        db.collection("flats")
            .add(flat)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, MyAdds())
                transaction.commit()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
    fun editFlat(flat: HashMap<String, Any?>, recivedTitle: String) {
        // Buscar el documento por el título
        db.collection("flats")
            .whereEqualTo("title", recivedTitle)  // Filtramos por título
            .get()  // Obtenemos los documentos
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Si encontramos al menos un documento con ese título
                    val document = querySnapshot.documents[0]  // Tomamos el primer documento
                    val documentId = document.id  // Obtenemos el ID del documento

                    // Convertir el HashMap<String, String> a Map<String, Any>
                    val updatedFlatMap: Map<String, Any> = flat.mapValues { it.value as Any }

                    // Ahora actualizamos el documento con el ID obtenido
                    db.collection("flats")
                        .document(documentId)  // Accedemos al documento por su ID
                        .update(updatedFlatMap)  // Actualizamos los campos con el Map<String, Any>

                        .addOnSuccessListener {
                            Log.d(TAG, "Documento actualizado con ID: $documentId")
                            // Transacción para navegar al fragmento de mis anuncios
                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                            transaction.replace(R.id.contenedor, MyAdds())  // Cambiar a la pantalla de mis anuncios
                            transaction.commit()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al actualizar el documento", e)
                        }
                } else {
                    Log.w(TAG, "No se encontró un documento con el título: $recivedTitle")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al buscar el documento", e)
            }
    }



}



