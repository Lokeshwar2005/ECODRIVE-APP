package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.androidproject.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // ✅ Firestore setup
                        val user = firebaseAuth.currentUser
                        user?.let {
                            val db = FirebaseFirestore.getInstance()
                            val userRef = db.collection("users").document(user.uid)

                            userRef.get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    val userData = hashMapOf(
                                        "name" to "Anonymous",
                                        "ecoPoints" to 0L,
                                        "badges" to listOf<String>()
                                    )
                                    userRef.set(userData)
                                }
                            }
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // optional: finish login screen
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()

        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}