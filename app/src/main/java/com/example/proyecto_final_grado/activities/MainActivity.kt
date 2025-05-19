package com.example.proyecto_final_grado.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.apollographql.apollo.ApolloClient
import com.example.proyecto_final_grado.R
import com.example.proyecto_final_grado.apollo.ApolloClientProvider
import com.example.proyecto_final_grado.databinding.ActivityMainBinding
import com.example.proyecto_final_grado.fragments.main.AnimeFragment
import com.example.proyecto_final_grado.fragments.main.HomeFragment
import com.example.proyecto_final_grado.fragments.main.MangaFragment
import com.example.proyecto_final_grado.fragments.main.ProfileFragment
import com.example.proyecto_final_grado.utils.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var apolloClient: ApolloClient
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        apolloClient = ApolloClientProvider.getApolloClient(context = this)

        viewPager = binding.viewPager
        bottomNav = binding.bottomNav

        val fragments = listOf(
            HomeFragment(),
            AnimeFragment(),
            MangaFragment(),
            ProfileFragment()
        )

        if (sharedViewModel.animeList.value == null) {
            sharedViewModel.loadInitialData()
        }

        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        viewPager.adapter = pagerAdapter

        // Cambiar pestaña al deslizar
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })

        // Cambiar fragment con el menú
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> viewPager.currentItem = 0
                R.id.menu_anime -> viewPager.currentItem = 1
                R.id.menu_manga -> viewPager.currentItem = 2
                R.id.menu_profile -> viewPager.currentItem = 3
            }
            true
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val isDetailVisible = supportFragmentManager.backStackEntryCount > 0
            binding.bottomNav.visibility = if (isDetailVisible) View.GONE else View.VISIBLE
            binding.viewPager.visibility = if (isDetailVisible) View.GONE else View.VISIBLE
        }
    }

    fun openDetailFragment(fragment: androidx.fragment.app.Fragment) {
        // Oculta el menú y el ViewPager
        binding.bottomNav.visibility = View.GONE
        binding.viewPager.visibility = View.GONE

        // Muestra el fragmento de detalle ocupando toda la pantalla
        supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .addToBackStack(null) // Permite volver atrás
            .commit()
    }
}