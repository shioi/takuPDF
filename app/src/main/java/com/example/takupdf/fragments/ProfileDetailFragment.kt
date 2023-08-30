package com.example.takupdf.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.takupdf.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileDetailFragment : Fragment(R.layout.fragment_profile_detail) {
    companion object {
        const val TAG = "PROFILE_TAG"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = this.arguments
        val email = bundle!!.getString("email")
        val mailView = view.findViewById<TextView>(R.id.mail)
        mailView.text = email

        val btn = view.findViewById<Button>(R.id.change_passw)
        btn.setOnClickListener {
            Log.d(TAG, "onViewCreated: Clicked")
            Firebase.auth.sendPasswordResetEmail(email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Send to Mail", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "onViewCreated: send mail")
                    }
                }
        }
    }
}