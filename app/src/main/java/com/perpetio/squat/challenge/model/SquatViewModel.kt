package com.perpetio.squat.challenge.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.perpetio.squat.challenge.model.domain.PlayerModel
import com.perpetio.squat.challenge.model.repository.LeaderBoardRepo
import com.perpetio.squat.challenge.util.ChallengeEnum

class SquatViewModel : ViewModel() {

    private var leaderBoardRepo: LeaderBoardRepo? = null

    private val _name = MutableLiveData<String?>()
    val name: LiveData<String?>
        get() = _name

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    private val _type = MutableLiveData<String?>()
    val type: LiveData<String?>
        get() = _type

    fun setName(userName: String) {
        _name.value = userName
    }

    fun setScore(score: Int) {
        _score.value = score
    }

    fun setType(type: String) {
        _type.value = type
    }

    init {
        leaderBoardRepo = LeaderBoardRepo()
        _type.value = ChallengeEnum.SQUAT.challengeName
        _score.value = 0
    }

    val getLeaderBoardList: LiveData<List<PlayerModel>>
        get() = Transformations.switchMap(type) {
            leaderBoardRepo?.getLeaderBoardList(type.value.toString())
        }

    fun addScoreToLeaderBoardList() {
        if (score.value!! > 0) {
            val exType = type.value!!
            leaderBoardRepo?.addScoreToLeaderBoardList(
                PlayerModel(name.value ?: "noname", score.value.toString(), exType),
                exType
            )
        }

    }

}