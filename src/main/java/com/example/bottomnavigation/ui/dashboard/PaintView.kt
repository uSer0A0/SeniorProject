package com.example.bottomnavigation.ui.dashboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.paint.Draw
import java.util.*


class PaintView : View {

    //変数初期化
    private lateinit var paint: Paint
    private lateinit var path: Path
    private var mX: Float = 0F
    private var mY: Float = 0F
    private var strokeWidth = BRUSH_SIZE
    private var currentColor = DEFAULT_COLOR

    private val paths = ArrayList<Draw>()
    private val undo = ArrayList<Draw>()

    private var image: Bitmap = Bitmap.createBitmap(1140, 1240, Bitmap.Config.ARGB_8888)

    //新しいcanvasを生成
    val cv = Canvas(image)
    var frag =true

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { //AttributeSetでAttributeにsetできるようにする

        //画面に線を書くためのPaintとPathを用意する
        paint = Paint()
        path = Path()

        //線の色や開始終了の形を決める
        paint!!.color = -0x1000000 //線の色を黒に
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeJoin = Paint.Join.ROUND //線のつなぎ目を丸く
        paint!!.strokeCap = Paint.Cap.ROUND //線の端面を丸く
        paint!!.strokeWidth = 10f
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint.setXfermode(null)
        paint.setAlpha(0xff)
    }

    //変数に初期値を設定
    companion object {
        var BRUSH_SIZE = 10
        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_BG_COLOR = Color.WHITE
        private const val TOUCH_TOLERANCE = 4f
    }

    //線描画メソッド
    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.drawColor(DEFAULT_BG_COLOR)

        for (draw in paths) {
            paint.setColor(draw.color)
            paint.setStrokeWidth(draw.strokeWidth)
            paint.setMaskFilter(null)
            canvas.drawPath(draw.path, paint)

            //保存用の画像を作るための処理
            //背景色を設定（デフォルトでは透明か黒？）
            if(frag) {//背景色設定は一度のみ
                cv.drawColor(Color.WHITE)
                frag = false
            }
            cv.drawPath(draw.path, paint)
        }
        canvas.restore()
    }

    //タッチされ始めの処理
    private fun touchStart(x: Float, y: Float) {
        path = Path()
        val draw = Draw(currentColor, strokeWidth.toFloat(), path)
        paths.add(draw)
        path.reset()
        path.moveTo(x, y)
        mX = x
        mY = y
    }

    //タッチされた状態の処理
    private fun touchMove(x: Float, y: Float) {
        val dx: Float = Math.abs(x - mX)
        val dy: Float = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    //タッチ終わりの処理
    private fun touchUp() {
        path.lineTo(mX, mY)
    }

    //viewがタッチされたときの処理
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
        }
        return true
    }

    //描いた絵を削除するメソッド。resetButtonクリック時に呼ばれる
    fun clear() {
        paths.clear()
        invalidate()
        cv.drawColor(Color.WHITE)
    }

    //消しゴム機能
    fun erase() {
        currentColor = Color.WHITE  //ペンの色を白に変更
    }

    //アンドゥ処理
    fun undo() {
        if (paths.size > 0) {
            undo.add(paths.removeAt(paths.size - 1))
            invalidate()
        }
    }

    //リドゥ処理
    fun redo() {
        if (undo.size > 0) {
            paths.add(undo.removeAt(undo.size - 1))
            invalidate()
        }
    }

    //ペンの太さ変更
    fun setStrokeWidth(width: Int) {
        strokeWidth = width
    }

    //ペンの色変更
    fun setColor(color: Int) {
        currentColor = color
    }

    fun getbitmap():Bitmap{
        return image
    }

}