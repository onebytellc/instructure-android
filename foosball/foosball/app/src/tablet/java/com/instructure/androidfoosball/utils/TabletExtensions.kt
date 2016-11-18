@file:Suppress("unused")

package com.instructure.androidfoosball.utils

import android.app.Activity
import android.util.TypedValue
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.ktmodels.CustomTeam
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import org.jetbrains.anko.displayMetrics
import java.util.*
import kotlin.comparisons.compareBy

inline fun <T : RealmObject> T.edit(block: T.(realm: Realm) -> Unit) = App.realm.inTransaction { block(this) }

inline fun <T : RealmList<*>> T.edit(block: T.(realm: Realm) -> Unit) = App.realm.inTransaction { block(this) }

inline fun Realm.inTransaction(block: Realm.(realm: Realm) -> Unit) {
    beginTransaction()
    block(this)
    commitTransaction()
}

fun <T : RealmObject> T.copyToRealmOrUpdate() = edit { it.copyToRealmOrUpdate(this) }

val Activity.mCommentator: Commentator get() = App.commentator

fun Float.dp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, App.context.displayMetrics)

fun <T> List<T>.shift(offset: Int): List<T> = if (offset == 0) this else subList(offset, size) + subList(0, offset)

fun <T> List<T>.split(vararg splitSizes: Int): List<List<T>> {
    if (splitSizes.sum() > size) throw IndexOutOfBoundsException("Sum of requested split sizes is larger than source list size")
    val list = ArrayList<List<T>>()
    var idx = 0
    splitSizes.forEach {
        list.add(subList(idx, idx + it))
        idx += it
    }
    list.add(subList(idx, size))
    return list
}


fun CustomTeam.getWinRate(minGamesRequired: Int) = when {
    teamWins + teamLosses < minGamesRequired -> -1f
    teamWins == 0L -> 0f
    teamLosses == 0L -> 100f
    else -> 100f * teamWins / (teamWins + teamLosses)
}

fun CustomTeam.getTeamHash() : String {
    return users.sortedBy { it }.fold("") { hash, user -> hash + user }
}

fun List<CustomTeam>.sortCustomTeamByWinRatio(minGamesRequired: Int)
        = sortedWith(compareBy({ -it.getWinRate(minGamesRequired) }, { -it.teamWins }, { it.teamLosses } ))
