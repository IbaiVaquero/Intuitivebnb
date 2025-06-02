package com.example.intuitivebnb.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.intuitivebnb.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class Register : Fragment() {
    private val db = Firebase.firestore

    // Infla el layout y configura el spinner y el botón de registro
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
        val imageRegisterText: EditText = view.findViewById(R.id.editImageSignUp)

        // Configura el spinner con opciones desde resources
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.opciones,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Muestra un toast con la opción seleccionada del spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = parent?.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Maneja el evento click del botón registrar
        buttonRegister.setOnClickListener {
            val nameRegister = nameRegisterText.text.toString().trim()
            val passwordRegister = passwordRegisterText.text.toString().trim()
            val mailRegister = mailRegisterText.text.toString().trim()
            val imageRegister = imageRegisterText.text.toString().trim()
            val selectedRole = spinner.selectedItem.toString()

            // URL por defecto si el usuario no añade imagen
            val defaultImageUrl = "https://cdn-icons-png.flaticon.com/512/9131/9131529.png"
            val finalImageUrl = if (imageRegister.isEmpty()) defaultImageUrl else imageRegister

            // Validaciones básicas de campos
            if (nameRegister.isEmpty() || passwordRegister.isEmpty() || mailRegister.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordRegister.length < 8) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!mailRegister.contains("@") || !mailRegister.contains(".")) {
                Toast.makeText(requireContext(), "Introduce un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Verifica que el email no esté registrado antes de añadir nuevo usuario
            db.collection("users")
                .whereEqualTo("mail", mailRegister)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Toast.makeText(requireContext(), "Este email ya está registrado", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = hashMapOf(
                            "name" to nameRegister,
                            "password" to passwordRegister,
                            "mail" to mailRegister,
                            "role" to selectedRole,
                            "image" to finalImageUrl
                        )
                        // Añade el nuevo usuario a Firestore

                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Usuario registrado", Toast.LENGTH_SHORT).show()
                                val ft: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                                ft.replace(R.id.contenedor, Loging())
                                ft.commit()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error al registrar usuario", e)
                                Toast.makeText(requireContext(), "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error verificando email", exception)
                    Toast.makeText(requireContext(), "Error de conexión con la base de datos", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
