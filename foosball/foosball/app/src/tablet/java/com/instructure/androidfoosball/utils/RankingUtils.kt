package com.instructure.androidfoosball.utils

import com.instructure.androidfoosball.ktmodels.User
import java.util.*

object RankingUtils {


    const val DEFAULT_FOOS_RANK = 1500
    const val FOOS_RANK_FLOOR = 1000
    const val FOOS_RANK_CEILING = 3000

    fun updateFoosRankings(winners: List<User>, losers: List<User>) {
        // NOTE: Any number of users on either team could be guest users (user.guest == true)
        if ((winners + losers).any { it.guest }) return
        //Check for non-initialized foosranks

        for (user in (winners + losers)) {
            if(user.foosRanking < FOOS_RANK_FLOOR){
                user.foosRanking = DEFAULT_FOOS_RANK
            }
        }

        val winnersFoosRank = winners.sumBy { it.foosRanking } / winners.size
        val losersFoosRank = losers.sumBy { it.foosRanking } / losers.size

        //If not, we must calculate the FoosRank changes for each player starting with the winners
        //each player is compared to the composite score of the opposing team

        for(user in winners) {
            var foosRank = user.foosRanking + (32*(1 - (1 / (1 + Math.pow(10.0, (losersFoosRank - user.foosRanking.toDouble()) / 400))))).toInt()
            //Prevent the player from breaking through the foosrank ceiling
            if(foosRank > FOOS_RANK_CEILING) {
                foosRank = FOOS_RANK_CEILING
                user.foosRanking = foosRank
            } else {
                user.foosRanking = foosRank
            }
            user.rankedGamesPlayed++
            //Update rank map with today's date
            user.foosRankMap.put(Date().time.toString(), user.foosRanking)
        }

        for(user in losers) {
            var foosRank = user.foosRanking + (32*(0 - (1 / (1 + Math.pow(10.0, (winnersFoosRank - user.foosRanking.toDouble()) / 400))))).toInt()
            //Prevent the player from dropping below the foosrank floor
            if(foosRank < FOOS_RANK_FLOOR) {
                foosRank = FOOS_RANK_FLOOR
                user.foosRanking = foosRank
            } else {
                user.foosRanking = foosRank
            }
            user.rankedGamesPlayed++
            //Update rank map with today's date
            user.foosRankMap.put(Date().time.toString(), user.foosRanking)
        }
    }
}
