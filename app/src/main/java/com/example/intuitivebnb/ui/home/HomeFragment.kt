package com.example.intuitivebnb.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.intuitivebnb.MainPage
import com.example.intuitivebnb.MyAdds
import com.example.intuitivebnb.R
import com.example.intuitivebnb.SearchMap
import com.example.intuitivebnb.SessionManager
import com.example.intuitivebnb.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (SessionManager.isUserLoggedIn()){
            if (SessionManager.isHost()){
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, MyAdds())
                transaction.commit()
            } else if (SessionManager.isGuest()){
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.contenedor, SearchMap())
                transaction.commit()
            }
        } else {

            val ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.contenedor, MainPage())
            ft.commit()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}