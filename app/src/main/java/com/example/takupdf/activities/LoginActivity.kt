package com.example.takupdf.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.takupdf.R
import com.example.takupdf.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    //for firebase authentication
    private lateinit var auth: FirebaseAuth
    //for shared preference for username
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var progressDialog: ProgressDialog

    companion object {
        const val TAG = "LOGIN_TAG"
        const val myPreference = "mypref"
        const val Username = "nameKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase setup
        auth = Firebase.auth

        //setup share preference
        sharedPreferences = getSharedPreferences(myPreference, Context.MODE_PRIVATE)

        //setting up progress bar for login
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        //setting up the registration
        binding.toRegistrationBtn.setOnClickListener {
            Log.d(TAG, "to registration button click: ")

            val view = LayoutInflater.from(this).inflate(R.layout.registration_page,null)
            //setting up
            val builder = AlertDialog.Builder(this)
            builder.setView(view)
            val alertDialog = builder.create()
            alertDialog.show()

            val loginBtn = view.findViewById<Button>(R.id.login_btn)
            val email = view.findViewById<EditText>(R.id.email_register)
            val password = view.findViewById<EditText>(R.id.password_register)
            val retypePassword = view.findViewById<EditText>(R.id.password_register_retype)

            loginBtn.setOnClickListener {
                //validation
                if(email.text.toString().isEmpty()) {
                    toast("Empty Email field")
                    return@setOnClickListener
                }
                if(password.text.toString().isEmpty()) {
                    toast("Empty Password field")
                    return@setOnClickListener
                }
                if(retypePassword.text.toString().isEmpty()) {
                    toast("Empty Retype Password field")
                    return@setOnClickListener
                }

                if(password.text.toString() != retypePassword.text.toString()) {
                    toast("Password does not Match")
                    return@setOnClickListener
                }

                signupUser(email.text.toString(), password.text.toString())
            }
        }
        //login
        binding.loginBtn.setOnClickListener {
            //validation
            val email = binding.usernameLogin.text.toString()
            val password = binding.passwordLogin.text.toString()

            if(email.isEmpty()){
                toast("Email is Empty")
                return@setOnClickListener
            }
            if(password.isEmpty()) {
                toast("Password is Empty")
                return@setOnClickListener
            }

            login(email,password)

        }
    }

    private fun signupUser(email: String, password: String) {
        progressDialog.setMessage("Signing up....")
        progressDialog.show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    Log.d(TAG, "signupUser: $user")
                    toast("Created $user successfully")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun login(email: String, password: String) {
        Log.d(TAG, "login: logging in with $email")
        progressDialog.setMessage("Logging in....")
        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    toast("Welcome")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d(TAG, "signInWithEmail:failure", task.exception)
                    toast("Authentication failed")
                    updateUI(null)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        //check if the user is signed in (non-null)
        val currentUser = auth.currentUser
        Log.d(TAG, "onStart: ${currentUser}")
        if(currentUser != null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun toast(text:String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user == null) {
            toast("Authentication failed.")
            return
        }
        //intent
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

}