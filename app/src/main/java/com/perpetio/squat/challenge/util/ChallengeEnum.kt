package com.perpetio.squat.challenge.util

enum class ChallengeEnum(val challengeName: String) {

    SQUAT("Squatting"),
    JUMP("Jumping");

    companion object {

        fun getAllExercises(): List<String> {
            val arrays = mutableListOf<String>()
            for (challenge in values()) {
                arrays.add(challenge.challengeName)
            }
            return arrays
        }

    }
}

