package com.instructure.androidfoosball.ktmodels

import io.realm.RealmObject

open class Goal (
        open var team: Team = Team(),
        open var time: Long = System.currentTimeMillis()
) : RealmObject(){ }