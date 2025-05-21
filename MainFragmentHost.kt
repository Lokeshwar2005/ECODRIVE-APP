import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.androidproject.DashboardFragment
import com.example.androidproject.EcoLeaderBoardFragment
import com.example.androidproject.MoreFragment
import com.example.androidproject.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragmentHost : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPagerMain)
        bottomNavigationView = view.findViewById(R.id.bottomNavigation)

        val fragmentList = listOf(
            DashboardFragment(),
            EcoLeaderBoardFragment(),
            MoreFragment()
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragmentList.size
            override fun createFragment(position: Int): Fragment = fragmentList[position]
        }

        // Sync BottomNavigationView with ViewPager2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        // Sync ViewPager2 when BottomNavigationView item is selected
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_eco_board -> viewPager.currentItem = 1
                R.id.nav_more -> viewPager.currentItem = 2
            }
            true
        }
    }
}

