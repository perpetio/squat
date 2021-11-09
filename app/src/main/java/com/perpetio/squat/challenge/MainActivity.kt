package com.perpetio.squat.challenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.perpetio.squat.challenge.model.SportViewModel
import com.perpetio.squat.challenge.model.SportViewModelFactory
import com.perpetio.squat.challenge.model.domain.LeaderBoardUseCase
import com.perpetio.squat.challenge.model.repository.LeaderBoardRepoImpl

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val sportViewModelFactory = SportViewModelFactory(LeaderBoardUseCase(LeaderBoardRepoImpl()))
        ViewModelProvider(this, sportViewModelFactory)
            .get(SportViewModel::class.java)
    }

}