package com.example.androidproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

import com.google.firebase.auth.FirebaseAuth

class MoreFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvUserName: TextView
    private lateinit var tvVehicleType: TextView
    private lateinit var themeSwitch: Switch
    private lateinit var logoutCard: CardView
    private lateinit var profileCard: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        // Profile Card without functionality (removed profile settings click)
        profileCard = view.findViewById(R.id.profilecard)
        // No click listener for profile card anymore

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Views
        tvUserName = view.findViewById(R.id.tvUserName)
        tvVehicleType = view.findViewById(R.id.tvVehicleType)
        themeSwitch = view.findViewById(R.id.themeSwitch)
        logoutCard = view.findViewById(R.id.logoutCard)

        // Load user info (this can be kept if required for the logout functionality)
        loadUserInfo()

        // Set up the theme switch
        setupThemeSwitch()

        // Set up the logout functionality
        setupLogout()

        return view
    }

    private fun loadUserInfo() {
        // Logic for loading user info can be simplified or removed
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Display the user info (in case required in the future)
            tvUserName.text = currentUser.displayName ?: "Eco Driver"
            tvVehicleType.text = "Vehicle: Unknown"  // This can be updated if needed
        }
    }

    private fun setupThemeSwitch() {
        val sharedPref = requireContext().getSharedPreferences("theme", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("darkMode", false)

        themeSwitch.isChecked = isDark

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("darkMode", isChecked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupLogout() {
        logoutCard.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Removed ProfileSettingsActivity result handling
}
