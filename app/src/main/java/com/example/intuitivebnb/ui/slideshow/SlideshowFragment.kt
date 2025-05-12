package com.example.intuitivebnb.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.intuitivebnb.GuestProfile
import com.example.intuitivebnb.HostProfile
import com.example.intuitivebnb.MainPage
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.example.intuitivebnb.databinding.FragmentSlideshowBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class SlideshowFragment : Fragment() {
    val db = Firebase.firestore

    private var _binding: FragmentSlideshowBinding? = null

    // Esta propiedad es válida solo entre onCreateView y onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val mailLogin = SessionManager.getLoggedInUser()

        if (mailLogin != null) {
            checkRole(mailLogin) { role ->
                if (role != null) {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    when (role) {
                        "Guest" -> transaction.replace(R.id.profileContainer, GuestProfile())
                        "Host" -> transaction.replace(R.id.profileContainer, HostProfile())
                        else -> transaction.replace(R.id.contenedor, MainPage())
                    }
                    transaction.addToBackStack(null)
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Error al obtener rol", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
