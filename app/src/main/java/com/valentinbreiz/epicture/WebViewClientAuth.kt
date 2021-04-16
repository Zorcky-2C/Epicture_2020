package com.valentinbreiz.epicture

import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.valentinbreiz.epicture.ui.account.AccountFragment
import com.valentinbreiz.epicture.ui.dashboard.DashboardFragment
import com.valentinbreiz.epicture.ui.webview.WebViewFragment
import java.lang.Exception

class WebViewClientAuth(private val context: WebViewFragment, private val fragment: WebViewFragment) : WebViewClient()
{
    override fun onPageFinished(view: WebView?, url: String?)
    {
        super.onPageFinished(view, url)

        Log.e(MainActivity::class.java.simpleName, url.toString())

        val parameters = try {
            Utils.parseOauthRedirectionUrl(url!!)
        } catch (e: Exception) {
            null
        }

        if (url!!.contains("empire")) {
            if (parameters == null) {
                val authWebView = fragment.view?.findViewById<WebView>(R.id.authWebView)
                val authUrl = fragment.OAUTH_LINK

                authWebView!!.loadUrl(authUrl)
                return
            }
        }
        if (url.contains("access_token")) {
            Utils.saveParameters(context.context!!, parameters!!)

            Imgur.username = parameters.accountUsername
            Imgur.accesstoken = parameters.accessToken

            loadFragment(AccountFragment())
        }
    }

    fun loadFragment(fragment: Fragment) {
        context.activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }
}