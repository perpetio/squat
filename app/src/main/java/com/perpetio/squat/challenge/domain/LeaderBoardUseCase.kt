package com.perpetio.squat.challenge.domain

import androidx.lifecycle.LiveData
import com.perpetio.squat.challenge.model.repository.LeaderBoardRepo
import javax.inject.Inject

class LeaderBoardUseCase @Inject constructor(private val firebaseRepo: LeaderBoardRepo) {

    fun addScoreToLeaderBoardList(player: PlayerModel) {
        firebaseRepo.addScoreToLeaderBoardList(player)
    }

    fun getLeaderBoardList(): LiveData<List<PlayerModel>> {
        return firebaseRepo.getLeaderBoardList()
    }

}