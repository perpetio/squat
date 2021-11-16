package com.perpetio.squat.challenge.model.repository

import androidx.lifecycle.LiveData
import com.perpetio.squat.challenge.domain.PlayerModel

interface LeaderBoardRepo {

    fun addScoreToLeaderBoardList(player: PlayerModel)

    fun getLeaderBoardList(): LiveData<List<PlayerModel>>

}