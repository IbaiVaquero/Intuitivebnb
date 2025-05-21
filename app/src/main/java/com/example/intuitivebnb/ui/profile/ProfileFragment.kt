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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment() {
    private val db = Firebase.firestore

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mailLogin = SessionManager.getLoggedInUser()

        if (mailLogin != null) {
            // Puedes mostrar un loader aquí si quieres, por ejemplo:
            binding.progressBar.visibility = View.VISIBLE

            checkRole(mailLogin) { role ->
                // Ocultar loader cuando obtengas resultado
                binding.progressBar.visibility = View.GONE

                if (role != null) {
                    // Reemplaza el fragmento solo cuando tengas rol válido
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    when (role) {
                        "Guest" -> transaction.replace(R.id.contenedor, GuestProfile())
                        "Host" -> transaction.replace(R.id.contenedor, HostProfile())
                        else -> transaction.replace(R.id.contenedor, MainPage())
                    }
                    // No añado a backstack para que al hacer atrás no vuelva a este fragmento que es solo pantalla "transición"
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Error al obtener rol", Toast.LENGTH_SHORT).show()
                    // En caso de error, ir a MainPage para que la app no se quede en blanco
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedor, MainPage())
                        .commit()
                }
            }
        } else {
            // Si mailLogin es null, mostrar toast y cargar MainPage
            Toast.makeText(requireContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor, MainPage())
                .commit()
        }

        return root
    }

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
