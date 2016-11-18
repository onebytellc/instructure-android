package com.instructure.androidfoosball.ktmodels

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class CutThroatGame(
        @PrimaryKey
        open var id: String = UUID.randomUUID().toString(),
        open var status: String = GameStatus.ONGOING.name,
        open var pointsToWin: Int = 0,
        open var rotateAfter: Int = 0,
        open var singleIdx: Int = 0,
        open var pointsSinceRotation: Int = 0,
        open var players: RealmList<CutThroatPlayer> = RealmList(),
        open var startTime: Long = -1L,
        open var endTime: Long = -1L
) : RealmObject() {
    fun getSingle() = players[singleIdx]
    fun hasWinner(): Boolean = getWinner() != null
    fun getWinner(): User? = players.firstOrNull { it.score >= pointsToWin }?.user
}
