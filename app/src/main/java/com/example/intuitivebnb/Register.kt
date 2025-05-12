package com.example.intuitivebnb

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Register : Fragment() {
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val spinner: Spinner = view.findViewById(R.id.cbRoleSignUp)
        val buttonRegister: Button = view.findViewById(R.id.btnSignUp)
        val mailRegisterText: EditText = view.findViewById(R.id.editMailSignUp)
        val passwordRegisterText: EditText = view.findViewById(R.id.editPasswordSignUp)
        val nameRegisterText: EditText = view.findViewById(R.id.editNameSignUp)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.opciones,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Listener del Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = parent?.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        buttonRegister.setOnClickListener {
            val nameRegister = nameRegisterText.text.toString()
            val passwordRegister = passwordRegisterText.text.toString()
            val mailRegister = mailRegisterText.text.toString()

            val user = hashMapOf(
                "name" to nameRegister,
                "password" to passwordRegister,
                "mail" to mailRegister,
                "role" to spinner.selectedItem.toString()
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Usuario añadido", Toast.LENGTH_SHORT).show()
                    val ft: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                    ft.replace(R.id.contenedor, Loging()) // Reemplaza con tu Fragment específico
                    ft.commit()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error al añadir usuario", e)
                }
        }

        return view
    }
}
