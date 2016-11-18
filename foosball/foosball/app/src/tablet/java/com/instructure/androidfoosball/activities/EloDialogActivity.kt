package com.instructure.androidfoosball.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.instructure.androidfoosball.R
import kotlinx.android.synthetic.tablet.activity_elo_layout.*
import java.util.*

class EloDialogActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context, data: HashMap<String, Int>) : Intent {
            val intent = Intent(context, EloDialogActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("HashMap", data)
            intent.putExtras(bundle)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elo_layout)

        val dataSet: HashMap<String, Int> = intent.extras.get("HashMap") as HashMap<String, Int>

        if(dataSet.isEmpty()) {
            foosRankView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            foosRankView.setData(dataSet)
            foosRankView.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
    }
}
