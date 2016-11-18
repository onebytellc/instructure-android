package com.instructure.androidfoosball.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.instructure.androidfoosball.ktmodels.TableSide

class GoalReceiver(val onGoal: (TableSide) -> Unit) : BroadcastReceiver() {

    companion object {
        val GOAL_ACTION = "action_goal"
        val EXTRA_SIDE = "scoringSide"

        fun getGoalIntent(side: TableSide) = Intent(GOAL_ACTION).apply {
            putExtra(EXTRA_SIDE, side.name)
        }

        fun sendGoal(context: Context, side: TableSide) {
            context.sendOrderedBroadcast(getGoalIntent(side), null)
        }

        fun register(context: Context, receiver: GoalReceiver, priority: Int = 0) {
            val filter = IntentFilter(GOAL_ACTION)
            filter.priority = priority
            context.registerReceiver(receiver, filter)
        }

        fun unregister(context: Context, receiver: GoalReceiver) {
            context.unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == GOAL_ACTION) {
            val sideString = intent.getStringExtra(EXTRA_SIDE)
            onGoal(TableSide.valueOf(sideString))
            abortBroadcast()
        }
    }
}
