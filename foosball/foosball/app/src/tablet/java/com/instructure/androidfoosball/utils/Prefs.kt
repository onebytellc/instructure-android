package com.instructure.androidfoosball.utils

import com.instructure.androidfoosball.App


object Prefs : PrefManager(App.context, "com.instructure.androidfoosball") {
    var userId: String by Pref("")
    var tableId: String by Pref("")
}
