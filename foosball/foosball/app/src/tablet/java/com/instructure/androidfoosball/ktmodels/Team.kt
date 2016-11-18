package com.instructure.androidfoosball.ktmodels

import io.realm.RealmList
import io.realm.RealmObject


open class Team(
        open var customName: String = "",
        open var users: RealmList<User> = RealmList()
) : RealmObject() {

    fun getTeamHash() = users.sortedBy { it.id }.fold("") { hash, user -> hash + user.id }

    fun getAverageWinRate(): Float {
        val (wins, losses) = users.fold(Pair(0, 0)) { pair, user ->
            Pair(pair.first + user.wins, pair.second + user.losses)
        }
        if (wins == 0) return 0f
        if (losses == 0) return 100f
        return wins * 100f / (wins + losses)
    }

    fun getCompositeFoosRank() = users.map { it.foosRanking }.average()

}
