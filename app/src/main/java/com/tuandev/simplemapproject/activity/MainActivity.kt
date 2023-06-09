package com.tuandev.simplemapproject.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.base.map.BaseMapFragment
import com.tuandev.simplemapproject.extension.compressBitmap
import com.tuandev.simplemapproject.extension.compressBitmapFromUri
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.SplashFragment
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.RouteDetailFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import com.tuandev.simplemapproject.ui.splash.toolMap.ToolMapFragment
import com.tuandev.simplemapproject.widget.ConfirmMessageDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        const val PICK_IMAGE_REQUEST = "PICK_IMAGE_REQUEST"
        const val TAKE_PHOTO_REQUEST = "TAKE_PHOTO_REQUEST"
        const val PERMISSIONS_REQUEST_SYSTEM_LOCATION = 111
    }

    var onActivityBackPressListener = {}
    private var onPermissionGranted: () -> Unit = {}
    var onImageFromResultReady: (ByteArray) -> Unit = {}
    private var imagePath: String? = null
    private var requestCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListener()
    }

    private fun initView() {
        openSplashFragment()
    }

    private fun initListener() {
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val count = supportFragmentManager.backStackEntryCount
                if (count > 1) {

                    supportFragmentManager.fragments.last().run {
                        if (tag == "SupportLifecycleFragmentImpl") {
                            supportFragmentManager.commit {
                                remove(this@run)
                            }
                        }
                    }

                    when (val currentFragment = supportFragmentManager.fragments.last()) {
                        is SuggestFragment -> {
                            when (val childSuggestFragment =
                                currentFragment.childFragmentManager.fragments.last()) {
                                is BaseMapFragment -> {
                                    supportFragmentManager.popBackStack()
                                }
                                is FeatureQuestionFragment -> {
                                    childSuggestFragment.handleChildFragmentBackPress()
                                }
                                is RouteDetailFragment -> {
                                    currentFragment.run {
                                        handleUpdateRouteFromBackPress()
                                        handleChildFragmentBackPress()
                                    }
                                }
                                else -> {
                                    currentFragment.handleChildFragmentBackPress()
                                }
                            }
                        }
                        is SplashFragment, is ToolMapFragment -> {
                            supportFragmentManager.popBackStack()
                        }
                    }
                } else {
                    finish()
                }
            }
        })
    }

    private fun Fragment.handleChildFragmentBackPress() {
        val count = childFragmentManager.backStackEntryCount
        if (count > 1) {
            childFragmentManager.popBackStack()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun getContainerId() = R.id.main_activity_container

    fun openSuggestRouteFragment() {
        checkLocationPermission {
            openFragment(
                containerId = getContainerId(),
                fragment = SuggestFragment.newInstance()
            )
        }
    }

    fun openSplashFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = SplashFragment.newInstance()
        )
    }

    fun openToolMapFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = ToolMapFragment.newInstance()
        )
    }

    private fun checkUserPermission(permission: String, onGranted: () -> Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, permission) -> {
                onGranted()
            }
            else -> {
                onPermissionGranted = { onGranted() }
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    private fun checkAppLocationPermission(action: () -> Unit) {
        val locationPermissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val requiredList = locationPermissions.filterNot {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }


        if (requiredList.isNotEmpty()) {
            onPermissionGranted = {
                action()
            }
            requestMultiplePermissionsLauncher.launch(requiredList.toTypedArray())
        } else {
            action()
        }
    }

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val isAllGranted = grantedMap.run { if (isNotEmpty()) all { it.value } else false }
        if (isAllGranted) {
            onPermissionGranted()
        }
    }

    private var resultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    when (requestCode) {
                        PICK_IMAGE_REQUEST -> {
                            handleOnPickPhotoResult(intent)
                        }
                        TAKE_PHOTO_REQUEST -> {
                            handleOnTakePhotoResult()
                        }
                    }
                }
            }
        }

    @SuppressLint("IntentReset")
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun takePhotoFromCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            val photoFile = createImageFile()
            photoFile.also {
                val photoURI: Uri =
                    FileProvider.getUriForFile(this, "com.example.android.fileprovider", it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultLauncher.launch(takePictureIntent)
            }
        }
    }

    fun handleTakePhoto(action: (ByteArray) -> Unit) {
        checkUserPermission(Manifest.permission.CAMERA) {
            requestCode = TAKE_PHOTO_REQUEST
            takePhotoFromCamera()
            onImageFromResultReady = action
        }
    }

    fun handleGetPhotoFromGallery(action: (ByteArray) -> Unit) {
        requestCode = PICK_IMAGE_REQUEST
        onImageFromResultReady = action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            pickImageFromGallery()
        } else {
            checkUserPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                pickImageFromGallery()
            }
        }
    }

    private fun handleOnPickPhotoResult(intent: Intent) {
        intent.data?.let { uri ->
            ByteArrayOutputStream().let { os ->
                compressBitmapFromUri(this, uri).compress(Bitmap.CompressFormat.JPEG, 100, os)
                onImageFromResultReady(os.toByteArray())
            }
        }
    }

    fun invokeBackPress() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun handleOnTakePhotoResult() {
        var bitmap = BitmapFactory.decodeFile(imagePath)
        val orientation = ExifInterface(imagePath.toString()).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        bitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
        ByteArrayOutputStream().let { os ->
            compressBitmap(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, os)
            onImageFromResultReady(os.toByteArray())
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${System.currentTimeMillis()}", ".jpg", storageDir).apply {
            imagePath = absolutePath
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun showConfirmDialog(
        title: String,
        content: String,
        action: () -> Unit
    ) {
        ConfirmMessageDialog(
            title = title,
            message = content
        ).apply {
            positiveAction = {
                action()
            }
        }.show(supportFragmentManager, null)
    }

    fun checkLocationPermission(action: () -> Unit) {
        if (!isSystemLocationPermissionEnable()) {
            ConfirmMessageDialog(
                title = "Message",
                message = "Your device's location is current off. Enable it",
                positiveTitle = "Take to system settings",
                negativeTitle = "Cancel"
            ).apply {
                positiveAction = {
                    startActivityIfNeeded(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        PERMISSIONS_REQUEST_SYSTEM_LOCATION
                    )
                }
            }.show(supportFragmentManager, null)
            return
        }
        checkAppLocationPermission {
            action()
        }
    }

    private fun isSystemLocationPermissionEnable(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isLocationEnabled
            } else {
                Settings.Secure.getInt(
                    contentResolver,
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
                ) != Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (e: Exception) {
            false
        }
    }
}
