package com.perpetio.squat.challenge.model

import androidx.lifecycle.*
import com.perpetio.squat.challenge.domain.LeaderBoardUseCase
import com.perpetio.squat.challenge.domain.PlayerModel
import com.perpetio.squat.challenge.util.ChallengeEnum
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(private val leaderBoardUseCase: LeaderBoardUseCase) : ViewModel() {

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
        _type.value = ChallengeEnum.SQUAT.challengeName
        _score.value = 0
    }

    val getLeaderBoardList: LiveData<List<PlayerModel>>
        get() = Transformations.switchMap(type) {
            getFilteredData()
        }

    private fun getFilteredData() = Transformations.map(leaderBoardUseCase.getLeaderBoardList()) { it ->
        it.filter {
            it.type == type.value.toString()
        }
    }

    fun addScoreToLeaderBoardList() {
        if (score.value!! > 0) {
            val exType = type.value!!
            leaderBoardUseCase.addScoreToLeaderBoardList(
                PlayerModel(name.value ?: "noname", score.value.toString(), exType))
        }
    }

}