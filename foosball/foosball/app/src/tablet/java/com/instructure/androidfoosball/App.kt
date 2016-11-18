package com.instructure.androidfoosball

import android.content.Context
import android.content.Intent
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.instructure.androidfoosball.services.FoosballSyncService
import com.instructure.androidfoosball.utils.Commentator
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration

class App : MultiDexApplication() {

    companion object {

        lateinit var context: Context

        val realm: Realm by lazy {
            val realmConfig = RealmConfiguration.Builder(context)
                    .schemaVersion(1)
                    .migration(FoosMigration())
                    .build()
            Realm.getInstance(realmConfig)
        }

        val commentator = Commentator()
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        startService(Intent(this, FoosballSyncService::class.java))
        commentator.initialize(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}

class FoosMigration() : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        // Migrate to version 1:
        // - User:
        //    - Add 'guest' boolean
        //    - Add 'foosRanking' integer
        //    - Fix typo in 'customVictoryPhrase' field name
        if (oldVersion < 1L) {
            realm.schema.get("User")
                    .addField("guest", Boolean::class.java)
                    .addField("foosRanking", Int::class.java)
                    .renameField("customVistoryPhrase", "customVictoryPhrase")
        }

    }

}
