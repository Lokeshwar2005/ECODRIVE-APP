package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.com.example.androidproject.Trip
import com.example.androidproject.com.example.androidproject.TripsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class Dashboard : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripsAdapter
    private val tripsList = mutableListOf<Trip>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val starttripButton = findViewById<Button>(R.id.startTripButton)

        loadAverageEcoScore()

        settingsButton.setOnClickListener {
            // Create an AlertDialog for logging out
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Log Out")
            builder.setMessage("Are you sure you want to log out?")

            builder.setPositiveButton("Log Out") { dialog, which ->
                // Log out the user using Firebase Authentication
                FirebaseAuth.getInstance().signOut()

                // Redirect to the login screen (or wherever you need)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()  // Finish the current activity if needed
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()  // Dismiss the dialog if Cancel is clicked
            }

            builder.show()  // Display the dialog
        }
        starttripButton.setOnClickListener {
            val intent = Intent(this, TripTrackingActivity::class.java)
            startActivity(intent)
        }
        recyclerView = findViewById(R.id.previousTripsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TripsAdapter(tripsList)
        recyclerView.adapter = adapter

        loadPreviousTrips()
    }
    private fun loadAverageEcoScore() {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("trips")
                .get()
                .addOnSuccessListener { documents ->
                    var totalScore = 0
                    var count = 0

                    for (doc in documents) {
                        val scoreStr = doc.getString("ecoScore")
                        val score = scoreStr?.toIntOrNull()
                        if (score != null) {
                            totalScore += score
                            count++
                        }
                    }

                    val averageScore = if (count > 0) totalScore / count else 0
                    val ecoScoreTextView = findViewById<TextView>(R.id.tvAverageEcoScore)
                    ecoScoreTextView.text = "$averageScore / 100"
                }
                .addOnFailureListener { e ->
                    Log.e("Dashboard", "Failed to load eco scores", e)
                }
        }
    }
    private fun loadPreviousTrips() {
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("trips")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    tripsList.clear()
                    var totalFuelSaved = 0f

                    for (doc in documents) {
                        val trip = doc.toObject(Trip::class.java)
                        tripsList.add(trip)
                        totalFuelSaved += trip.fuelSaved
                    }

                    val fuelSavedText: TextView = findViewById(R.id.fuelSavedText)
                    fuelSavedText.text = String.format("%.1f L", totalFuelSaved)

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("Dashboard", "Failed to load trips", e)
                }
        }
    }
}