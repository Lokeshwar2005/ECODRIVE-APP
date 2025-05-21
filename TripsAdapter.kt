package com.example.androidproject.com.example.androidproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.R

class TripsAdapter(private val trips: List<Trip>) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int {
        return trips.size
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripLabel: TextView = itemView.findViewById(R.id.tripLabel)
        private val fuelSavedText: TextView = itemView.findViewById(R.id.fuelSavedText)
        private val ecoScoreText: TextView = itemView.findViewById(R.id.ecoScoreText)

        fun bind(trip: Trip) {
            tripLabel.text = "Trip ${adapterPosition + 1}"  // Dynamic trip number
            fuelSavedText.text = "Fuel Saved: ${trip.fuelSaved} L"
            ecoScoreText.text = "Eco Score: ${trip.ecoScore}"
        }
    }
}
