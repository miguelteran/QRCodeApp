package com.matb.qr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GraphicOverlay(context: Context?, attrs: AttributeSet?) : View(context, attrs)
{
    private val paint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 7f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width.toFloat() / 2
        val cy = height.toFloat() / 2
        val rectWidth = width * 0.5f
        val rectHeight = rectWidth
        canvas.drawRect(
            cx - (rectWidth/2),
            cy - (rectHeight/2),
            cx + (rectWidth/2),
            cy + (rectHeight/2), paint
        );
    }
}