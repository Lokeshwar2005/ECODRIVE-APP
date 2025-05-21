package com.example.androidproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var vehicleTypeSpinner: Spinner
    private lateinit var changeProfilePicButton: Button
    private lateinit var saveButton: Button

    private val vehicleTypes = listOf("Car", "Truck", "Motorcycle", "Electric Vehicle", "Hybrid")

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        profileImageView = findViewById(R.id.profileImageView)
        vehicleTypeSpinner = findViewById(R.id.vehicleTypeSpinner)
        changeProfilePicButton = findViewById(R.id.changeProfilePicButton)
        saveButton = findViewById(R.id.saveButton)

        // Set up the Spinner with vehicle types
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vehicleTypeSpinner.adapter = adapter

        // Handle changing the profile picture
        changeProfilePicButton.setOnClickListener {
            // Open the gallery or camera to select an image (for simplicity, use an intent for the gallery)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1001)
        }

        // Handle saving the profile picture and vehicle type
        saveButton.setOnClickListener {
            val updatedVehicleType = vehicleTypeSpinner.selectedItem?.toString() ?: "Default Vehicle Type"
            val updatedProfilePicUri = selectedImageUri?.toString() ?: "default_uri"


            if (updatedVehicleType != null) {
                // Check if profile image URI is null or not, and assign a default value if it's null

                // Log the values before proceeding with saving
                Log.d("ProfileSettings", "Vehicle Type: $updatedVehicleType, Profile Pic URI: $updatedProfilePicUri")

                // Create an Intent to pass the updated values back
                val resultIntent = Intent()
                resultIntent.putExtra("USER_NAME", "Updated User Name")  // You can add logic to retrieve the updated name
                resultIntent.putExtra("VEHICLE_TYPE", updatedVehicleType)
                resultIntent.putExtra("PROFILE_PIC_URI", updatedProfilePicUri)

                setResult(Activity.RESULT_OK, resultIntent)
                finish()  // Close the activity and return to the calling activity
            } else {
                Toast.makeText(this, "Please select a vehicle type", Toast.LENGTH_SHORT).show()
            }
        }



    }

    // Handle the result of image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
            this.selectedImageUri = selectedImageUri
        } else {
            Log.d("ProfileSettings", "Image selection failed or cancelled.")
        }
    }


}
