package com.valentinbreiz.epicture.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.valentinbreiz.epicture.R

class FullscreenFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.image_fullscreen_preview, container, false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}