package com.valentinbreiz.epicture

import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.valentinbreiz.epicture.ui.home.HomeFragment
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit


data class AccountInfo(
        var username: String,
        var bio: String,
        var avatar: String,
        var cover: String,
        var created: Long,
        var reputation: Double
)

class Imgur {
    companion object {
        val clientID = "2a131fb42fc3a80"

        var username: String = "none"

        var accesstoken: String = "none"

        fun getAccountInfo(accessToken: String, callback: ServerCallback) {
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET,
                    "https://api.imgur.com/3/account/me", null, Response.Listener { response ->
                try {
                    val data = JSONObject(response.toString())
                    val items = data.getJSONObject("data")

                    callback.onSuccess(AccountInfo(
                            items.getString("url"), items.getString("bio"),
                            items.getString("avatar"), items.getString("cover"),
                            items.getLong("created"), items.getDouble("reputation")));
                } catch (e: JSONException) {
                    Log.e(HomeFragment.TAG, "Parsing error: " + e.message)
                }
            }, Response.ErrorListener { error ->
                Log.e(HomeFragment.TAG, "Response error: " + error.message)
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${accessToken}"
                    headers["User-Agent"] = "epicture"
                    return headers
                }
            }
            AppController.instance?.addToRequestQueue(jsonObjectRequest)
        }

        fun uploadImage(file: String, image: MainActivity.ImageDescription): String {
            val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file)
                    .addFormDataPart("title", image!!.image_title)
                    .addFormDataPart("description", image.image_description)
                    .build()

            val request = Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID $clientID")
                    .header("Authorization", "Bearer $accesstoken")
                    .post(body)
                    .build()

            val clientBuilder = OkHttpClient.Builder()
            clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
            clientBuilder.readTimeout(60, TimeUnit.SECONDS)
            val client = clientBuilder.build()
            var response = client.newCall(request).execute()
            val responseBody = response.body()!!
            val jsonResponse = responseBody.string()

            val data = JSONObject(jsonResponse)
            val items = data.getJSONObject("data")
            return items.getString("link")
        }

        fun favouriteImage(imageHash: String)
        {
            Log.e(HomeFragment.TAG, "https://api.imgur.com/post/v1/posts/$imageHash/favorite")
            val client = OkHttpClient()
            val mediaType = MediaType.parse("application/octet-stream")
            val body = RequestBody.create(mediaType, "")

            val request = Request.Builder()
                    .url("https://api.imgur.com/post/v1/posts/$imageHash/favorite")
                    .header("Authorization", "Client-ID $clientID")
                    .header("Authorization", "Bearer $accesstoken")
                    .post(body)
                    .build()
            client.newCall(request).execute()
        }

        fun deleteImage(imageHash: String)
        {
            val client = OkHttpClient()

            val request = Request.Builder()
                    .url("https://api.imgur.com/3/image/$imageHash")
                    .header("Authorization", "Client-ID $clientID")
                    .header("Authorization", "Bearer $accesstoken")
                    .delete()
                    .build()
            client.newCall(request).execute()
        }
    }
}
