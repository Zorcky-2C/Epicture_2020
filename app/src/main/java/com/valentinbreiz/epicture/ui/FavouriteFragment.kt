package com.valentinbreiz.epicture.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.valentinbreiz.epicture.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class FavouriteFragment : Fragment() {

    val TAG = MainActivity::class.java.simpleName
    var images: ArrayList<Image>? = null
    var pDialog: ProgressDialog? = null
    var mAdapter: GalleryAdapter? = null
    var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_favourite, container, false)

        recyclerView = root.findViewById<View>(R.id.recycler_view_favourite) as RecyclerView
        pDialog = ProgressDialog(activity)
        images = ArrayList()
        mAdapter = GalleryAdapter(activity!!, images!!)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(activity, 2)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter

        val pullToRefresh: SwipeRefreshLayout = root.findViewById(R.id.swiperefreshfavourite)
        pullToRefresh.setOnRefreshListener {
            fetchData()
            pullToRefresh.isRefreshing = false
        }

        recyclerView!!.addOnItemTouchListener(GalleryAdapter.RecyclerTouchListener(root.context, recyclerView!!, object : GalleryAdapter.ClickListener {
            override fun onClick(view: View?, position: Int) {
                val bundle = Bundle()
                bundle.putSerializable("images", images)
                bundle.putInt("position", position)
                val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
                val newFragment = SlideshowDialogFragment.newInstance()
                newFragment.arguments = bundle
                newFragment.show(ft, "slideshow")
            }

            override fun onLongClick(view: View?, position: Int) {
                AsyncAction({
                    return@AsyncAction Imgur.favouriteImage(images!![position].real_id!!)
                }, {
                    Toast.makeText(root.context, "Image removed from favorites!", Toast.LENGTH_SHORT).show()
                    fetchData()
                })
            }
        }))

        fetchData()
        
        return root
    }

    fun fetchData() {
        pDialog!!.setMessage("Loading images...")
        pDialog!!.show()
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, "https://api.imgur.com/3/account/${Imgur.username}/gallery_favorites", null, Response.Listener { response ->
            Log.e(TAG, "got $response")
            pDialog!!.hide()
            images!!.clear()
            try {
                val data = JSONObject(response.toString())
                val items = data.getJSONArray("data")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val photo = Image()
                    if (item.getBoolean("is_album")) {
                        photo.id = item.getString("cover")
                    } else {
                        photo.id = item.getString("id")
                    }
                    photo.real_id = item.getString("id")
                    photo.name = item.getString("title")
                    photo.views = item.getString("views")
                    images!!.add(photo) // Add photo to list
                    Log.e(TAG, photo.name + " added with id " + photo.id)
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Error: " + e.message)
                pDialog!!.hide()
            }
            mAdapter!!.notifyDataSetChanged()
        }, Response.ErrorListener { error ->
            Log.e(TAG, "Error: " + error.message)
            pDialog!!.hide()
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${Imgur.accesstoken}"
                headers["User-Agent"] = "epicture"
                return headers
            }
        }

        // Adding request to request queue
        AppController.instance?.addToRequestQueue(jsonObjectRequest)
    }
}