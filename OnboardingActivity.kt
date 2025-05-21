package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.androidproject.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var handler: Handler
    private var currentPage = 0
    private val slideInterval = 3000L

    private val slides = listOf(
        OnboardingItem(R.drawable.slide1),
        OnboardingItem(R.drawable.slide2),
        OnboardingItem(R.drawable.slide3)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingAdapter(slides)
        binding.viewPager.adapter = adapter

        handler = Handler(Looper.getMainLooper())
        autoSlide()

        binding.signInButton.setOnClickListener {
            Toast.makeText(this, "Sign In Clicked", Toast.LENGTH_SHORT).show()
            println("🚀 Launching LoginActivity...")

            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }


    }

    private fun autoSlide() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentPage = (currentPage + 1) % slides.size
                binding.viewPager.setCurrentItem(currentPage, true)
                handler.postDelayed(this, slideInterval)
            }
        }, slideInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
