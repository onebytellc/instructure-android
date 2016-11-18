package com.instructure.androidfoosball.ktmodels

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*


open class User(
        @PrimaryKey
        open var id: String = "",
        open var name: String = "",
        open var email: String = "",
        open var avatar: String = "",
        open var pinHash: String = "",
        open var pinDisabled: String = "",
        open var guest: Boolean = false,
        open var customAssignmentPhrase: String = "",
        open var customVictoryPhrase: String = "",
        open var foosRanking: Int = 0,
        open var rankedGamesPlayed: Int = 0,
        @Ignore
        open var foosRankMap: HashMap<String, Int> = HashMap<String, Int>(),
        open var wins: Int = 0,
        open var losses: Int = 0
) : RealmObject() {}
