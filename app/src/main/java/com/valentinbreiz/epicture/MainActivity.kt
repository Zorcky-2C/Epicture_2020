package com.valentinbreiz.epicture

import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.valentinbreiz.epicture.ui.home.HomeFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var loadingView: AlertDialog
    var imagePicker: ImagePicker = ImagePicker(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        // Loading View
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.loading_dialog)
        loadingView = builder.create()

        var fab = this.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            if (Imgur.username == "none") {
                Toast.makeText(this, "Please connect to upload an image!", Toast.LENGTH_SHORT).show()
            }
            else {
                UploadImage()
            }
        }

        val (loggedin, _) = Utils.getParameters(this)

        if (loggedin) {
            Imgur.username = Utils.getParameters(this).second.accountUsername
            Imgur.accesstoken = Utils.getParameters(this).second.accessToken
        }
    }

    private fun UploadImage() {
        val options = arrayOf<CharSequence>("Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upload Image")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Choose from Gallery") {
                imagePicker.askPermissions()
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    data class ImageDescription (
        val image_title: String,
        val image_description: String,
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val results: Bitmap? = imagePicker.onActivityResult(requestCode, resultCode, data)

        if (results != null) {

            //make dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Name")
            val customLayout: View = layoutInflater.inflate(R.layout.custom_upload_layout, null)
            builder.setView(customLayout)
            builder.setPositiveButton("OK") { dialog, which ->

                loadingView.show()

                val editText = customLayout.findViewById<EditText>(R.id.editTextTextPersonName)
                val editText2 = customLayout.findViewById<EditText>(R.id.editTextTextMultiLine)

                var image = ImageDescription(editText.text.toString(), editText2.text.toString())

                getBase64Image(results, complete = { base64Image ->
                    AsyncAction({
                        return@AsyncAction Imgur.uploadImage(base64Image, image)
                    }, {
                        loadingView.dismiss()
                        Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show()
                    })
                })

            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun getBase64Image(image: Bitmap, complete: (String) -> Unit) {
        GlobalScope.launch {
            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val b = outputStream.toByteArray()
            complete(Base64.encodeToString(b, Base64.DEFAULT))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        search = menu.findItem(R.id.search)
        filter = menu.findItem(R.id.filter)
        disconnect = menu.findItem(R.id.disconnect)
        val searchView = search!!.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(tmp: String): Boolean {
                MainActivity.query = tmp
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/{{sort}}/{{window}}/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == "") {
                    HomeFragment.endpoint = "https://api.imgur.com/3/gallery/user/rising/0.json"
                    HomeFragment.fetchData()
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.latest -> {
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/time/{{window}}/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                true
            }
            R.id.viral ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/viral/{{window}}/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            R.id.day ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/top/day/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            R.id.week ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/top/week/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            R.id.month ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/top/month/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            R.id.year ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/top/year/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            R.id.all ->{
                HomeFragment.endpoint = "https://api.imgur.com/3/gallery/search/top/all/{{page}}?q=" + MainActivity.query
                HomeFragment.fetchData()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        var query: String? = null

        var search: MenuItem? = null
        var filter: MenuItem? = null
        var disconnect: MenuItem? = null
    }
}

class AsyncAction<R>(val asyncRunnable: () -> R, val resultHandler: (R) -> Unit): AsyncTask<Unit, Void, R>() {
    companion object {
        val pool: ExecutorService = Executors.newCachedThreadPool()
    }

    init {
        exec()
    }

    override fun doInBackground(vararg params: Unit): R = asyncRunnable()
    override fun onPostExecute(result: R) = resultHandler(result)
    private fun exec() = super.executeOnExecutor(pool)
}