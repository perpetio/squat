package com.perpetio.squat.challenge.model.repository

import androidx.lifecycle.LiveData
import com.perpetio.squat.challenge.domain.PlayerModel

interface LeaderBoardRepo {

    fun addScoreToLeaderBoardList(player: PlayerModel, type: String)

    fun getLeaderBoardList(): LiveData<List<PlayerModel>>

}