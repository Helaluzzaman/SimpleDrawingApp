package com.hbsoft.simpledrawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.lang.Exception

class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var mDrawPath : CustomPath? = null
    val mPaths = ArrayList<CustomPath>()
    var mUndoPaths = ArrayList<CustomPath>()
    var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    var mBitmap: Bitmap? = null
    var mCanvas: Canvas? = null
    var mBrushSize: Float = 20f
    var mColor: Int = Color.BLACK
    init {
        initialSetting()
    }
    fun initialSetting(){
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mCanvasPaint!!.color = Color.RED
        mDrawPaint = Paint(Paint.DITHER_FLAG)
        mDrawPaint!!.color = mColor
        mDrawPaint!!.strokeWidth = mBrushSize
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.isAntiAlias = true
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPath = CustomPath(mColor,mBrushSize)
    }

    fun undoOnClick(){
        if(mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.lastIndex))
            invalidate()
        }
    }
    fun redoOnClick(){
        if(mUndoPaths.size > 0 ){
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.lastIndex))
            invalidate()
        }
    }
    fun undoAllonClick(): Boolean{
        if(mPaths.size > 0){
            mUndoPaths.addAll(mPaths.asReversed())
            mPaths.clear()
            invalidate()
            return true
        }
        return false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
//        mCanvas = Canvas(mBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
//        canvas?.drawBitmap(mBitmap!!,0F, 0F, mCanvasPaint)
        for ( path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushSize
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path, mDrawPaint!!)
            if(path.x !=null && path.y != null){
                canvas?.drawPoint(path.x!!, path.y!!, mDrawPaint!!)
            }
        }
        if(mDrawPath != null){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushSize
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = (event?.x)
        val touchY = event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath!!.color = mColor
                mDrawPath!!.brushSize = mBrushSize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.x = touchX
                        mDrawPath!!.y = touchY
                        mDrawPath!!.moveTo(touchX, touchY)
                        if(mUndoPaths.size > 0){
                            mUndoPaths.clear()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.x = null
                        mDrawPath!!.y = null
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
    }
    fun setColor(newColor:String){
        try{
            mColor = Color.parseColor(newColor)
        }catch (e: IllegalArgumentException){
            e.printStackTrace()
        }
    }

    fun setBrushSize(newSize: Float){
        val nBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mBrushSize = nBrushSize
    }
}

class CustomPath(
        var color: Int, var brushSize : Float,
        var x: Float? = null,
        var y: Float? = null) : Path() {

}
