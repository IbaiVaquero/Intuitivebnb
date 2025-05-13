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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.firestore.FirebaseFirestore


class Reviews : Fragment() {
    private var faltReviews: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()
    private var title: String? = null  // Aquí guardaremos el 'title'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title")  // Recuperamos 'title' desde los argumentos
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        faltReviews = view.findViewById(R.id.flatReviews)
        val btnBackReview = view.findViewById<Button>(R.id.btnBackReview)
        val rateEdit = view.findViewById<EditText>(R.id.editRate)
        val commentEdit = view.findViewById<EditText>(R.id.editComment)

        val btnAddReview = view.findViewById<Button>(R.id.btnAddReview)
        btnAddReview.setOnClickListener {
            if (SessionManager.isUserLoggedIn()) {
                val rate = rateEdit.text.toString().toDoubleOrNull()
                val comment = commentEdit.text.toString()

                if (rate == null || comment.isBlank()) {
                    Toast.makeText(requireContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val review = hashMapOf(
                    "mail" to SessionManager.getLoggedInUser(),
                    "rate" to rate,
                    "comment" to comment,
                    "flatName" to title,
                )

                db.collection("reviews")
                    .add(review)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Review añadida", Toast.LENGTH_SHORT).show()

                        // Limpiar los campos
                        rateEdit.text.clear()
                        commentEdit.text.clear()

                        // Recargar el fragmento Reviews con el mismo título
                        val newFragment = Reviews()
                        val args = Bundle()
                        args.putString("title", title)
                        newFragment.arguments = args

                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.contenedor, newFragment)
                        transaction.commit()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error al añadir review", e)
                        Toast.makeText(requireContext(), "Error al añadir review", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Debes iniciar sesión para añadir una review", Toast.LENGTH_SHORT).show()
            }
        }

        btnBackReview.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, SearchMap()) // Reemplazar el fragmento actual con SearchMap
            transaction.addToBackStack(null) // Añadir a la pila de retroceso si deseas poder volver al fragmento anterior
            transaction.commit() // Confirmar la transacción
        }
        loadReviews(inflater)
        return view
    }

    private fun loadReviews(inflater: LayoutInflater) {
        faltReviews?.removeAllViews()
        title?.let { title ->
            db.collection("reviews")
                .whereEqualTo("flatName", title)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val comment = document.getString("comment")
                        val mail = document.getString("mail")
                        val rate = document.getDouble("rate")

                        addReview(mail, rate, comment, inflater)
                    }
                }
                .addOnFailureListener { it.printStackTrace() }
        }
    }

    private fun addReview(
        mail: String?,
        rate: Double?,
        comment: String?,
        inflater: LayoutInflater
    ) {
        val flatView = inflater.inflate(R.layout.view_review, faltReviews, false)
        val userMailReview = flatView.findViewById<TextView>(R.id.userMailReview)
        val commentReview = flatView.findViewById<TextView>(R.id.commentReview)
        val rateReview = flatView.findViewById<TextView>(R.id.rateReview)


        userMailReview.text = mail
        commentReview.text = comment
        rateReview.text = rate.toString()

        faltReviews?.addView(flatView)
    }


}
