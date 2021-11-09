package com.perpetio.squat.challenge.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.perpetio.squat.challenge.domain.LeaderBoardUseCase

class SportViewModelFactory(val leaderBoardUseCase: LeaderBoardUseCase) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(LeaderBoardUseCase::class.java)
            .newInstance(leaderBoardUseCase)
    }
}