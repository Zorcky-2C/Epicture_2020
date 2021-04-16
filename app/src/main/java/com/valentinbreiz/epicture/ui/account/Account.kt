package com.valentinbreiz.epicture.ui.account

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.material.tabs.TabLayout
import com.valentinbreiz.epicture.*
import com.valentinbreiz.epicture.ui.home.HomeFragment
import com.valentinbreiz.epicture.ui.main.SectionsPagerAdapter
import com.valentinbreiz.epicture.ui.webview.WebViewFragment


class AccountFragment : Fragment() {

    private var dashboardViewModel: AccountViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dashboardViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.account_fragment, container, false)

        Log.e(HomeFragment.TAG, "accessToken= " + Imgur.accesstoken)

        val sectionsPagerAdapter = SectionsPagerAdapter(this.context!!, childFragmentManager!!)
        val viewPager: ViewPager = root.findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = root.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        MainActivity.disconnect!!.isVisible = true

        MainActivity.disconnect!!.setOnMenuItemClickListener {
            Utils.deleteParameters(root.context)
            Imgur.username = "none"
            Imgur.accesstoken = "none"
            loadFragment(WebViewFragment())
            Toast.makeText(root.context, "Disconnected!", Toast.LENGTH_SHORT).show()
            true
        }

        val parameters = Utils.getParameters(this.context!!)

        Imgur.getAccountInfo(parameters.second.accessToken, object : ServerCallback {
            override fun onSuccess(info: AccountInfo?) {
                val text: TextView = root.findViewById(R.id.textview_account) as TextView
                text.text = info!!.username;

                val text1: TextView = root.findViewById(R.id.textview_accountid) as TextView
                text1.text = "reputation: " + info.reputation;

                val profilePicture: ImageView = root.findViewById(R.id.profile_picture) as ImageView
                Glide.with(root.context).load(info.avatar).into(profilePicture)

                val cover: LinearLayout = root.findViewById(R.id.cover) as LinearLayout

                Glide.with(root.context).load(info.cover).asBitmap().into(object : SimpleTarget<Bitmap?>(992, 558) {
                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap?>?) {
                        val drawable: Drawable = BitmapDrawable(context!!.resources, resource)
                        cover.background = drawable
                    }
                })
            }
        })

        return root
    }

    fun loadFragment(fragment: Fragment) {
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }
}