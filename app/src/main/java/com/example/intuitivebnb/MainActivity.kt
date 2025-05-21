package com.example.intuitivebnb

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

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

        val menu = navView.menu
        val loggedUser = SessionManager.getLoggedInUser()
        if (loggedUser == null) {
            menu.findItem(R.id.nav_slideshow)?.isVisible = false
        } else {
            menu.findItem(R.id.nav_slideshow)?.isVisible = true
        }

        val topLevelDestinations = if (loggedUser != null) {
            setOf(R.id.nav_home, R.id.nav_slideshow)
        } else {
            setOf(R.id.nav_home)
        }
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set text color of the toolbar title
        binding.appBarMain.toolbar.setTitleTextColor(getColor(R.color.azul))

        // Create and set the DrawerArrowDrawable with your custom color
        drawerArrow = DrawerArrowDrawable(this).apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.azul)
        }
        binding.appBarMain.toolbar.navigationIcon = drawerArrow

        navController.addOnDestinationChangedListener { _, _, _ ->
            drawerArrow.color = ContextCompat.getColor(this, R.color.azul)
            binding.appBarMain.toolbar.navigationIcon = drawerArrow
        }

        if (SessionManager.isUserLoggedIn()) {
            loadUserInfo()
        }
    }

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

                    val headerView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
                        .getHeaderView(0)

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



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

