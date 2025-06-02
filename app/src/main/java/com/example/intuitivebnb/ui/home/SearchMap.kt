package com.example.intuitivebnb.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.intuitivebnb.ui.flat.FlatPage
import com.example.intuitivebnb.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SearchMap : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var addRows: LinearLayout? = null
    private val db = FirebaseFirestore.getInstance()

    // Infla el layout y obtiene la referencia al contenedor de anuncios
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_map, container, false)
        addRows = view.findViewById(R.id.addRows)
        return view
    }

    // Inicializa el mapa cuando la vista está creada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Configura el mapa una vez está listo, centra la cámara y carga contenido
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val coordenada = LatLng(40.343568, -3.716361)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordenada, 4f), 4000, null)
        cargarContenido(layoutInflater)
    }

    // Crea un marcador en el mapa si las coordenadas son válidas
    private fun createMarker(latitude: Double?, longitude: Double?, title: String?) {
        if (latitude != null && longitude != null) {
            val coordenada = LatLng(latitude, longitude)
            val marker = MarkerOptions().position(coordenada).title(title ?: "Ubicación")
            map.addMarker(marker)
        } else {
            println("Error: Coordenadas nulas")
        }
    }

    // Carga los flats no reservados desde Firestore y los muestra con marcador
    private fun cargarContenido(inflater: LayoutInflater) {
        addRows?.removeAllViews()
        db.collection("flats")
            .whereEqualTo("booked", false)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    val images = document.get("image") as? List<String>
                    val image = images?.getOrNull(0)
                    val description = document.getString("description")
                    val price = document.getString("price")
                    val calificacion = document.getDouble("calificacion")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    addFlat(inflater, title, image, description, price, calificacion)
                    createMarker(latitude, longitude, title)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Crea la vista para un flat y configura el botón para abrir la página del flat
    private fun addFlat(
        inflater: LayoutInflater,
        title: String?,
        image: String?,
        description: String?,
        price: String?,
        calificacion: Double?
    ) {
        val flatView: View = inflater.inflate(R.layout.flat_view, addRows, false)
        val titleTextView = flatView.findViewById<TextView>(R.id.myAddTitle)
        val descriptionTextView = flatView.findViewById<TextView>(R.id.myAddDescription)
        val priceTextView = flatView.findViewById<TextView>(R.id.myAddPrice)
        val imageImageView = flatView.findViewById<ImageView>(R.id.myAddImage)
        val btnEnterFlat = flatView.findViewById<Button>(R.id.btnEnterBookedFlat)

        titleTextView.text = title
        descriptionTextView.text = description
        priceTextView.text = price
        Picasso.get()
            .load(image)
            .into(imageImageView)

        btnEnterFlat.setOnClickListener {
            val title = titleTextView.text.toString()
            val flatFragment = FlatPage()

            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("origin", "searchMap")
            flatFragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.contenedor, flatFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            addRows?.removeAllViews()
        }

        addRows?.addView(flatView)
    }
}
