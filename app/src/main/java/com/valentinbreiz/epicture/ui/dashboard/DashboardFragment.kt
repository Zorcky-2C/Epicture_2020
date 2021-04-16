package com.valentinbreiz.epicture.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.valentinbreiz.epicture.*
import com.valentinbreiz.epicture.ui.account.AccountFragment
import com.valentinbreiz.epicture.ui.webview.WebViewFragment

class DashboardFragment : Fragment() {

    private var dashboardViewModel: DashboardViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        MainActivity.filter!!.isVisible = false
        MainActivity.search!!.isVisible = false

        val (loggedin, _) = Utils.getParameters(this.context!!)

        if (loggedin) {
            loadFragment(AccountFragment())
        }
        else {
            loadFragment(WebViewFragment())
        }

        return root
    }

    fun loadFragment(fragment: Fragment) {
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }
}