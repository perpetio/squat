package com.perpetio.squat.challenge.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.perpetio.squat.challenge.domain.PlayerModel
import com.perpetio.squat.challenge.util.ChallengeEnum

class LeaderBoardFirebaseRepoImpl : LeaderBoardRepo {

    private var myRef: DatabaseReference? = null
    private val _leaderBoardPlayer = MutableLiveData<List<PlayerModel>>()
    private val leaderBoardPlayer: LiveData<List<PlayerModel>> = _leaderBoardPlayer

    init {
        val database =
            Firebase.database("https://squat-challenge-107da-default-rtdb.europe-west1.firebasedatabase.app/")
        myRef = database.getReference("leaderboard")

        myRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfPlayer = mutableListOf<PlayerModel>()
                for (player in dataSnapshot.child(ChallengeEnum.SQUAT.challengeName).children) {
                    listOfPlayer.add(player.getValue(PlayerModel::class.java)!!)
                }
                for (player in dataSnapshot.child(ChallengeEnum.JUMP.challengeName).children) {
                    listOfPlayer.add(player.getValue(PlayerModel::class.java)!!)
                }
                listOfPlayer.sortByDescending { player -> player.score.toInt() }
                _leaderBoardPlayer.value = listOfPlayer
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun addScoreToLeaderBoardList(player: PlayerModel, type: String) {
        myRef?.child(type)?.push()?.setValue(player)
    }

    override fun getLeaderBoardList(type: String): LiveData<List<PlayerModel>> {
        return Transformations.map(leaderBoardPlayer) { it ->
            it.filter {
                it.type == type
            }
        }
    }

}