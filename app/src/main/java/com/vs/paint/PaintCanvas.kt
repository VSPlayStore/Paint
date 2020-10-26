package com.vs.paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

private var motionTouchEventX = 0f
private var motionTouchEventY = 0f
private var currentX = 0f
private var currentY = 0f

class PaintCanvas(context: Context) : View(context) {

    companion object {
        lateinit var extraCanvas: Canvas
        lateinit var extraBitmap: Bitmap
        var path = Path()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        try {
            if (width > 0 && height > 0) {
                extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                extraCanvas = Canvas(extraBitmap)
                extraCanvas.drawColor(MainActivity.backgroundColor)
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
}