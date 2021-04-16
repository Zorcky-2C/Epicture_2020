package com.valentinbreiz.epicture.ui.home

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.valentinbreiz.epicture.*
import com.valentinbreiz.epicture.GalleryAdapter.ClickListener
import com.valentinbreiz.epicture.GalleryAdapter.RecyclerTouchListener
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HomeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        //final TextView textView = root.findViewById(R.id.text_home);
        recyclerView = root.findViewById<View>(R.id.recycler_view) as RecyclerView
        pDialog = ProgressDialog(activity)
        images = ArrayList()
        mAdapter = GalleryAdapter(requireActivity(), images!!)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(activity, 2)
        recyclerView!!.layoutManager = mLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = mAdapter

        if (MainActivity.filter != null && MainActivity.search != null && MainActivity.disconnect != null) {
            MainActivity.filter!!.isVisible = true
            MainActivity.search!!.isVisible = true
            MainActivity.disconnect!!.isVisible = false
        }

        val pullToRefresh: SwipeRefreshLayout = root.findViewById(R.id.swiperefresh)
        pullToRefresh.setOnRefreshListener {
            fetchData()
            pullToRefresh.isRefreshing = false
        }

        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(root.context, recyclerView!!, object : ClickListener {
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
                    Toast.makeText(root.context, "Image added to favorites!", Toast.LENGTH_SHORT).show()
                })
            }
        }))

        fetchData()

        return root
    }

    companion object {

        var endpoint = "https://api.imgur.com/3/gallery/user/rising/0.json"

        val TAG = MainActivity::class.java.simpleName
        var images: ArrayList<Image>? = null
        var pDialog: ProgressDialog? = null
        var mAdapter: GalleryAdapter? = null
        var recyclerView: RecyclerView? = null

        fun fetchData() {
            pDialog!!.setMessage("Loading images...")
            pDialog!!.show()
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, endpoint, null, Response.Listener { response ->
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
                        images!!.add(photo)
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
                    headers["Authorization"] = "Client-ID ${Imgur.clientID}"
                    headers["User-Agent"] = "epicture"
                    return headers
                }
            }

            // Adding request to request queue
            AppController.instance?.addToRequestQueue(jsonObjectRequest)
        }

    }
}