package com.instructure.androidfoosball.push

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.util.Log
import com.instructure.androidfoosball.BuildConfig
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject


class PushIntentService: IntentService("PushIntentService") {

    private val JSON = MediaType.parse("application/json; charset=utf-8")
    private val FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send"
    private val mClient = OkHttpClient()

    companion object {

        val PUSH_ID = "push_id"
        val TABLE_NAME = "table_name"

        fun getIntent(context: Context, pushId: String, tableName: String) = Intent(context, PushIntentService::class.java).apply {
            putExtra(PUSH_ID, pushId)
            putExtra(TABLE_NAME, tableName)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        try {

            val pushId = intent?.getStringExtra(PUSH_ID)
            val tableName = intent?.getStringExtra(TABLE_NAME)

            val bodyString = JSONObject(
                    "{\"to\": \"/topics/"+ pushId + "\", \"data\": {\"message\": \"" + tableName + "\" }}"
            )

            Log.d("push", "json: " + bodyString.toString())

            val body = RequestBody.create(JSON, bodyString.toString())
            val request = Request.Builder().url(FCM_MESSAGE_URL).post(body)
                    .addHeader("Authorization", "key=" + BuildConfig.FIREBASE_SERVER_KEY).build()
            val response = mClient.newCall(request).execute()
            val resultJson = JSONObject(response.body().string())

            val success = resultJson.getInt("success")
            val failure = resultJson.getInt("failure")

            Log.d("push", "Success: " + success)
            Log.d("push", "Failure: " + failure)
        } catch (e: Exception) {
            Log.d("push", "Error: " + e)
        }

    }
}