package com.instructure.androidfoosball.ktmodels

import com.instructure.androidfoosball.utils.edit
import com.instructure.androidfoosball.utils.elseIfBlank
import io.realm.RealmList
import io.realm.RealmObject

open class Round(
        open var pointsToWin: Int = 5,
        open var sideOneTeam: Team = Team(),
        open var sideTwoTeam: Team = Team(),
        open var goalHistory: RealmList<Goal> = RealmList(),
        open var startTime: Long = -1L,
        open var endTime: Long = -1L
) : RealmObject() {
    fun getScore(team: Team) = goalHistory.count { it.team == team }

    fun getTeamName(side: TableSide, table: Table) = when (side) {
        TableSide.SIDE_1 -> sideOneTeam.customName.elseIfBlank(table.sideOneName)
        TableSide.SIDE_2 -> sideTwoTeam.customName.elseIfBlank(table.sideTwoName)
    }
    fun getTeamName(team: Team, table: Table) = when (team) {
        sideOneTeam -> sideOneTeam.customName.elseIfBlank(table.sideOneName)
        sideTwoTeam -> sideTwoTeam.customName.elseIfBlank(table.sideTwoName)
        else -> ""
    }

    fun recordGoal(side: TableSide) {
        edit {
            val goal = Goal(if (side == TableSide.SIDE_1) sideOneTeam else sideTwoTeam)
            goalHistory.add(goal)
        }
    }

    fun hasWinner() = getWinningTeam() != null

    fun getWinningTeam(): Team? = when {
        getScore(sideOneTeam) >= pointsToWin -> sideOneTeam
        getScore(sideTwoTeam) >= pointsToWin -> sideTwoTeam
        else -> null
    }

    fun getLosingTeam(): Team? = when {
        getScore(sideOneTeam) >= pointsToWin -> sideTwoTeam
        getScore(sideTwoTeam) >= pointsToWin -> sideOneTeam
        else -> null
    }
}