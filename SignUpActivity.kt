package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val name = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmpass = binding.confirmPassEt.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty()) {
                if (pass == confirmpass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = firebaseAuth.currentUser?.uid
                            val db = FirebaseFirestore.getInstance()

                            val userData = hashMapOf(
                                "name" to name,
                                "ecoPoints" to 0L,
                                "badges" to listOf<String>()
                            )

                            user?.let {
                                db.collection("users").document(user).set(userData)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }

                        } else {
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
