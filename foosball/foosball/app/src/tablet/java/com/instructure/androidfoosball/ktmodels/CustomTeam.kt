package com.instructure.androidfoosball.ktmodels

import java.util.*


open class CustomTeam(
        open var id: String = "",
        open var teamName: String = "",
        open var teamWins: Long = 0,
        open var teamLosses: Long = 0,
        open var users: List<String> = ArrayList()
)