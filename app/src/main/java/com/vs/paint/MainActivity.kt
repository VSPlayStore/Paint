package com.vs.paint

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.SdkInitializationListener
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubInterstitial
import com.vs.paint.databinding.ActivityMainBinding
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    companion object {
        var STROKE_WIDTH = 12f
        var ERASER_WIDTH = 12f
        lateinit var paint: Paint
        var backgroundColor = Color.parseColor("#FFFFFF")
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val code = 1
    private val addUnitID = ""
    private lateinit var mInterstitial: MoPubInterstitial

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseAnalytics = Firebase.analytics
        val configBuilder = SdkConfiguration.Builder(addUnitID)

        configBuilder.withLogLevel(MoPubLog.LogLevel.INFO)
        MoPub.initializeSdk(this, configBuilder.build(), initSdkListener())
        mInterstitial = MoPubInterstitial(this, addUnitID)

        requestStoragePermission()

        // Inflate fragment
        val fragment = PaintFragment()
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(R.id.frame, fragment, "PAINT_FRAGMENT").commit()

        var drawColor = ResourcesCompat.getColor(resources, R.color.White, null)
        paintStroke(drawColor, STROKE_WIDTH)
        binding.strokeWidth.progress = STROKE_WIDTH.toInt() / 2
        binding.eraserWidth.progress = ERASER_WIDTH.toInt()

        binding.strokeWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                STROKE_WIDTH = progress.toFloat() * 2
                paintStroke(drawColor, STROKE_WIDTH)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.eraserWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ERASER_WIDTH = progress.toFloat()
                paintStroke(Color.parseColor("#FFFFFF"), ERASER_WIDTH)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.eraser.setOnClickListener {
            binding.eraserWidth.progress = ERASER_WIDTH.toInt()
            paintStroke(Color.parseColor("#FFFFFF"), ERASER_WIDTH)
        }

        binding.saveBtn.setOnClickListener {
            requestStoragePermission()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                saveImage(PaintCanvas.extraBitmap, this)
                if (Build.VERSION.SDK_INT >= 29) {
                    Toast.makeText(this, "saved at /Pictures/Paint/", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "saved at /Paint/", Toast.LENGTH_SHORT).show()
                }
                if (mInterstitial.isReady) {
                    mInterstitial.show()
                }
            }
        }

        binding.strokeColor.setOnColorChangeListener { _, _, color ->
            drawColor = color
            paintStroke(drawColor, STROKE_WIDTH)
        }
    }

    private fun paintStroke(drawColor: Int, STROKE_WIDTH: Float) {
        paint = Paint().apply {
            color = drawColor
            isAntiAlias = true
            isDither =
                true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = STROKE_WIDTH
        }
    }

    private fun initSdkListener(): SdkInitializationListener {
        return SdkInitializationListener {
            mInterstitial.load()
        }
    }

    private fun saveImage(bitmap: Bitmap, context: Context) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Paint")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory =
                File(Environment.getExternalStorageDirectory().absolutePath + separator + "Paint")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATE_ADDED, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                code
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                code
            )
        }
    }
}
