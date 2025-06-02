package com.example.intuitivebnb.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentTransaction
import com.example.intuitivebnb.R

class MainPage : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Infla el layout y configura los botones para navegar a los fragments de login y registro
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_page, container, false)

        val logingButton: Button = view.findViewById(R.id.btnLogingPrincipal)
        val singUpButton: Button = view.findViewById(R.id.btnSingUpPrincipal)

        // Navega al fragmento de login al pulsar el botón
        logingButton.setOnClickListener {
            val ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.contenedor, Loging())
            ft.commit()
        }

        // Navega al fragmento de registro al pulsar el botón
        singUpButton.setOnClickListener {
            val ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.contenedor, Register())
            ft.commit()
        }

        return view
    }
}
