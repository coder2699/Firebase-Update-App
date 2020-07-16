package com.example.updatechecker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }
    lateinit var downloadController: DownloadController

    var firebaseRemoteConfig: FirebaseRemoteConfig? = null
    var version=1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder().build()

        firebaseRemoteConfig!!.setConfigSettings(configSettings)
        val defaultValue = HashMap<String, Any>()

        defaultValue["ver"] = 1.0
        defaultValue["apk_url"] = "app_apk"

        firebaseRemoteConfig!!.setDefaults(defaultValue)

btn.setOnClickListener {
    firebaseRemoteConfig!!.fetch(0)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                firebaseRemoteConfig!!.activateFetched()

                if (version != firebaseRemoteConfig!!.getDouble("ver")) {

                    val builder = AlertDialog.Builder(this)
                    //set title for alert dialog
                    builder.setTitle("Update Required")
                    //set message for alert dialog
                    builder.setMessage("A newer version of this app is available.")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                    //performing positive action
                    builder.setPositiveButton("Update") { dialogInterface, which ->
                        val apkUrl = firebaseRemoteConfig!!.getString("apk_url")
                        downloadController = DownloadController(this, apkUrl)
                        checkStoragePermission()
                    }

                    builder.setNegativeButton("Cancel") { dialogInterface, which ->
                    }

                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                }
                else{
                    Toast.makeText(applicationContext,"No new Updates available",Toast.LENGTH_LONG).show()
                }


            }

        }
}

   }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                downloadController.enqueueDownload()
            } else {
                // Permission request was denied.
                mainLayout.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // start downloading
            downloadController.enqueueDownload()
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission()
        }
    }
    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            mainLayout.showSnackbar(
                R.string.storage_access_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }
        } else {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }
    fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission)
    fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    fun AppCompatActivity.requestPermissionsCompat(
        permissionsArray: Array<String>,
        requestCode: Int
    ) {
        ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
    }


    fun View.showSnackbar(msgId: Int, length: Int) {
        showSnackbar(context.getString(msgId), length)
    }
    fun View.showSnackbar(msg: String, length: Int) {
        showSnackbar(msg, length, null, {})
    }
    fun View.showSnackbar(
        msgId: Int,
        length: Int,
        actionMessageId: Int,
        action: (View) -> Unit
    ) {
        showSnackbar(context.getString(msgId), length, context.getString(actionMessageId), action)
    }
    fun View.showSnackbar(
        msg: String,
        length: Int,
        actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(this, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(this)
            }.show()
        }
    }
}

