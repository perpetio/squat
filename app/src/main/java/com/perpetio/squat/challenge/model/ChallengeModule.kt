package com.perpetio.squat.challenge.model

import com.perpetio.squat.challenge.model.repository.LeaderBoardFirebaseRepoImpl
import com.perpetio.squat.challenge.model.repository.LeaderBoardRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class ChallengeModule {

    @Binds
    abstract fun getLeaderBoardSource(repo: LeaderBoardFirebaseRepoImpl): LeaderBoardRepo
}