package com.instructure.androidfoosball.ktmodels

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class CustomTeamName(
        @PrimaryKey open var teamHash: String = "",
        open var name: String = ""
) : RealmObject() {}