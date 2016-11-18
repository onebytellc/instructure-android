package com.instructure.androidfoosball.activities

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.instructure.androidfoosball.App
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.adapters.FoosRankLeaderboardAdapter
import com.instructure.androidfoosball.adapters.LeaderboardAdapter
import com.instructure.androidfoosball.adapters.TeamLeaderboardAdapter
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.ktmodels.CustomTeam
import com.instructure.androidfoosball.utils.sortByFoosRanking
import com.instructure.androidfoosball.utils.sortByWinRatio
import com.instructure.androidfoosball.utils.sortCustomTeamByWinRatio
import kotlinx.android.synthetic.tablet.activity_leaderboard.*
import org.jetbrains.anko.onClick
import java.util.*



class LeaderboardActivity : AppCompatActivity() {

    companion object {
        const val MIN_GAMES_FOR_RANKING = 9
        const val MIN_GAMES_FOR_TEAM_RANKING = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        leaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(MIN_GAMES_FOR_RANKING)

        teamLeaderboardSubtitle.text = getString(R.string.leaderboard_subtitle).format(LeaderboardActivity.MIN_GAMES_FOR_TEAM_RANKING)

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()

        loadIndividualLeaderboard()

        setupListeners()
    }

    private fun loadIndividualLeaderboard() {

        unselectAll()
        selectIndividual()

        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedUsers = App.realm.where(User::class.java).equalTo("guest", false).findAll().sortByWinRatio(MIN_GAMES_FOR_RANKING)
                recyclerView.adapter = LeaderboardAdapter(this@LeaderboardActivity, sortedUsers)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun selectIndividual() {
        leaderboardSelected.visibility = View.VISIBLE
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)

    }

    private fun unselectIndividual() {
        leaderboardSelected.visibility = View.GONE
        leaderboardText.typeface = Typeface.create(leaderboardText.typeface, Typeface.DEFAULT.style)
        leaderboardSubtitle.typeface = Typeface.create(leaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectTeam() {
        teamLeaderboardSelected.visibility = View.VISIBLE
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectTeam() {
        teamLeaderboardSelected.visibility = View.GONE
        teamLeaderboardText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        teamLeaderboardSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun selectFoosRank() {
        foosRankSelected.visibility = View.VISIBLE
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT_BOLD.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT_BOLD.style)
    }

    private fun unselectFoosRank() {
        foosRankSelected.visibility = View.GONE
        foosRankText.typeface = Typeface.create(teamLeaderboardText.typeface, Typeface.DEFAULT.style)
        foosRankSubtitle.typeface = Typeface.create(teamLeaderboardSubtitle.typeface, Typeface.DEFAULT.style)
    }

    private fun unselectAll() {
        unselectFoosRank()
        unselectIndividual()
        unselectTeam()
    }

    private fun loadTeamLeaderboard() {

        unselectAll()
        selectTeam()

        FirebaseDatabase.getInstance().reference.child("customTeams").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val teams = ArrayList<CustomTeam>()

                for (child in dataSnapshot.children) {
                    val team = child.getValue(CustomTeam::class.java)
                    teams.add(team)
                }

                FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users = java.util.HashMap<String, User>()

                        for (child in dataSnapshot.children) {
                            val user = child.getValue(User::class.java)
                            users.put(user.id, user)
                        }
                        recyclerView.adapter = TeamLeaderboardAdapter(this@LeaderboardActivity, teams.sortCustomTeamByWinRatio(MIN_GAMES_FOR_TEAM_RANKING), users)

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })


            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun loadFoosRankLeaderboard() {
        unselectAll()
        selectFoosRank()

        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortedUsers = dataSnapshot.children.map { it.getValue(User::class.java) }.filter { !it.guest }.sortByFoosRanking()
                recyclerView.adapter = FoosRankLeaderboardAdapter(this@LeaderboardActivity, sortedUsers) {
                    startActivity(EloDialogActivity.createIntent(this@LeaderboardActivity, it.foosRankMap))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    private fun setupListeners() {

        leaderboardWrapper.setOnClickListener { loadIndividualLeaderboard() }

        teamLeaderboardWrapper.setOnClickListener { loadTeamLeaderboard() }

        foosRankWrapper.onClick { loadFoosRankLeaderboard() }
    }
}
