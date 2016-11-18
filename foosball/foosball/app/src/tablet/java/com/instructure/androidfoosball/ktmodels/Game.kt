package com.instructure.androidfoosball.ktmodels

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class Game(
        @PrimaryKey
        open var id: String = UUID.randomUUID().toString(),
        open var status: String = GameStatus.ONGOING.name,
        open var bestOf: Int = 3,
        open var rounds: RealmList<Round> = RealmList(),
        open var teamOne: Team = Team(),
        open var teamTwo: Team = Team(),
        open var startTime: Long = -1L,
        open var endTime: Long = -1L
) : RealmObject() {

    fun currentRound() = rounds.last()

    fun hasWinner(): Boolean = getWinningTeam() != null

    fun getWinningTeam(): Team? {

        val (winCountTeamOne, winCountTeamTwo) = rounds.fold(0 to 0) { counts, round ->
            val winner = round.getWinningTeam()
            when (winner) {
                teamOne -> (counts.first + 1) to counts.second
                teamTwo -> counts.first to (counts.second + 1)
                else -> counts.first to counts.second
            }
        }

        val roundsToWin = 1 + (bestOf / 2)
        return when {
            winCountTeamOne >= roundsToWin -> teamOne
            winCountTeamTwo >= roundsToWin -> teamTwo
            else -> null
        }
    }

    fun getLosingTeam(): Team? = when (getWinningTeam()) {
        teamOne -> teamTwo
        teamTwo -> teamOne
        else -> null
    }

    fun getTeamWinCount(team: Team) = rounds.count { it.getWinningTeam() == team }

    /**
     * Returns the [TableSide] that should serve the ball, based on which [Team] scored last.
     */
    fun getServingSide() = if (getLastScoringTeam() == currentRound().sideOneTeam) TableSide.SIDE_2 else TableSide.SIDE_1

    /**
     * Returns the [Team] that last scored during this game. If no team has scored yet, this function
     * returns the team with the higher FoosRank
     */
    fun getLastScoringTeam() = when {
        // If current round has goal history, return the team of the last goal
        currentRound().goalHistory.isNotEmpty() -> currentRound().goalHistory.last().team

        // If not, and there was a previous round, return the team of that round's last goal
        rounds.size > 1 -> rounds[rounds.size - 2].goalHistory.last().team

        // Otherwise, return the team with the better average win rate
        else -> listOf(teamOne, teamTwo).maxBy { it.getCompositeFoosRank() }!!
    }
}
