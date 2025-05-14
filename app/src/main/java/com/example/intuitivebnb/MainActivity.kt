package com.example.intuitivebnb

import android.os.Bundle
import android.view.Menu
import android.view.View
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerArrow: DrawerArrowDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_slideshow), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set text color of the toolbar title
        binding.appBarMain.toolbar.setTitleTextColor(getColor(R.color.azul))

        // Create and set the DrawerArrowDrawable with your custom color
        drawerArrow = DrawerArrowDrawable(this).apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.azul)
        }
        binding.appBarMain.toolbar.navigationIcon = drawerArrow

        // First listener for handling the Drawer Arrow color update
        navController.addOnDestinationChangedListener { _, _, _ ->
            drawerArrow.color = ContextCompat.getColor(this, R.color.azul)
            binding.appBarMain.toolbar.navigationIcon = drawerArrow
        }

        // Actualiza la visibilidad de la AppBar de acuerdo a la vista activa en el FrameLayout
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.frameLayoutContainer -> {
                    // Ocultar la AppBar cuando estamos en el FrameLayout con el ID "frameLayoutContainer"
                    binding.appBarMain.toolbar.visibility = View.GONE
                }
                else -> {
                    // Mostrar la AppBar para otras vistas
                    binding.appBarMain.toolbar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

