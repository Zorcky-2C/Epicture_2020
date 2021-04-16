package com.valentinbreiz.epicture

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.util.*

class SlideshowDialogFragment : DialogFragment() {

    private val TAG = SlideshowDialogFragment::class.java.simpleName
    private var images: ArrayList<Image>? = null
    private var viewPager: ViewPager? = null
    private var myViewPagerAdapter: MyViewPagerAdapter? = null
    private var lblCount: TextView? = null
    private var lblTitle: TextView? = null
    private var lblDate: TextView? = null
    private var selectedPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v: View = inflater.inflate(R.layout.fragment_image_slider, container, false)
        viewPager = v.findViewById<View>(R.id.viewpager) as ViewPager
        lblCount = v.findViewById<View>(R.id.lbl_count) as TextView
        lblTitle = v.findViewById<View>(R.id.title) as TextView
        lblDate = v.findViewById<View>(R.id.date) as TextView
        images = arguments!!.getSerializable("images") as ArrayList<Image>?
        selectedPosition = arguments!!.getInt("position")
        Log.e(TAG, "position: $selectedPosition")
        Log.e(TAG, "images size: " + images!!.size)
        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)
        setCurrentItem(selectedPosition)
        return v
    }

    private fun setCurrentItem(position: Int) {
        viewPager!!.setCurrentItem(position, false)
        displayMetaInfo(selectedPosition)
    }

    //  page change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            displayMetaInfo(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    private fun displayMetaInfo(position: Int) {
        lblCount!!.text = (position + 1).toString() + " of " + images!!.size
        val image: Image = images!![position]
        lblTitle!!.text = image.name
        lblDate!!.text = "Views: " + image.views
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    //  adapter
    inner class MyViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val view: View = layoutInflater!!.inflate(R.layout.image_fullscreen_preview, container, false)
            val imageViewPreview = view.findViewById<View>(R.id.image_preview) as ImageView
            val image: Image = images!![position]
            Glide.with(activity).load("https://i.imgur.com/" + image.id + ".jpg")
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPreview)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return images!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    companion object {
        fun newInstance(): SlideshowDialogFragment {
            return SlideshowDialogFragment()
        }
    }
}