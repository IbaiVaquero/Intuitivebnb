package com.example.intuitivebnb

import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.intuitivebnb.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerArrow: DrawerArrowDrawable

    // Configura la actividad principal con Navigation Drawer y NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Oculta o muestra la opción "Perfil" según si el usuario está logueado
        val menu = navView.menu
        val loggedUser = SessionManager.getLoggedInUser()
        if (loggedUser == null) {
            menu.findItem(R.id.nav_slideshow)?.isVisible = false
        } else {
            menu.findItem(R.id.nav_slideshow)?.isVisible = true
        }

        // Define destinos top-level según estado del usuario
        val topLevelDestinations = if (loggedUser != null) {
            setOf(R.id.nav_home, R.id.nav_slideshow)
        } else {
            setOf(R.id.nav_home)
        }
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Establece el color del título de la toolbar
        binding.appBarMain.toolbar.setTitleTextColor(getColor(R.color.azul))

        // Configura el icono del drawer con color personalizado
        drawerArrow = DrawerArrowDrawable(this).apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.azul)
        }
        binding.appBarMain.toolbar.navigationIcon = drawerArrow

        navController.addOnDestinationChangedListener { _, _, _ ->
            drawerArrow.color = ContextCompat.getColor(this, R.color.azul)
            binding.appBarMain.toolbar.navigationIcon = drawerArrow
        }

        // Carga la información del usuario en el header del drawer si está logueado
        if (SessionManager.isUserLoggedIn()) {
            loadUserInfo()
        }
    }

    // Obtiene datos del usuario desde Firestore y los muestra en el header del Navigation Drawer
    fun loadUserInfo() {
        val email = SessionManager.getLoggedInUser() ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("mail", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.documents[0]
                    val name = userDoc.getString("name")
                    val imageUrl = userDoc.getString("image")

                    val headerView = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)

                    val nameTextView = headerView.findViewById<TextView>(R.id.navName)
                    val emailTextView = headerView.findViewById<TextView>(R.id.navMail)
                    val imageView = headerView.findViewById<ImageView>(R.id.navImage)

                    nameTextView.text = name
                    emailTextView.text = email

                    if (!imageUrl.isNullOrEmpty()) {
                        com.squareup.picasso.Picasso.get()
                            .load(imageUrl)
                            .into(imageView)
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    // Infla el menú principal (opciones de toolbar)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Maneja la navegación "Up" en la toolbar con NavController
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
