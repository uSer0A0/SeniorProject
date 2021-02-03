package com.example.bottomnavigation.ui.home

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bottomnavigation.MainActivity
import com.example.bottomnavigation.MainActivity.Companion.colorList
import com.example.bottomnavigation.MainActivity.Companion.get_Bitmap
import com.example.bottomnavigation.MainActivity.Companion.get_Flag
import com.example.bottomnavigation.MainActivity.Companion.get_paintColorFlag
import com.example.bottomnavigation.MainActivity.Companion.get_paintEraserFlag
import com.example.bottomnavigation.MainActivity.Companion.get_paintView
import com.example.bottomnavigation.MainActivity.Companion.seedCoordinateList
import com.example.bottomnavigation.MainActivity.Companion.set_Bitmap
import com.example.bottomnavigation.MainActivity.Companion.set_Flag
import com.example.bottomnavigation.MainActivity.Companion.set_paintColorFlag
import com.example.bottomnavigation.MainActivity.Companion.set_paintEraserFlag
import com.example.bottomnavigation.MainActivity.Companion.set_paintView
import com.example.bottomnavigation.MainActivity.Companion.undoColorList
import com.example.bottomnavigation.MainActivity.Companion.undoSeedList
import com.example.bottomnavigation.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils.bitmapToMat
import org.opencv.android.Utils.matToBitmap
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.imgproc.Imgproc.floodFill
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.*
import java.util.*

class HomeFragment : Fragment() {

    //変数初期化
    private var root: View? = null
    private var defaultColor = 0
    private var count: Int = 0
    private val handler = Handler()

    //////////////色変数//////////////
    private var Red = 0.0
    private var Green = 0.0
    private var Blue = 0.0
    //////////////////////////////////

    val RESULT_PICK_IMAGEFILE = 1000
    private var workBitmap: Bitmap? = null //読み込んだ時のBitmap


    //必要な変数定義　初期値なし
    companion object {
        //よくわからん（ログを出すために必要？）
        private val TAG = MainActivity::class.java.simpleName

        //////////////初期値設定なしの変数//////////////
        private var bitmap: Bitmap? = null                 //setting()で使用
        private lateinit var r : Resources                  //setting()で使用
        private lateinit var mat: Mat                       //painting()で使用
        ///////////////////////////////////////////////

        init {
            //デバッグ用のログ表示
            Log.d(TAG, "static initializer: Trying to load OpenCV library.")
            OpenCVLoader.initDebug()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //View呼び出し判定
        if(get_paintView() == null) {
            root = inflater.inflate(R.layout.fragment_home, container, false)
            Log.d("Fragment", "onCreateView")
        }else {
            root = get_paintView()
            Log.d("Fragment", "onCreateView2")
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ぬりえスタートアニメーション
        view.animationView_main.visibility = View.VISIBLE
        handler.postDelayed({
            view.animationView_main.visibility = View.INVISIBLE
        }, 6000)

        //色替え・消しゴムボタン縮小・透過セット(起動時にボタンが沈んでいるようにする)
        view?.let { setScale(it.eraser_paint_imgButton) }

        //Fragment切り替え有無判定
        if(get_paintView() == null) {
            r = resources
            bitmap = BitmapFactory.decodeResource(r, R.drawable.pic4)
            view.imageView.setImageBitmap(bitmap)
            var bmp = bitmap as Bitmap
            saveImage(bmp, "painting.bmp")
        }else{
            bitmap = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/painting.bmp")
            view.imageView.setImageBitmap(bitmap)
            var bmp = bitmap as Bitmap
            saveImage(bmp, "painting.bmp")
        }

        //Viewタッチ処理
        view.imageView.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Fragment", "ImageView")
                    onTouch(event)
                }
            }
            true
        }

        //セーブボタンクリック処理
        view.saveButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Fragment", "ImageView")
                    if (get_Flag() == true) {
                        savedFile(bitmap!!)
                    } else {
                        savedFile(workBitmap!!)
                    }
                    setScale(view.saveButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.saveButton)  //リセットボタン縮小・透過

                    //ダウンロードアニメーション
                    view.animationView_save.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_save.visibility = View.INVISIBLE
                    }, 1760)

                }
            }
            true
        }

        //リードボタンクリック処理
        view.readButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Fragment", "ImageView")
                    AlertDialog.Builder(activity) // FragmentではActivityを取得して生成
                        .setTitle("注意！")
                        .setMessage("現在作業中の作品は保存されていません")
                        .setPositiveButton("キャンセル", { dialog, which ->
                        })
                        .setNegativeButton("無視して次へ", { dialog, which ->
                            readFile()
                        })
                        .setNeutralButton("保存して次へ", { dialog, which ->
                            savedFile(workBitmap!!)
                            readFile()
                        })
                        .show()
                    setScale(view.readButton)  //リセットボタン縮小・透過
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.readButton)

                    //リードアニメーション
                    view.animationView_read.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_read.visibility = View.INVISIBLE
                    }, 1000)
                }
            }
            true
        }


        //色変えボタンクリック処理
        view.change_paint_color_imgButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    openColourPicker()
                }

                MotionEvent.ACTION_UP -> {
                    //色変えアニメーション
                    view.animationView_change_color.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_change_color.visibility = View.INVISIBLE
                    }, 6000)
                }
            }
            true
        }

        //消しゴムボタンクリック処理
        view.eraser_paint_imgButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_UP -> {
                    erase()

                    resetScale(view.eraser_paint_imgButton)   //消しゴムボタン縮小・透過
                    setScale(view.change_paint_color_imgButton)   //色変えボタン縮小・透過リセット

                    //色変え・消しゴムフラグ反転
                    set_paintColorFlag()
                    set_paintEraserFlag()

                    //消しゴムアニメーション
                    view.animationView_eraser.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_eraser.visibility = View.INVISIBLE
                    }, 5000)

                }
            }
            true
        }

        view.reset_paint_imgButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    reset()
                    setting()
                    painting()
                    setScale(view.reset_paint_imgButton)
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.reset_paint_imgButton)

                    //リセットアニメーション
                    view.animationView_reset.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_reset.visibility = View.INVISIBLE
                    }, 2000)
                }
            }
            true
        }

        view.undo_paint_imgButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    undo()
                    setting()
                    painting()
                    setScale(view.undo_paint_imgButton)
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.undo_paint_imgButton)

                    //UNDOアニメーション
                    view.animationView_undo_redo.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_undo_redo.visibility = View.INVISIBLE
                    }, 3000)
                }
            }
            true
        }

        view.redo_paint_imgButton.setOnTouchListener() { v, event ->
            val action = event.action
            when(action){
                MotionEvent.ACTION_DOWN -> {
                    redo()
                    setting()
                    painting()
                    setScale(view.redo_paint_imgButton)
                }

                MotionEvent.ACTION_UP -> {
                    resetScale(view.redo_paint_imgButton)

                    //REDOアニメーション
                    view.animationView_undo_redo.visibility = View.VISIBLE
                    handler.postDelayed({
                        view.animationView_undo_redo.visibility = View.INVISIBLE
                    }, 3000)
                }
            }
            true
        }

        Log.d("Fragment", "ViewCreated")
    }

    override fun onPause() {
        set_paintView(root)
        set_Bitmap(bitmap)
        Log.d("Fragment", "onPause")
        super.onPause()
    }

    //画面タッチ処理
    private fun onTouch(event: MotionEvent) {
        setting()           //117行目
        addSeed(event.x.toDouble(), event.y.toDouble())
        painting()          //124行目
    }

    //処理対象画像セット処理
    private fun setting() {
        if(get_Bitmap() == null){
            //res内の情報を変数に格納
            r = resources
            //sampleの部分はdrawable内の画像を指定
            bitmap = BitmapFactory.decodeResource(r, R.drawable.pic4)
        }else{
            if(get_Flag() != true){
                //res内の情報を変数に格納
                r = resources
                //sampleの部分はdrawable内の画像を指定
                bitmap = BitmapFactory.decodeResource(r, R.drawable.pic4)
                workBitmap = Bitmap.createBitmap(
                    bitmap!!.width,
                    bitmap!!.height,
                    Bitmap.Config.ARGB_8888
                )

            }else{
                bitmap = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/painting.bmp")

            }
        }
    }

    //色塗り処理
    private fun painting() {
        if(!get_Flag()){
            Log.d("Fragment", "とぅるー")
            //塗りつぶす画像の情報を格納
            mat = Mat.zeros(bitmap!!.height, bitmap!!.width, CvType.CV_8UC1)

            //////////////グレー変換//////////////
            bitmapToMat(bitmap, mat)           // Bitmap -> Mat 変換
            cvtColor(mat, mat, 7)        // グレー画像へ変換 COLOR_BGR2GRAY = 7
            cvtColor(mat, mat, 8)       //グレーから元の色に変換　COLOR_GRAY2BGR = 8
            //////////////////////////////////////

            // タッチされた回数分だけループ
            for (seedCoordinate in seedCoordinateList) {
                //塗りつぶし開始地点を取得
                val seedPoint = Point(seedCoordinate.cX, seedCoordinate.cY)
                //塗りつぶす色を取得
                val color = getRGB()

                //塗りつぶし
                floodFill(
                    mat,
                    Mat(),
                    seedPoint,
                    color
                )
                //色情報のカウンター
                count++
                Log.d("Fragment", "seedCoordinate")
            }
            //////////////bitmap変換してImageViewに表示//////////////
            matToBitmap(mat, workBitmap)
            view?.imageView?.setImageBitmap(workBitmap)
            count = 0   //色を配列の先頭から取得するため
            ////////////////////////////////////////////////////////

            set_Flag(true)
            var bmp = bitmap as Bitmap
            saveImage(bmp, "painting.bmp")
        }else{
            Log.d("Fragment", "ふぉるす")
            //塗りつぶす画像の情報を格納
            mat = Mat.zeros(bitmap!!.height, bitmap!!.width, CvType.CV_8UC1)

            //////////////グレー変換//////////////
            bitmapToMat(bitmap, mat)           // Bitmap -> Mat 変換
            cvtColor(mat, mat, 7)        // グレー画像へ変換 COLOR_BGR2GRAY = 7
            cvtColor(mat, mat, 8)       //グレーから元の色に変換　COLOR_GRAY2BGR = 8
            //////////////////////////////////////

            // タッチされた回数分だけループ
            for (seedCoordinate in seedCoordinateList) {
                //塗りつぶし開始地点を取得
                val seedPoint = Point(seedCoordinate.cX, seedCoordinate.cY)
                //塗りつぶす色を取得
                val color = getRGB()

                //塗りつぶし
                floodFill(
                    mat,
                    Mat(),
                    seedPoint,
                    color
                )
                //色情報のカウンター
                count++
                Log.d("Fragment", "seedCoordinate")
            }
            //////////////bitmap変換してImageViewに表示//////////////
            matToBitmap(mat, bitmap)
            view?.imageView?.setImageBitmap(bitmap)
            count = 0   //色を配列の先頭から取得するため
            ////////////////////////////////////////////////////////
        }

        var bmp = bitmap as Bitmap
        saveImage(bmp, "painting.bmp")
    }

    //消しゴム処理
    private fun erase() {
        //白色をRGBに変換
        var Red = Color.red(Color.WHITE).toDouble()
        var Green = Color.green(Color.WHITE).toDouble()
        var Blue = Color.blue(Color.WHITE).toDouble()
        setRGB(Red, Green, Blue)
    }

    //リセット処理
    private fun reset() {
        seedCoordinateList.clear()
        colorList.clear()
    }

    //アンドゥボタン処理
    private fun undo() {
        if (seedCoordinateList.size > 0 && colorList.size > 0) {
            undoSeedList.add(seedCoordinateList.removeAt(seedCoordinateList.size - 1))
            undoColorList.add(colorList.removeAt(colorList.size - 1))
        }
    }

    //リドゥボタン処理
    private fun redo() {
        if (undoSeedList.size > 0 && undoColorList.size > 0) {
            seedCoordinateList.add(undoSeedList.removeAt(undoSeedList.size - 1))
            colorList.add(undoColorList.removeAt(undoColorList.size - 1))
        }
    }

    open fun saveImage(bmp: Bitmap, fname: String) {
        try {
            val extStrageDir = Environment.getExternalStorageDirectory()
            val file = File(
                extStrageDir.absolutePath
                        + "/" + Environment.DIRECTORY_DCIM,
                fname
            )
            val outStream = FileOutputStream(file)
            bmp!!.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            outStream.close()
            Log.d("HomeFragment", "saveImage:$file")
            /*
            Toast.makeText(
                    context,
                    "Image saved",
                    Toast.LENGTH_SHORT).show()
            */
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //タッチされた座標を取得する
    private fun addSeed(x: Double, y: Double) {
        //タッチ座標取得（X・Y）
        var pointX = x
        var pointY = y

        //画像比率合わせ
        pointX = pointX * bitmap!!.width / imageView.width
        pointY = pointY * bitmap!!.height / imageView.height

        //取得した座標をクラス変数に格納する
        addSeed(MainActivity.SeedCoordinate(pointX, pointY))
    }

    private fun addSeed(seedCoordinate: MainActivity.SeedCoordinate) {
        //SeedCoordinateクラスに座標を格納
        seedCoordinateList.add(seedCoordinate)
    }

    private fun setRGB(R: Double, G: Double, B: Double){
        if(seedCoordinateList.size == colorList.size){
            //通常の場合
            setRGB(MainActivity.ColorCoordinate(R, G, B))
        } else{
            //色選択ボタンが押されず同じ色を引き続き使う場合
            colorList[colorList.size - 1] = MainActivity.ColorCoordinate(R, G, B)
        }
    }

    private fun setRGB(colorcoordinate: MainActivity.ColorCoordinate){
        //カラーリストクラスに色情報を保存
        colorList.add(colorcoordinate)
    }

    private fun getRGB(): Scalar {
        when {
            colorList.size == 0 -> {
                //色情報が入っていないときの色を黒色にする
                Red = 0.0
                Green = 0.0
                Blue = 0.0
                setRGB(MainActivity.ColorCoordinate(Red, Green, Blue))
            }
            colorList.size-1 < count -> {
                //色選択ボタンが押されていないときは1つ前に選択した色を指定する
                Red = colorList[colorList.size - 1].red
                Green = colorList[colorList.size - 1].green
                Blue = colorList[colorList.size - 1].blue
                setRGB(MainActivity.ColorCoordinate(Red, Green, Blue))
            }
            else -> {
                //色選択ボタンで押された色にする
                Red = colorList[count].red
                Green = colorList[count].green
                Blue = colorList[count].blue
            }
        }
        //色情報を返す
        return Scalar(Red, Green, Blue)
    }

    //色変更するための処理
    private fun openColourPicker() {
        val ambilWarnaDialog = AmbilWarnaDialog(
            context,
            defaultColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                //色変えCancel処理
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    Toast.makeText(context, "Unavailable", Toast.LENGTH_LONG).show()
                    //ボタン状態判定
                    if (!get_paintColorFlag() && !get_paintEraserFlag()) {
                        view?.let { setScale(it.change_paint_color_imgButton) }
                    } else if (get_paintColorFlag() && !get_paintEraserFlag()) {
                        view?.let { resetScale(it.change_paint_color_imgButton) }
                        view?.let { setScale(it.eraser_paint_imgButton) }
                    } else if (!get_paintColorFlag() && get_paintEraserFlag()) {
                        view?.let { setScale(it.change_paint_color_imgButton) }
                    }
                }

                //色変えOK処理
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    //選択された色をRGBに変換
                    var Red = Color.red(color).toDouble()
                    var Green = Color.green(color).toDouble()
                    var Blue = Color.blue(color).toDouble()
                    setRGB(Red, Green, Blue)

                    view?.let { resetScale(it.change_paint_color_imgButton) } //色変えボタン縮小・透過
                    view?.let { setScale(it.eraser_paint_imgButton) } //消しゴムボタン縮小・透過リセット

                    //色変え・消しゴムフラグ反転
                    set_paintColorFlag()
                    set_paintEraserFlag()
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
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
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
    /////////読み込み処理/////////////
    private fun readFile(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
    }

    ///////////外部の画面(今回は画像選択が内部のメディアから取るとき)から戻ってきたとき///////////////
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == AppCompatActivity.RESULT_OK) {
            var uri: Uri? = null
            if (data != null) {
                uri = data.data;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                    var bmp = bitmap as Bitmap
                    saveImage(bmp, "painting.bmp")
                    set_Flag(true)
                    workBitmap = Bitmap.createBitmap(
                        bitmap!!.width,
                        bitmap!!.height,
                        Bitmap.Config.ARGB_8888
                    )
                    imageView.setImageBitmap(bitmap)
                    //画像が読み込まれたときリストを初期化している
                    seedCoordinateList.clear()
                    colorList.clear()

                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
    }

}