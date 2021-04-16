package com.valentinbreiz.epicture.ui.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.valentinbreiz.epicture.Imgur
import com.valentinbreiz.epicture.R
import com.valentinbreiz.epicture.WebViewClientAuth

class WebViewFragment : Fragment() {

    private var dashboardViewModel: WebViewViewModel? = null
    val OAUTH_LINK = "https://api.imgur.com/oauth2/authorize?client_id=${Imgur.clientID}&response_type=token"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dashboardViewModel = ViewModelProviders.of(this).get(WebViewViewModel::class.java)
        val root = inflater.inflate(R.layout.webview_fragment, container, false)

        val authWebView: WebView = root.findViewById(R.id.authWebView)

        authWebView.settings.javaScriptEnabled = true
        authWebView.webViewClient = WebViewClientAuth(this@WebViewFragment, this)
        authWebView.loadUrl(OAUTH_LINK)

        return root
    }
}