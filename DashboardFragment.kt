
import android.content.Intent
import android.os.Bundlepackage com.example.androidproject

import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.androidproject.com.example.androidproject.Trip
import com.example.androidproject.com.example.androidproject.TripsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query



class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripsAdapter
    private val tripsList = mutableListOf<Trip>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var badgeProgressBar: ProgressBar
    private lateinit var fuelSavedTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsButton = view.findViewById<ImageButton>(R.id.settingsButton)
        val startTripButton = view.findViewById<Button>(R.id.startTripButton)
        fuelSavedTextView = view.findViewById(R.id.fuelSavedText)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        badgeProgressBar = view.findViewById(R.id.badgeProgressBar)

        listenToBadgeProgress()

        // Handle settings click
        settingsButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Handle start trip button
        startTripButton.setOnClickListener {
            val intent = Intent(requireContext(), TripTrackingActivity::class.java)
            startActivity(intent)
        }

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.previousTripsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TripsAdapter(tripsList)
        recyclerView.adapter = adapter

        // Load Average Eco Score
        loadAverageEcoScore(view)

        // Load Previous Trips
        loadPreviousTrips()

        loadFuelSaved()

    }

    private fun listenToBadgeProgress() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BadgeProgress", "Error listening to badge data", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val ecoPoints = snapshot.getLong("ecoPoints")?.toInt() ?: 0

                    // Example logic for progress (adjust based on your badge thresholds)
                    val badgeThreshold = 100
                    val progress = (ecoPoints * 100 / badgeThreshold).coerceAtMost(100)

                    // Update the badge progress bar
                    badgeProgressBar.progress = progress

                    // Optionally update the eco score display if you want real-time updates there too
                    val ecoScoreTextView = view?.findViewById<TextView>(R.id.tvAverageEcoScore)
                    ecoScoreTextView?.text = ecoPoints.toString() // You can adjust the logic based on your needs
                }
            }
    }



    private fun loadAverageEcoScore(view: View) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .collection("trips")
                .get()
                .addOnSuccessListener { documents ->
                    val scores = documents.mapNotNull { it.getString("ecoScore")?.toFloatOrNull() }
                    val average = if (scores.isNotEmpty()) scores.average().toInt() else 0
                    val ecoScoreTextView = view.findViewById<TextView>(R.id.tvAverageEcoScore)
                    ecoScoreTextView.text = average.toString()
                }
        }
    }

    private fun loadFuelSaved() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("trips")
                .get()
                .addOnSuccessListener { documents ->
                    var totalFuelSaved = 0.0
                    for (doc in documents) {
                        // Get the fuelSaved field from each trip document
                        totalFuelSaved += doc.getDouble("fuelSaved") ?: 0.0
                    }
                    // Update the Fuel Saved TextView with the total fuel saved
                    fuelSavedTextView.text = "%.2f L".format(totalFuelSaved)
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardFragment", "Failed to load fuel saved", e)
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
                .get()
                .addOnSuccessListener { documents ->
                    tripsList.clear()
                    for (doc in documents) {
                        val trip = doc.toObject(Trip::class.java)
                        tripsList.add(trip)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("DashboardFragment", "Failed to load trips", e)
                }
        }
    }
}
