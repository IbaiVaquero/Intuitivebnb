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


class MyAdds : Fragment() {
    private var myAdds: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_adds, container, false)
        myAdds = view.findViewById(R.id.myAdds)
        val btnNewAdd: Button = view.findViewById(R.id.btnNewAdd)
        btnNewAdd.setOnClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, FlatEdit())
            transaction.commit()
        }
        loadAdds(inflater)
        return view
    }

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
                    val image = document.getString("image")
                    val name = document.getString("actualGuest")
                    val price = document.getString("price")
                    val calificacion = document.getDouble("calificacion")

                    addFlat(inflater, title, image, name, price, calificacion)
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
        val flatView = inflater.inflate(R.layout.my_flat_view, myAdds, false)
        val titleTextView = flatView.findViewById<TextView>(R.id.myAddTitle)
        val priceTextView = flatView.findViewById<TextView>(R.id.myAddMoney)
        val imageImageView = flatView.findViewById<ImageView>(R.id.myAddImage)
        val btnEditFlat = flatView.findViewById<Button>(R.id.btnEditFlat)

        btnEditFlat.setOnClickListener {
            val title = titleTextView.text.toString()

            val flatFragment = FlatEdit()

            val bundle = Bundle()
            bundle.putString("title", title)
            flatFragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, flatFragment)
            transaction.addToBackStack(null)  // Esto permite volver a la pantalla anterior
            transaction.commit()

            myAdds?.removeAllViews()
        }

        titleTextView.text = title
        priceTextView.text = price

        Picasso.get()
            .load(image)
            .into(imageImageView)


        myAdds?.addView(flatView)
    }

}