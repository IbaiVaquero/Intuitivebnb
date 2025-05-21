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
import com.example.intuitivebnb.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class Loging : Fragment() {
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loging, container, false)
        val button: Button = view.findViewById(R.id.btnLoging)
        val mailLoginText: EditText = view.findViewById(R.id.editMailLging)
        val passwordText: EditText = view.findViewById(R.id.editFlatDescription)



        button.setOnClickListener {
            val mailLogin = mailLoginText.text.toString()
            val passwordLogin = passwordText.text.toString()

            if (mailLogin.isNotEmpty() && passwordLogin.isNotEmpty()) {
                verifyUserPassword(mailLogin, passwordLogin) { isCorrect ->
                    if (isCorrect) {
                        checkRole(mailLogin) { role ->
                            if (role != null) {
                                SessionManager.login(mailLogin, role)

                                Toast.makeText(requireContext(), "Login exitoso!", Toast.LENGTH_SHORT).show()
                                val intent = requireActivity().intent
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                requireActivity().startActivity(intent)
                                requireActivity().finish()

                            } else {
                                Toast.makeText(requireContext(), "Error al obtener rol", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    fun checkRole(mailLogin: String, callback: (String?) -> Unit) {
        val usersCollection = db.collection("users")

        usersCollection
            .whereEqualTo("mail", mailLogin)
            .get()
            .addOnSuccessListener { documents ->
                var role: String? = null
                for (document in documents) {
                    role = document.getString("role")
                }
                callback(role)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback(null)
            }
    }

    fun verifyUserPassword(mail: String, inputPassword: String, onComplete: (isCorrect: Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection
            .whereEqualTo("mail", mail)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val storedPassword = documents.documents[0].getString("password")
                    println(storedPassword)
                    println(inputPassword)
                    if (storedPassword != null && storedPassword == inputPassword) {
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete(false)
                println("Error")

            }
    }

}