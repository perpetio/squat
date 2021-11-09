package com.perpetio.squat.challenge.util

import com.miguelrochefort.fitnesscamera.lib.BodyPart
import com.miguelrochefort.fitnesscamera.lib.Person


class ChallengeRepetitionCounter(
    private val activityType: String?,
    private val listener: CounterListener
) {

    val MIN_AMPLITUDE = 40
    val REP_THRESHOLD = 0.8
    val MIN_CONFIDENCE = 0.5

    var isTurnOn: Boolean = false

    var count = 0

    var previousY = 0
    var previousDeltaY = 0
    var first = true
    var goal = 1
    var top = 0
    var bottom = 0

    private fun onCalculateSquatsData(person: Person): Int {
        if (person.keyPoints[BodyPart.LEFT_HIP.ordinal].score >= MIN_CONFIDENCE && person.keyPoints[BodyPart.RIGHT_HIP.ordinal].score >= MIN_CONFIDENCE) {
            val y1 = person.keyPoints[BodyPart.LEFT_HIP.ordinal].position.y
            val y = 1000 - y1
            val dy = y - previousY
            if (!first) {
                if (bottom != 0 && top != 0) {
                    if (goal == 1 && dy > 0 && (y - bottom) > (top - bottom) * REP_THRESHOLD) {
                        if (top - bottom > MIN_AMPLITUDE) {
                            count++
                            goal = -1
                            listener.onCounterVoice(count)
                        }
                    } else if (goal == -1 && dy < 0 && (top - y) > (top - bottom) * REP_THRESHOLD) {
                        goal = 1
                    }
                }

                if (dy < 0 && previousDeltaY >= 0 && previousY - bottom > MIN_AMPLITUDE) {
                    top = previousY
                } else if (dy > 0 && previousDeltaY <= 0 && top - previousY > MIN_AMPLITUDE) {
                    bottom = previousY
                }
            }
            first = false
            previousY = y
            previousDeltaY = dy
        }
        return count
    }

    private fun onCalculateJumpingData(person: Person): Int {
        if (person.keyPoints[BodyPart.LEFT_ANKLE.ordinal].score >= MIN_CONFIDENCE && person.keyPoints[BodyPart.RIGHT_ANKLE.ordinal].score >= MIN_CONFIDENCE) {
            val y = person.keyPoints[BodyPart.LEFT_ANKLE.ordinal].position.y
            val dy = y - previousY
            if (!first) {
                if (bottom != 0 && top != 0) {
                    if (goal == 1 && dy < 0) {
                        count++
                        goal = -1
                        listener.onCounterVoice(count)
                    } else if (goal == -1 && dy < 0 && (top - y) > (top - bottom) * REP_THRESHOLD) {
                        goal = 1
                    }
                }
                if (dy < 0 && previousDeltaY >= 0 && previousY - bottom > MIN_AMPLITUDE) {
                    top = previousY
                } else if (dy > 0 && previousDeltaY <= 0 && top - previousY > MIN_AMPLITUDE) {
                    bottom = previousY
                }
            }
            first = false
            previousY = y
            previousDeltaY = dy
        }
        return count
    }

    fun resetCounter() {
        count = 0
        previousY = 0
        previousDeltaY = 0
        first = true
        goal = 1
        top = 0
        bottom = 0
    }

    fun changeTurnState(): Boolean {
        isTurnOn = !isTurnOn
        return isTurnOn
    }

    fun onCalculateData(person: Person): Int {
        return if (activityType.equals(ChallengeEnum.SQUAT.challengeName))
            onCalculateSquatsData(person) else onCalculateJumpingData(person)
    }

    interface CounterListener {
        fun onCounterVoice(quantity: Int)
    }
}