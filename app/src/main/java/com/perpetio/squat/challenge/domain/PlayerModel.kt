package com.perpetio.squat.challenge.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerModel(val name: String = "Without name", val score: String= "0", val type : String = "squat") : Parcelable