package com.example.takupdf.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.takupdf.R
import com.example.takupdf.databinding.ActivityMainBinding
import com.example.takupdf.fragments.AboutFragment
import com.example.takupdf.fragments.ImageFragment
import com.example.takupdf.fragments.PdfListFragment
import com.example.takupdf.fragments.ProfileDetailFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var email: String

    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchEmail()
        loadImageFragment()

        bottomNavigationView = findViewById(R.id.bottomNavigationMenu)


        toggle = ActionBarDrawerToggle(this, binding.drawerLayout,R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Log.d(TAG, "onCreate: $email")

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.bottom_menu_images -> {
                    loadImageFragment()
                }
                R.id.bottom_menu_pdfs -> {
                    loadPdfFragment()
                }
            }
            return@setOnItemSelectedListener true
        }


        //Navigation on click listeners
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.profile_item -> {
                    loadProfileFragment()
                }
                R.id.logout_item -> {
                    logout()
                }
                R.id.about_app_item -> {
                    loadAboutFragment()
                }
                R.id.bug_report_item -> {
                    sendBugReport()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.account_email).text = email


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return false
    }

    private fun loadImageFragment() {
        val imageFragment = ImageFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, imageFragment)
            .commit()
    }

    private fun loadPdfFragment() {
        val pdfFragment = PdfListFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, pdfFragment)
            .commit()
    }

    private fun loadAboutFragment() {
        val aboutFragment = AboutFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, aboutFragment)
            .addToBackStack("about")
            .commit()
    }

    private fun loadProfileFragment(){
        val profileFragment = ProfileDetailFragment()
        val bundle = Bundle()
        bundle.putString("email", email)
        profileFragment.setArguments(bundle)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, profileFragment)
            .addToBackStack("profile")
            .commit()
    }

    private fun logout() {
        Firebase.auth.signOut()
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun fetchEmail(){
        val user = Firebase.auth.currentUser
        user?.let {
            email = it.email.toString()
        }
    }

    private fun sendBugReport() {
        val recipientMail = "helloloneliness3032001@gmail.com"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientMail))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report for takuPDF")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
        startActivity(Intent.createChooser(intent, "Select your Email app"))
    }

}