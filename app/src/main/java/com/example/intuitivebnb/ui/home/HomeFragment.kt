package com.example.intuitivebnb.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SessionManager
import com.example.intuitivebnb.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Infla la vista del fragmento y decide qué fragmento mostrar según el estado de sesión
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (SessionManager.isUserLoggedIn()) {
            // Si el usuario está logueado, muestra MyAdds si es host o SearchMap si es guest
            if (SessionManager.isHost()) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, MyAdds())
                transaction.addToBackStack(null)
                transaction.commit()
            } else if (SessionManager.isGuest()) {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, SearchMap())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        } else {
            // Si no hay usuario logueado, muestra la página principal (login/registro)
            val ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            ft.add(R.id.contenedor, MainPage())
            ft.addToBackStack(null)
            ft.commit()
        }

        return root
    }

    // Limpia el binding para evitar fugas de memoria
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
