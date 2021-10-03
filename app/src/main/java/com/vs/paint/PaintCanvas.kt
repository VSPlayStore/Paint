package com.vs.paint

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.util.Base64.*
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class PaintCanvas(context: Context) : View(context) {

    companion object {
        lateinit var extraCanvas: Canvas
        lateinit var extraBitmap: Bitmap
        var path = Path()
        var encodedImage: String = ""
    }

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val edit: SharedPreferences.Editor = sharedPreferences.edit()

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        try {
            val encodedImage = sharedPreferences.getString("image_data", "")

            if (width > 0 && height > 0) {
                extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                extraCanvas = Canvas(extraBitmap)
                if (!encodedImage.equals("", ignoreCase = true)) {
                    val b: ByteArray = decode(encodedImage, DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                    extraCanvas.drawBitmap(bitmap, 0f, 0f, null)
                } else {
                    extraCanvas.drawColor(MainActivity.backgroundColor)
                }
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(
                context,
                "Some Thing is not right please restart the app",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun touchStart() {
        path.reset()
        extraCanvas.save()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        path.quadTo(
            currentX,
            currentY,
            (motionTouchEventX + currentX) / 2,
            (motionTouchEventY + currentY) / 2
        )
        currentX = motionTouchEventX
        currentY = motionTouchEventY
        extraCanvas.drawPath(path, MainActivity.paint)
        invalidate()
    }

    private fun touchUp() {
        path.reset()
        CoroutineScope(Dispatchers.IO).launch {
            saveCanvas()
        }
    }

    private fun saveCanvas() {
        val paint = ByteArrayOutputStream()
        extraBitmap.compress(Bitmap.CompressFormat.PNG, 100, paint)
        val byteArray: ByteArray = paint.toByteArray()

        encodedImage = encodeToString(byteArray, DEFAULT)
        edit.putString("image_data", encodedImage)
        edit.apply()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
}