package com.instructure.androidfoosball.ktmodels

import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.utils.Prefs
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Table(
        @PrimaryKey
        open var id: String = "",
        open var name: String = "",
        open var sideOneColor: String = "",
        open var sideTwoColor: String = "",
        open var sideOneName: String = "",
        open var sideTwoName: String = "",
        open var currentGame: String = "",
        open var currentGameInfo: String = "",
        open var pushId: String = ""

) : RealmObject() {
    companion object {

        /**
         * Returns the selected [Table], specified by `tableId` in [Prefs]
         */
        fun getSelectedTable(): Table = App.realm.where(Table::class.java).equalTo("id", Prefs.tableId).findFirst()

        /**
         * Returns `true` if there are any ongoing games
         */
        fun hasOngoingGames() = App.realm.where(Game::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0
            || App.realm.where(CutThroatGame::class.java).equalTo("status", GameStatus.ONGOING.name).count() > 0

        /**
         * Returns a [List] of ongoing [Game]s
         */
        fun getOngoingGames(): List<Any> = App.realm.where(Game::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList() + App.realm.where(CutThroatGame::class.java).equalTo("status", GameStatus.ONGOING.name).findAll().toList()
    }
}
