package com.example.intuitivebnb.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MyAdds : Fragment() {
    private var myAdds: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Infla el layout y configura el botón para crear un nuevo anuncio
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_adds, container, false)
        myAdds = view.findViewById(R.id.myAdds)

        val btnNewAdd: Button = view.findViewById(R.id.btnNewAdd)
        btnNewAdd.setOnClickListener{
            // Navega al fragmento para editar o crear un nuevo anuncio
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, FlatEdit())
            transaction.commit()
        }

        // Carga y muestra los anuncios del usuario
        loadAdds(inflater)
        return view
    }

    // Obtiene los anuncios del usuario logueado y los muestra en el layout
    private fun loadAdds(inflater: LayoutInflater) {
        myAdds?.removeAllViews()

        val email = SessionManager.getLoggedInUser()
        if (email == null) return

        db.collection("flats")
            .whereEqualTo("host", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    val images = document.get("image") as? List<String>
                    val image = images?.getOrNull(0)
                    val name = document.getString("actualGuest")
                    val price = document.getString("price")
                    val calificacion = document.getDouble("calificacion")

                    addFlat(inflater, title, image, name, price, calificacion, document.id)
                }
            }
            .addOnFailureListener { it.printStackTrace() }
    }

    // Crea la vista de cada anuncio con sus datos y configura los botones editar y eliminar
    private fun addFlat(
        inflater: LayoutInflater,
        title: String?,
        image: String?,
        description: String?,
        price: String?,
        calificacion: Double?,
        id: String
    ) {
        val flatView = inflater.inflate(R.layout.my_flat_view, myAdds, false)

        val titleTextView = flatView.findViewById<TextView>(R.id.myAddTitle)
        val priceTextView = flatView.findViewById<TextView>(R.id.myAddMoney)
        val imageImageView = flatView.findViewById<ImageView>(R.id.myAddImage)
        val btnEditFlat = flatView.findViewById<ImageButton>(R.id.btnEditFlat)
        val btnDeleteFlat = flatView.findViewById<ImageButton>(R.id.btnDeleteFlat)

        // Elimina el anuncio de Firestore y lo remueve de la vista
        btnDeleteFlat.setOnClickListener {
            db.collection("flats").document(id)
                .delete()
                .addOnSuccessListener {
                    myAdds?.removeView(flatView)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }

        // Navega al fragmento de edición del anuncio con el título pasado como argumento
        btnEditFlat.setOnClickListener {
            val title = titleTextView.text.toString()
            val flatFragment = FlatEdit()
            val bundle = Bundle()
            bundle.putString("title", title)
            flatFragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, flatFragment)
            transaction.addToBackStack(null)  // Permite volver atrás
            transaction.commit()

            myAdds?.removeAllViews()
        }

        // Asigna los datos a las vistas correspondientes
        titleTextView.text = title
        priceTextView.text = price

        // Carga la imagen usando Picasso
        Picasso.get()
            .load(image)
            .into(imageImageView)

        myAdds?.addView(flatView)
    }
}
