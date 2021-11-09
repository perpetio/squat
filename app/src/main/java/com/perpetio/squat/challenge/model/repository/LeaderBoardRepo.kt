package com.perpetio.squat.challenge.model.repository

import androidx.lifecycle.LiveData
import com.perpetio.squat.challenge.model.domain.PlayerModel

interface LeaderBoardRepo {

    fun addScoreToLeaderBoardList(player: PlayerModel, type: String)

    fun getLeaderBoardList(type: String): LiveData<List<PlayerModel>>

}