package com.instructure.androidfoosball.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.support.v7.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.BuildConfig
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.Table
import com.instructure.androidfoosball.ktmodels.User
import java.util.*

class FoosballSyncService : Service() {

    private val SERVICE_ID = 12321

    inner class FoosballSyncServiceBinder : Binder() {
        fun getService() = this@FoosballSyncService
    }

    lateinit private var mAuth: FirebaseAuth
    lateinit private var mDatabase: DatabaseReference

    private val mBinder = FoosballSyncServiceBinder()
    private val mPendingSyncs = ArrayList<(Boolean) -> Unit>()
    private var mIsFirebaseReady = false
    private var mIsFirebaseFailed = false
    private var mIsSyncing = false
    private var mIsListeningForUserChanges = false
    private var mSyncStep = 0

    override fun onBind(intent: Intent?) = mBinder

    override fun onCreate() {
        super.onCreate()
        startForeground(SERVICE_ID, NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Foosball Service Running")
                .build())

        mAuth = FirebaseAuth.getInstance()

        mAuth.signInWithEmailAndPassword(BuildConfig.FIREBASE_USERNAME, BuildConfig.FIREBASE_PASSWORD).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                mIsFirebaseFailed = true
                finishSync(false)
            } else {
                mIsFirebaseReady = true
                if (mPendingSyncs.isNotEmpty()) startSync()
            }
        }

        mDatabase = FirebaseDatabase.getInstance().reference
    }

    fun requestSync(onFinish: (Boolean) -> Unit) {
        if (mIsFirebaseFailed) {
            onFinish(false)
        } else {
            mPendingSyncs.add(onFinish)
            startSync()
        }
    }

    private fun startSync() {
        if (!mIsFirebaseReady || mIsSyncing) return
        performNextSyncStep()
    }

    private fun performNextSyncStep() {
        mIsSyncing = true
        mSyncStep++
        when (mSyncStep) {
            1 -> syncTables()
            2 -> syncUsers()
            3 -> finishSync(true)
        }
    }

    private fun syncTables() {
        mDatabase.child("tables").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tables = ArrayList<Table>()
                val tableNames = ArrayList<String>()
                for (child in dataSnapshot.children) {
                    val table = child.getValue(Table::class.java)
                    table.id = child.key
                    tables.add(table)
                    tableNames.add(table.name)
                }

                App.realm.beginTransaction()
                App.realm.copyToRealmOrUpdate(tables)
                App.realm.commitTransaction()

                performNextSyncStep()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                finishSync(false)
            }
        })
    }

    private fun syncUsers() {
        if (mIsListeningForUserChanges) {
            performNextSyncStep()
        } else {
            mDatabase.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val users = ArrayList<User>()
                    for (child in dataSnapshot.children) {
                        val user = child.getValue(User::class.java)
                        user.id = child.key
                        users.add(user)
                    }

                    App.realm.beginTransaction()
                    App.realm.copyToRealmOrUpdate(users)
                    App.realm.commitTransaction()

                    if (!mIsListeningForUserChanges) {
                        mIsListeningForUserChanges = true
                        performNextSyncStep()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    if (!mIsListeningForUserChanges) finishSync(false)
                }
            })
        }
    }

    private fun finishSync(success: Boolean) {
        mIsSyncing = false
        mSyncStep = 0
        mPendingSyncs.forEach { it(success) }
        mPendingSyncs.clear()
    }

}
