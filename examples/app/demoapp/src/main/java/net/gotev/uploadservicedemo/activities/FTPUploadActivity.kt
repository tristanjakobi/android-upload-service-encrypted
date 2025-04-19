package com.levin.uploadservicedemo.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.levin.recycleradapter.RecyclerAdapter
import com.levin.uploadservice.ftp.FTPUploadRequest
import com.levin.uploadservicedemo.R
import com.levin.uploadservicedemo.adapteritems.EmptyItem
import com.levin.uploadservicedemo.adapteritems.UploadItem
import com.levin.uploadservicedemo.dialogs.AddFileParameterNameDialog
import com.levin.uploadservicedemo.extensions.isValidIPorHostname
import com.levin.uploadservicedemo.extensions.openBrowser
import com.levin.uploadservicedemo.utils.UploadItemUtils
import com.levin.uploadservicedemo.utils.UploadItemUtils.ForEachDelegate
import com.levin.uploadservicedemo.views.AddItem
import java.io.IOException

class FTPUploadActivity : FilePickerActivity() {

    companion object {
        fun show(activity: BaseActivity) {
            activity.startActivity(Intent(activity, FTPUploadActivity::class.java))
        }
    }

    fun onInfo() {
        openBrowser("https://github.com/gotev/android-upload-service/blob/master/uploadservice-ftp/README.md")
    }

    val serverUrl: EditText by lazy { findViewById(R.id.server_url) }
    val serverPort: EditText by lazy { findViewById(R.id.server_port) }
    val ftpUsername: EditText by lazy { findViewById(R.id.ftp_username) }
    val ftpPassword: EditText by lazy { findViewById(R.id.ftp_password) }
    val requestItems: RecyclerView by lazy { findViewById(R.id.request_items) }

    private val uploadItemsAdapter = RecyclerAdapter()
    private val uploadItemUtils = UploadItemUtils(uploadItemsAdapter)

    private var remotePath: String? = null
    private val addFTPFile: AddFileParameterNameDialog by lazy {
        AddFileParameterNameDialog(
            context = this,
            hint = R.string.file_remote_path_hint,
            errorMessage = R.string.provide_remote_path,
            detailsMessage = R.string.provide_remote_path_next_instructions,
            delegate = { value ->
                remotePath = value
                openFilePicker()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_ftp)

        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        requestItems.apply {
            layoutManager =
                LinearLayoutManager(this@FTPUploadActivity, RecyclerView.VERTICAL, false)
            adapter = uploadItemsAdapter
        }

        uploadItemsAdapter.setEmptyItem(EmptyItem(getString(R.string.empty_ftp_upload)))

        findViewById<AddItem>(R.id.add_file).setOnClickListener {
            addFTPFile.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_upload, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }

            R.id.settings -> return true

            R.id.info -> {
                onInfo()
                return true
            }

            R.id.done -> {
                onDone()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        addFTPFile.hide()
    }

    override fun onPickedFiles(pickedFiles: List<String>) {
        val path = remotePath
        if (path.isNullOrBlank()) return
        uploadItemUtils.addFile(path, pickedFiles.first())
    }

    fun onDone() {
        if (!serverUrl.text.toString().isValidIPorHostname()) {
            serverUrl.error = getString(R.string.provide_valid_host)
            return
        }

        val ftpPort: Int
        try {
            ftpPort = serverPort.text.toString().toInt(10)
        } catch (exc: Exception) {
            serverPort.error = getString(R.string.provide_valid_port)
            return
        }

        try {
            val request = FTPUploadRequest(this, serverUrl.text.toString(), ftpPort)
                .setNotificationConfig { _: Context, uploadId: String ->
                    getNotificationConfig(uploadId, R.string.ftp_upload)
                }
                .setUsernameAndPassword(ftpUsername.text.toString(), ftpPassword.text.toString())

            uploadItemUtils.forEach(object : ForEachDelegate {
                override fun onHeader(item: UploadItem) {
                    // FTP does not support headers
                }

                override fun onParameter(item: UploadItem) {
                    // FTP does not support parameters
                }

                override fun onFile(item: UploadItem) {
                    try {
                        request.addFileToUpload(item.subtitle, item.title)
                    } catch (exc: IOException) {
                        Toast.makeText(
                            this@FTPUploadActivity,
                            getString(R.string.file_not_found, item.subtitle),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
            request.startUpload()
            finish()
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }
}
