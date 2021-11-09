package com.perpetio.squat.challenge.domain

import androidx.lifecycle.LiveData
import com.perpetio.squat.challenge.model.repository.LeaderBoardRepo

class LeaderBoardUseCase(private val firebaseRepo: LeaderBoardRepo) {

    fun addScoreToLeaderBoardList(player: PlayerModel, type: String) {
        firebaseRepo.addScoreToLeaderBoardList(player, type)
    }

    fun getLeaderBoardList(): LiveData<List<PlayerModel>> {
        return firebaseRepo.getLeaderBoardList()
    }

}