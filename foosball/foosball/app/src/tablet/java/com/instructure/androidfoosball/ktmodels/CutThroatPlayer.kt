package com.instructure.androidfoosball.ktmodels

import io.realm.RealmObject

open class CutThroatPlayer(
        open var user: User = User(),
        open var score: Int = 0
) : RealmObject() {}
