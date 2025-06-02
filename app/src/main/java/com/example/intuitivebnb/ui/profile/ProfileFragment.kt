package com.example.intuitivebnb.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.intuitivebnb.ui.home.MainPage
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.example.intuitivebnb.databinding.FragmentSlideshowBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val db = Firebase.firestore

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    // Infla la vista y según el rol del usuario carga el fragmento correspondiente
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mailLogin = SessionManager.getLoggedInUser()

        if (mailLogin != null) {
            binding.progressBar.visibility = View.VISIBLE

            // Consulta el rol del usuario y carga el fragmento adecuado
            checkRole(mailLogin) { role ->
                binding.progressBar.visibility = View.GONE

                if (role != null) {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    when (role) {
                        "Guest" -> transaction.replace(R.id.contenedor, GuestProfile())
                        "Host" -> transaction.replace(R.id.contenedor, HostProfile())
                        else -> transaction.replace(R.id.contenedor, MainPage())
                    }
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Error al obtener rol", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor, MainPage())
                        .commit()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor, MainPage())
                .commit()
        }

        return root
    }

    // Consulta Firestore para obtener el rol del usuario por su email
    fun checkRole(mailLogin: String, callback: (String?) -> Unit) {
        db.collection("users")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
