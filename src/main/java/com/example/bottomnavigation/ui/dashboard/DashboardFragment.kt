package com.example.bottomnavigation.ui.dashboard


import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bottomnavigation.MainActivity.Companion.get_drawColorFlag
import com.example.bottomnavigation.MainActivity.Companion.get_drawEraserFlag
import com.example.bottomnavigation.MainActivity.Companion.get_drawView
import com.example.bottomnavigation.MainActivity.Companion.set_drawColorFlag
import com.example.bottomnavigation.MainActivity.Companion.set_drawEraserFlag
import com.example.bottomnavigation.MainActivity.Companion.set_drawView
import com.example.bottomnavigation.R
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.IOException

class DashboardFragment : Fragment() {

    //変数初期化
    private var root: View? = null
    private lateinit var paintView: PaintView
    private var defaultColor = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //View呼び出し判定
        if(get_drawView() == null){
            root = inflater.inflate(R.layout.fragment_dashboard, container, false)
            Log.d("Fragment", "onCreateView")
        }else{
            root = get_drawView()
            Log.d("Fragment", "onCreateView2")
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Viewを取得
        paintView = view.findViewById(R.id.view) as PaintView

        //消しゴムボタン縮小・透過セット(起動時にボタンが沈んでいるようにする)
        view?.let { setScale(it.eraser_imgButton) }

        //色替えボタンクリック処理
        view.change_color_imgButton.setOnTouchListener() {v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN ->{
                    Log.d("Fragment", "changeColorButton")
                    openColourPicker()
                }
            }
            true
        }

        //消しゴムボタンクリック処理
        view.eraser_imgButton.setOnTouchListener(){ v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_UP -> {
                    Log.d("Fragment", "resetButtonTouch")
                    paintView.erase()

                    resetScale(view.eraser_imgButton) //消しゴムボタン縮小・透過
                    setScale(view.change_color_imgButton) //色変えボタン縮小・透過リセット

                    //色変え・消しゴムフラグ反転
                    set_drawColorFlag()
                    set_drawEraserFlag()
                }
            }
            true
        }

        //リセットボタンクリック処理
        view.reset_imgButton.setOnTouchListener(){ v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    setScale(view.reset_imgButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    Log.d("Fragment", "resetButtonTouch")
                    paintView.clear()   //Viewクリア
                    resetScale(view.reset_imgButton)    //リセットボタン縮小・透過リセット
                }

            }
            true
        }

        //アンドゥボタンクリック処理
        view.undo_imgButton.setOnTouchListener(){ v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    setScale(view.undo_imgButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    paintView.undo()
                    resetScale(view.undo_imgButton)    //リセットボタン縮小・透過リセット
                }
            }
            true
        }

        //リドゥボタンクリック処理
        view.redo_imgButton.setOnTouchListener(){ v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    setScale(view.redo_imgButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    paintView.redo()
                    resetScale(view.redo_imgButton)    //リセットボタン縮小・透過リセット
                }
            }
            true
        }

        //ペンの太さ変更処理
        view.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            //値が変更された時に呼ばれる
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val paintView = view?.findViewById(R.id.view) as PaintView
                paintView.setStrokeWidth(seekBar!!.progress)
//                current_pen_size.text = "Pen size:" + seekBar.progress
            }
            //つまみがタッチされた時に呼ばれる
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            //つまみが離された時に呼ばれる
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        view.save_imgButton.setOnTouchListener(){ v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Fragment", "resetButtonTouch")
                    savedFile(paintView.getbitmap())
                    setScale(view.save_imgButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.save_imgButton)    //リセットボタン縮小・透過リセット
                }
            }
            true
        }
    }

    override fun onPause() {
        set_drawView(root)
        Log.d("Fragment", "onPause")
        super.onPause()
    }

    //色変更処理
    private fun openColourPicker() {
        val ambilWarnaDialog = AmbilWarnaDialog(context, defaultColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
            //色変えCancel処理
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                Toast.makeText(context, "Unavailable", Toast.LENGTH_LONG).show()
                //ボタン状態判定
                if (!get_drawColorFlag() && !get_drawEraserFlag()) {
                    view?.let { setScale(it.change_color_imgButton) }
                }else if(get_drawColorFlag() && !get_drawEraserFlag()) {
                    view?.let { resetScale(it.change_color_imgButton) }
                    view?.let { setScale(it.eraser_imgButton) }
                }else if(!get_drawColorFlag() && get_drawEraserFlag()) {
                    view?.let { setScale(it.change_color_imgButton) }
                }
            }

            //色変えOK処理
            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                defaultColor = color
                paintView.setColor(color)

                //ボタン切り替え
                view?.let { resetScale(it.change_color_imgButton) }
                view?.let { setScale(it.eraser_imgButton) }

                //色変え・消しゴムフラグ反転
                set_drawColorFlag()
                set_drawEraserFlag()
            }
        })
        ambilWarnaDialog.show()
    }

    //ボタン縮小・透過処理
    private fun setScale(view: View) {
        view.scaleY = 0.82f
        view.scaleX = 0.86f
        view.alpha = 0.55f
    }

    //ボタン縮小・透過リセット処理
    private fun resetScale(view: View) {
        view.scaleY = 1.0f
        view.scaleX = 1.0f
        view.alpha = 1.0f
    }

    private fun savedFile(bmp: Bitmap){
        if (isExternalStorageWritable) {//ストレージに書き込むことができるか
            val values = ContentValues()
            ////////// ファイル情報//////////////
            // ファイル名
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "Test.jpg")
            // ファイル拡張子の設定
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            //ファイルに排他的にアクセスする
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
            /////////////////////

            //////////////////////
            val resolver = activity?.applicationContext?.contentResolver
            val collection = MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val item = resolver?.insert(collection, values)
            /////////////////////

            ///////////画像をメディアに保存//////////
            try {
                activity?.contentResolver?.openOutputStream(item!!).use { outstream ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, outstream)//bitmapをjpeg,
                    Toast.makeText(activity, "画像を保存しました", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(activity, "画像保存に失敗しました。$e", Toast.LENGTH_SHORT).show()
            }
            values.clear()
            //　排他的にアクセスの解除
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver?.update(item!!, values, null, null)//ファイルを30個ぐらい保存してるとエラーが起きる。メモリ関連？
        }
    }

    //外部ストレージが読み取りと書き込みに使用できるかどうかを確認します
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

}

