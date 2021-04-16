package com.valentinbreiz.epicture

import android.content.Context
import android.graphics.Bitmap
import okhttp3.HttpUrl
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.HashMap


data class OauthResult(
        val accessToken: String,
        val refreshToken: String,
        val expirationDate: Date,
        val accountId: String,
        val accountUsername: String
)

class Utils {
    companion object {
        fun parseQueryParameters(rawParameters: String): Map<String, String>
        {
            val keyValues = rawParameters.split("&")
            val store = HashMap<String, String>()
            for (pair in keyValues) {
                val splitKeyValue = pair.split("=")
                store.put(splitKeyValue[0], splitKeyValue[1])
            }
            return store
        }

        fun parseOauthRedirectionUrl(url: String): OauthResult
        {
            val fragmentString = HttpUrl.parse(url)!!.encodedFragment()
            fragmentString ?: throw Exception()

            val parameters = parseQueryParameters(fragmentString)
            val error = parameters["error"]

            if (error != null) {
                throw Exception()
            }

            val accessToken = parameters["access_token"]!!
            val refreshToken = parameters["refresh_token"]!!
            val accountId = parameters["account_id"]!!
            val accountUsername = parameters["account_username"]!!
            val expiresIn = (parameters["expires_in"])!!.toLong()
            val now = System.currentTimeMillis()
            val expirationDate = Date(now + expiresIn * 1000)

            return OauthResult(
                    accessToken,
                    refreshToken,
                    expirationDate,
                    accountId,
                    accountUsername
            )
        }

        fun saveParameters(context: Context, oauthResult: OauthResult)
        {
            val prefs = context.getSharedPreferences("secret", Context.MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putString("accessToken", oauthResult.accessToken)
            editor.putString("refreshToken", oauthResult.refreshToken)
            editor.putLong("expirationDate", oauthResult.expirationDate.time)
            editor.putString("accountId", oauthResult.accountId)
            editor.putString("accountUsername", oauthResult.accountUsername)

            editor.apply()
        }

        fun getParameters(context: Context) : Pair<Boolean, OauthResult>
        {
            val prefs = context.getSharedPreferences("secret", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("accessToken", "")
            val refreshToken = prefs.getString("refreshToken", "")
            val expirationDate = prefs.getLong("expirationDate", 0)
            val accountId = prefs.getString("accountId", "")
            val accountUsername = prefs.getString("accountUsername", "")
            val result = OauthResult(
                    accessToken.toString(),
                    refreshToken.toString(),
                    Date(expirationDate),
                    accountId.toString(),
                    accountUsername.toString()
            )

            return (accessToken != "") to result
        }

        fun deleteParameters(context: Context)
        {
            val prefs = context.getSharedPreferences("secret", Context.MODE_PRIVATE)
            prefs.edit().clear().apply();
        }

        fun Bitmap.convertToByteArray(): ByteArray {
            val size = this.byteCount
            val buffer = ByteBuffer.allocate(size)
            val bytes = ByteArray(size)
            this.copyPixelsToBuffer(buffer)
            buffer.rewind()
            buffer.get(bytes)
            return bytes
        }
    }
}

interface ServerCallback {
    fun onSuccess(info: AccountInfo?)
}