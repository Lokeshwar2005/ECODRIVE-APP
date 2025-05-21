package com.example.androidproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R
import com.example.androidproject.databinding.FragmentEcoLeaderBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration


class EcoLeaderBoardFragment : Fragment() {

    private lateinit var binding: FragmentEcoLeaderBoardBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userListener: ListenerRegistration? = null
    private lateinit var ecoPointsText: TextView
    private lateinit var badgeLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var smoothStarterBadge: ImageView
    private lateinit var carbonSaverBadge: ImageView




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEcoLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userNameText: TextView = binding.userNameText
        val ecoPointsText: TextView = binding.ecoPointsText
        smoothStarterBadge = binding.smoothStarterBadge
        carbonSaverBadge = binding.carbonSaverBadge
        progressBar = binding.progressBar
        val leaderboardRecyclerView: RecyclerView = binding.leaderboardRecyclerView

        // Fetch the current user's UID from Firebase
        val userId = auth.currentUser?.uid

        if (userId != null) {
            userListener = db.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Optional: Show toast or log error
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Eco Driver"
                        val points = snapshot.getLong("ecoPoints") ?: 0
                        val badges = snapshot.get("badges") as? List<*> ?: emptyList<Any>()

                        // Update the UI with the fetched data
                        userNameText.text = "Hello $name"
                        ecoPointsText.text = "Eco Points: $points"

                        // Display badges if the user has them
                        smoothStarterBadge.visibility = if (badges.contains("Smooth Starter")) View.VISIBLE else View.GONE
                        carbonSaverBadge.visibility = if (badges.contains("Carbon Saver")) View.VISIBLE else View.GONE

                        // Set the progress bar based on the ecoPoints
                        progressBar.progress = points.toInt() // Example, adjust if needed
                    }
                }
        }

        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance()

        loadLeaderboard(leaderboardRecyclerView)
        loadEcoPoints()
        loadBadges()
    }


    private fun loadLeaderboard(leaderboardRecyclerView: RecyclerView) {
        firestore.collection("users").orderBy("ecoPoints", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val leaderboardList = result.map { document ->
                    LeaderboardEntry(
                        document.getString("name") ?: "Unknown",
                        document.getLong("ecoPoints")?.toInt() ?: 0
                    )
                }
                leaderboardRecyclerView.layoutManager = LinearLayoutManager(context)
                leaderboardRecyclerView.adapter = LeaderboardAdapter(leaderboardList)
            }
    }
    private fun loadEcoPoints() {
        val userId = currentUser.currentUser?.uid
        firestore.collection("users").document(userId!!).get()
            .addOnSuccessListener { document ->
                val ecoPoints = document.getLong("ecoPoints") ?: 0
                binding.ecoPointsText.text = "Eco Points: $ecoPoints"
                progressBar.progress = ecoPoints.toInt()
            }
    }

    private fun loadBadges() {
        val userId = currentUser.currentUser?.uid
        firestore.collection("users").document(userId!!).get()
            .addOnSuccessListener { document ->
                val badges = document.get("badges") as List<String>
                if ("Smooth Starter" in badges) {
                    smoothStarterBadge.visibility = View.VISIBLE
                }
                if ("Carbon Saver" in badges) {
                    carbonSaverBadge.visibility = View.VISIBLE
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener when the view is destroyed to prevent memory leaks
        userListener?.remove()
    }

    data class LeaderboardEntry(val name: String, val ecoPoints: Int)

    class LeaderboardAdapter(private val leaderboardList: List<LeaderboardEntry>) :
        RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
            return LeaderboardViewHolder(view)
        }

        override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
            val entry = leaderboardList[position]
            holder.rank.text = (position + 1).toString() // Set rank number
            holder.name.text = entry.name
            holder.ecoPoints.text = "Eco Points: ${entry.ecoPoints}"
        }

        override fun getItemCount(): Int = leaderboardList.size

        class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rank: TextView = itemView.findViewById(R.id.leaderboardRank)
            val name: TextView = itemView.findViewById(R.id.leaderboardName)
            val ecoPoints: TextView = itemView.findViewById(R.id.leaderboardEcoPoints)
        }
    }
}
